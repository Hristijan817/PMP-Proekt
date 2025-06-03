package com.example.eating.data

import androidx.room.*
import com.example.eating.model.GroceryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_items")
    fun getAllItems(): Flow<List<GroceryItem>>

    @Insert
    suspend fun insert(item: GroceryItem)

    @Delete
    suspend fun delete(item: GroceryItem)
}
