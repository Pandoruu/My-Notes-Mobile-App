package com.example.mynotes.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.mynotes.data.local.dao.CategoryDao
import com.example.mynotes.data.mapper.toDomain
import com.example.mynotes.data.mapper.toEntity
import com.example.mynotes.domain.model.Category
import com.example.mynotes.domain.repository.CategoryRepository as DomainCategoryRepository

class CategoryRepository(
    private val categoryDao: CategoryDao
) : DomainCategoryRepository {
    // Category
    override suspend fun addCategory(category: Category): Long = categoryDao.insert(category.toEntity())
    override suspend fun updateCategory(category: Category) = categoryDao.update(category.toEntity())
    override suspend fun deleteCategory(category: Category) = categoryDao.delete(category.toEntity())

    override fun observeCategoriesByUser(userId: Int): LiveData<List<Category>> =
        categoryDao.observeByUser(userId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getCategoryByName(userId: Int, name: String): Category? =
        categoryDao.getCategoryByNameOnce(userId, name)?.toDomain()
}