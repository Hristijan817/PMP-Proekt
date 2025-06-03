package com.example.eating

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatingTheme {
                var selectedScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Recipes) }
                val bottomScreens = listOf(BottomNavScreen.Recipes, BottomNavScreen.GroceryList)

                val context = LocalContext.current
                var expanded by remember { mutableStateOf(false) }
                var selectedLanguage by remember { mutableStateOf("en") }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("DIY Eats") },
                            actions = {
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Language")
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("English") },
                                            onClick = {
                                                selectedLanguage = "en"
                                                LocaleHelper.setLocale(context, "en")
                                                context.startActivity(Intent(context, MainActivity::class.java))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Македонски") },
                                            onClick = {
                                                selectedLanguage = "mk"
                                                LocaleHelper.setLocale(context, "mk")
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
                                    label = { Text(screen.title) },
                                    icon = {
                                        Icon(imageVector = Icons.Default.Menu, contentDescription = screen.title)
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Recipes Screen")
    }
}

@Composable
fun GroceryListScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Grocery List Screen")
    }
}
