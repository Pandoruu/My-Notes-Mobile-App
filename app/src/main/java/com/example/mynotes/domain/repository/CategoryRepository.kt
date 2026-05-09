package com.example.mynotes.domain.repository

import androidx.lifecycle.LiveData
import com.example.mynotes.domain.model.Category

interface CategoryRepository {
    suspend fun addCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)

    fun observeCategoriesByUser(userId: Int): LiveData<List<Category>>
    suspend fun getCategoryByName(userId: Int, name: String): Category?
}

