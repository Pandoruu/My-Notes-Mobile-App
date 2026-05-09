package com.example.mynotes.domain.usecase.category

import com.example.mynotes.domain.model.Category
import com.example.mynotes.domain.repository.CategoryRepository

class UpdateCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category) {
        categoryRepository.updateCategory(category)
    }
}

