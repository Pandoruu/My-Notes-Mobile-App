package com.example.mynotes.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynotes.database.table.Category

@Dao
interface CategoryDao {
    @Insert suspend fun insert(category: Category): Long
    @Update suspend fun update(category: Category)
    @Delete suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE user_id = :userId")
    fun getUser(userId: Int): LiveData<List<Category>>
}