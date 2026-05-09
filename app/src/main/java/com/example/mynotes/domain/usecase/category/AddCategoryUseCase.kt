package com.example.mynotes.domain.usecase.category

import com.example.mynotes.domain.model.Category
import com.example.mynotes.domain.repository.CategoryRepository

class AddCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: Int, name: String): Long {
        return categoryRepository.addCategory(Category(userId = userId, name = name))
    }
}

