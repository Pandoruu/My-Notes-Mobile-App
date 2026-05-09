package com.example.mynotes.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mynotes.domain.model.Category
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.example.mynotes.domain.usecase.category.ObserveCategoriesUseCase
import com.example.mynotes.domain.usecase.notes.ObserveAllNotesUseCase
import com.example.mynotes.domain.usecase.notes.ObserveNotesByCategoryUseCase
import com.example.mynotes.domain.usecase.notes.ToggleFavoriteUseCase
import com.example.mynotes.domain.usecase.notes.TogglePinUseCase
import kotlinx.coroutines.launch

class NotesListViewModel(
    observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val observeAllNotesUseCase: ObserveAllNotesUseCase,
    private val observeNotesByCategoryUseCase: ObserveNotesByCategoryUseCase,
    private val togglePinUseCase: TogglePinUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val currentUserIdLiveData = observeCurrentUserIdUseCase().asLiveData()

    fun observeCategories(): LiveData<List<Category>> =
        currentUserIdLiveData.switchMap { userId ->
            if (userId == null) MutableLiveData(emptyList()) else observeCategoriesUseCase(userId)
        }

    fun observeAllNotes(): LiveData<List<Note>> =
        currentUserIdLiveData.switchMap { userId ->
            if (userId == null) MutableLiveData(emptyList()) else observeAllNotesUseCase(userId)
        }

    fun observeNotesByCategory(categoryId: Int): LiveData<List<Note>> =
        currentUserIdLiveData.switchMap { userId ->
            if (userId == null) MutableLiveData(emptyList())
            else observeNotesByCategoryUseCase(userId, categoryId)
        }

    fun togglePin(note: Note) {
        viewModelScope.launch { togglePinUseCase(note) }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch { toggleFavoriteUseCase(note) }
    }

    class Factory(
        private val observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
        private val observeCategoriesUseCase: ObserveCategoriesUseCase,
        private val observeAllNotesUseCase: ObserveAllNotesUseCase,
        private val observeNotesByCategoryUseCase: ObserveNotesByCategoryUseCase,
        private val togglePinUseCase: TogglePinUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesListViewModel(
                observeCurrentUserIdUseCase = observeCurrentUserIdUseCase,
                observeCategoriesUseCase = observeCategoriesUseCase,
                observeAllNotesUseCase = observeAllNotesUseCase,
                observeNotesByCategoryUseCase = observeNotesByCategoryUseCase,
                togglePinUseCase = togglePinUseCase,
                toggleFavoriteUseCase = toggleFavoriteUseCase
            ) as T
        }
    }
}

