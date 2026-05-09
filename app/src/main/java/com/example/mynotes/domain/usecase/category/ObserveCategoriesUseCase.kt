package com.example.mynotes.domain.usecase.category

import androidx.lifecycle.LiveData
import com.example.mynotes.domain.model.Category
import com.example.mynotes.domain.repository.CategoryRepository

class ObserveCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(userId: Int): LiveData<List<Category>> =
        categoryRepository.observeCategoriesByUser(userId)
}

