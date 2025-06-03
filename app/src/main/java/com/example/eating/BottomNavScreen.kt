package com.example.eating.navigation

sealed class BottomNavScreen(val route: String, val title: String) {
    object Recipes : BottomNavScreen("recipes", "Recipes")
    object GroceryList : BottomNavScreen("grocery", "Grocery")
}
