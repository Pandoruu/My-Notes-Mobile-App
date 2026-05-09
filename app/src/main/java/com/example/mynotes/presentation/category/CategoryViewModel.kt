package com.example.mynotes.presentation.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mynotes.domain.model.Category
import com.example.mynotes.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.example.mynotes.domain.usecase.category.AddCategoryUseCase
import com.example.mynotes.domain.usecase.category.DeleteCategoryUseCase
import com.example.mynotes.domain.usecase.category.ObserveCategoriesUseCase
import com.example.mynotes.domain.usecase.category.UpdateCategoryUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CategoryViewModel(
    observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val currentUserIdFlow = observeCurrentUserIdUseCase()
    val currentUserId: LiveData<Int?> = currentUserIdFlow.asLiveData()

    fun observeCategories(): LiveData<List<Category>> =
        currentUserId.switchMap { userId ->
            if (userId == null) MutableLiveData(emptyList()) else observeCategoriesUseCase(userId)
        }

    fun addCategory(name: String) {
        viewModelScope.launch {
            val userId = currentUserIdFlow.first() ?: return@launch
            addCategoryUseCase(userId, name)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch { updateCategoryUseCase(category) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { deleteCategoryUseCase(category) }
    }

    class Factory(
        private val observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
        private val observeCategoriesUseCase: ObserveCategoriesUseCase,
        private val addCategoryUseCase: AddCategoryUseCase,
        private val updateCategoryUseCase: UpdateCategoryUseCase,
        private val deleteCategoryUseCase: DeleteCategoryUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoryViewModel(
                observeCurrentUserIdUseCase = observeCurrentUserIdUseCase,
                observeCategoriesUseCase = observeCategoriesUseCase,
                addCategoryUseCase = addCategoryUseCase,
                updateCategoryUseCase = updateCategoryUseCase,
                deleteCategoryUseCase = deleteCategoryUseCase
            ) as T
        }
    }
}

