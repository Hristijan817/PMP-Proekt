package com.example.eating.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.eating.model.GroceryItem

@Database(entities = [GroceryItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groceryDao(): GroceryDao
}
