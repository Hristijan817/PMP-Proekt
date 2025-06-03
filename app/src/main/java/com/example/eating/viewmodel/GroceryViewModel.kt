package com.example.eating.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.room.Room
import com.example.eating.data.AppDatabase
import com.example.eating.model.GroceryItem
import kotlinx.coroutines.launch

class GroceryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "grocery_db"
    ).build()

    val items = db.groceryDao().getAllItems().asLiveData()

    fun addItem(name: String) {
        viewModelScope.launch {
            db.groceryDao().insert(GroceryItem(name = name))
        }
    }

    fun deleteItem(item: GroceryItem) {
        viewModelScope.launch {
            db.groceryDao().delete(item)
        }
    }
}
