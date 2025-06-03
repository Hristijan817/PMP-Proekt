package com.example.eating

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.eating.navigation.BottomNavScreen
import com.example.eating.ui.theme.EatingTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.example.eating.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatingTheme {
                var selectedScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Recipes) }

                val bottomScreens = listOf(
                    BottomNavScreen.Recipes,
                    BottomNavScreen.GroceryList
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomScreens.forEach { screen ->
                                NavigationBarItem(
                                    selected = selectedScreen.route == screen.route,
                                    onClick = { selectedScreen = screen },
                                    label = { Text(screen.title) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = screen.title
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedScreen) {
                            is BottomNavScreen.Recipes -> RecipesScreen()
                            is BottomNavScreen.GroceryList -> GroceryListScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipesScreen() {
    val db = FirebaseFirestore.getInstance()
    val recipes = remember { mutableStateListOf<Recipe>() }
    var isLoading by remember { mutableStateOf(true) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Trigger to reload
    var reloadTrigger by remember { mutableStateOf(0) }

    val mainScope = rememberCoroutineScope()

    LaunchedEffect(reloadTrigger) {
        isLoading = true
        db.collection("recipes").get()
            .addOnSuccessListener { result ->
                mainScope.launch(Dispatchers.Main) {
                    recipes.clear()
                    recipes.addAll(result.documents.map { doc ->
                        Recipe(
                            id = doc.id,
                            title = doc.getString("title") ?: "No title",
                            description = doc.getString("description") ?: "No description"
                        )
                    })
                    isLoading = false
                }
            }
            .addOnFailureListener {
                mainScope.launch(Dispatchers.Main) {
                    isLoading = false
                }
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val newRecipe = hashMapOf(
                            "title" to title,
                            "description" to description
                        )
                        db.collection("recipes").add(newRecipe).addOnSuccessListener {
                            title = ""
                            description = ""
                            reloadTrigger++ // ✅ re-trigger on main thread
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add Recipe")
            }

            Spacer(modifier = Modifier.height(24.dp))

            recipes.forEach { recipe ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                db.collection("recipes")
                                    .document(recipe.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        reloadTrigger++
                                    }
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }
        }
    }
}



fun loadRecipes(db: FirebaseFirestore, recipes: SnapshotStateList<Recipe>) {
    db.collection("recipes").get()
        .addOnSuccessListener { result ->
            recipes.clear()
            recipes.addAll(result.documents.map { doc ->
                Recipe(
                    id = doc.id,
                    title = doc.getString("title") ?: "No title",
                    description = doc.getString("description") ?: "No description"
                )
            })
        }
}

@Composable
fun GroceryListScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Grocery List Screen")
    }
}
