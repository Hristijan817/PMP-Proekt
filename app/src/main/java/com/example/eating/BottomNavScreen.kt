package com.example.eating.navigation

import com.example.eating.R

sealed class BottomNavScreen(val route: String, val titleResId: Int) {
    object Recipes : BottomNavScreen("recipes", R.string.nav_recipes)
    object GroceryList : BottomNavScreen("grocery", R.string.nav_grocery)
}