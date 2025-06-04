package com.example.eating

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eating.model.Recipe
import com.example.eating.model.GroceryItem
import com.example.eating.navigation.BottomNavScreen
import com.example.eating.ui.theme.EatingTheme
import com.example.eating.viewmodel.GroceryViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.applyLocale(newBase)
        super.attachBaseContext(context)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatingTheme {
                var selectedScreen: BottomNavScreen by remember { mutableStateOf(BottomNavScreen.Recipes) }
                val bottomScreens = listOf(BottomNavScreen.Recipes, BottomNavScreen.GroceryList)
                val context = LocalContext.current
                var expanded by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = context.getString(R.string.app_name)) },
                            actions = {
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.Menu, contentDescription = context.getString(R.string.language))
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(context.getString(R.string.english)) },
                                            onClick = {
                                                LocaleHelper.saveLanguage(context, "en")
                                                context.startActivity(Intent(context, MainActivity::class.java))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(context.getString(R.string.macedonian)) },
                                            onClick = {
                                                LocaleHelper.saveLanguage(context, "mk")
                                                context.startActivity(Intent(context, MainActivity::class.java))
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            bottomScreens.forEach { screen ->
                                NavigationBarItem(
                                    selected = selectedScreen.route == screen.route,
                                    onClick = { selectedScreen = screen },
                                    label = { Text(text = context.getString(screen.titleResId)) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = context.getString(screen.titleResId)
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (selectedScreen) {
                            BottomNavScreen.Recipes -> RecipesScreen()
                            BottomNavScreen.GroceryList -> GroceryListScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipesScreen() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val recipes = remember { mutableStateListOf<Recipe>() }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reloadTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reloadTrigger) {
        db.collection("recipes").get()
            .addOnSuccessListener { result ->
                scope.launch(Dispatchers.Main) {
                    recipes.clear()
                    recipes.addAll(result.map { doc ->
                        Recipe(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: ""
                        )
                    })
                }
            }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(context.getString(R.string.title)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(context.getString(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (title.isNotBlank() && description.isNotBlank()) {
                db.collection("recipes")
                    .add(mapOf("title" to title, "description" to description))
                    .addOnSuccessListener {
                        title = ""
                        description = ""
                        reloadTrigger++
                    }
            }
        }) {
            Text(context.getString(R.string.add_recipe))
        }

        Spacer(modifier = Modifier.height(16.dp))

        recipes.forEach { recipe ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                    Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            db.collection("recipes").document(recipe.id).delete()
                                .addOnSuccessListener { reloadTrigger++ }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(context.getString(R.string.delete), color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}

@Composable
fun GroceryListScreen() {
    val context = LocalContext.current
    val viewModel: GroceryViewModel = viewModel(factory = viewModelFactory {
        initializer {
            GroceryViewModel(context.applicationContext as android.app.Application)
        }
    })

    val items by viewModel.items.observeAsState(emptyList())
    var newItem by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = newItem,
            onValueChange = { newItem = it },
            label = { Text(context.getString(R.string.new_item)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (newItem.isNotBlank()) {
                viewModel.addItem(newItem)
                newItem = ""
            }
        }) {
            Text(context.getString(R.string.add))
        }

        Spacer(modifier = Modifier.height(16.dp))

        items.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.name)
                    Button(
                        onClick = { viewModel.deleteItem(item) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(context.getString(R.string.delete), color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}