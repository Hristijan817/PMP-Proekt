package com.example.eating

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.compose.foundation.Image


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
                            title = {
                                Text(
                                    text = when (selectedScreen) {
                                        BottomNavScreen.Recipes -> "📖 DIY Eats: Recipes"
                                        BottomNavScreen.GroceryList -> "🛒 DIY Eats: Grocery"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = when (selectedScreen) {
                                    BottomNavScreen.Recipes -> Color(0xFF56AB2F)
                                    BottomNavScreen.GroceryList -> Color(0xFF2980B9)
                                }
                            ),
                            actions = {
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.Menu, contentDescription = context.getString(R.string.language), tint = Color.White)
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
                    } ,

                    floatingActionButton = {
                        when (selectedScreen) {
                            BottomNavScreen.Recipes -> AddRecipeFab()
                            BottomNavScreen.GroceryList -> AddGroceryFab()
                        }
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
                    val modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    when (selectedScreen) {
                        BottomNavScreen.Recipes -> RecipesScreen(modifier)
                        BottomNavScreen.GroceryList -> GroceryListScreen(modifier)
                    }
                }
            }
        }
    }
}

@Composable
fun RecipesScreen(modifier: Modifier = Modifier) {
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

    Column(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFA8E063), Color(0xFF56AB2F))
                )
            )
            .padding(16.dp)
    ) {
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

        if (recipes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.empty_recipes),
                    contentDescription = "Empty recipes",
                    modifier = Modifier
                        .height(240.dp)
                        .padding(bottom = 12.dp)
                )
                Text(
                    text = "No recipes yet!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        } else {
            recipes.forEach { recipe ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
}





@Composable
fun GroceryListScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: GroceryViewModel = viewModel(factory = viewModelFactory {
        initializer {
            GroceryViewModel(context.applicationContext as android.app.Application)
        }
    })

    val items by viewModel.items.observeAsState(emptyList())
    var newItem by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF6DD5FA), Color(0xFF2980B9))
                )
            )
            .padding(16.dp)
    ) {
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

        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.empty_grocery),
                    contentDescription = "Empty grocery",
                    modifier = Modifier
                        .height(240.dp)
                        .padding(bottom = 12.dp)
                )
                Text(
                    text = "Your grocery list is empty!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        } else {
            items.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🛒 ${item.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF333333)
                        )
                        Button(
                            onClick = { viewModel.deleteItem(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("🗑 " + context.getString(R.string.delete), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AddRecipeFab() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        db.collection("recipes").add(
                            mapOf("title" to title, "description" to description)
                        ).addOnSuccessListener {
                            title = ""
                            description = ""
                            showDialog = false
                        }
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Add Recipe") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") }
                    )
                }
            }
        )
    }

    FloatingActionButton(
        onClick = { showDialog = true },
        containerColor = Color(0xFF4CAF50)
    ) {
        Text("+", color = Color.White)
    }
}

@Composable
fun AddGroceryFab() {
    val context = LocalContext.current
    val viewModel: GroceryViewModel = viewModel(factory = viewModelFactory {
        initializer {
            GroceryViewModel(context.applicationContext as android.app.Application)
        }
    })

    var showDialog by remember { mutableStateOf(false) }
    var newItem by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (newItem.isNotBlank()) {
                        viewModel.addItem(newItem)
                        newItem = ""
                        showDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Add Grocery Item") },
            text = {
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    label = { Text("Item name") }
                )
            }
        )
    }

    FloatingActionButton(
        onClick = { showDialog = true },
        containerColor = Color(0xFF2196F3)
    ) {
        Text("+", color = Color.White)
    }
}

