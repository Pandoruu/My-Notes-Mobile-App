package com.example.mynotes.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.example.mynotes.domain.usecase.notes.SearchNotesUseCase
import com.example.mynotes.domain.usecase.notes.ToggleFavoriteUseCase
import com.example.mynotes.domain.usecase.notes.TogglePinUseCase
import com.example.mynotes.domain.util.TextNormalizer
import kotlinx.coroutines.launch

class SearchViewModel(
    observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val togglePinUseCase: TogglePinUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val currentUserIdLiveData = observeCurrentUserIdUseCase().asLiveData()

    fun search(query: String): LiveData<List<Note>> {
        val normalizedQuery = TextNormalizer.removeAccents(query.lowercase())
        return currentUserIdLiveData.switchMap { userId ->
            if (userId == null) MutableLiveData(emptyList())
            else searchNotesUseCase(userId, normalizedQuery)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { togglePinUseCase(note) }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch { toggleFavoriteUseCase(note) }
    }

    class Factory(
        private val observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
        private val searchNotesUseCase: SearchNotesUseCase,
        private val togglePinUseCase: TogglePinUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(
                observeCurrentUserIdUseCase = observeCurrentUserIdUseCase,
                searchNotesUseCase = searchNotesUseCase,
                togglePinUseCase = togglePinUseCase,
                toggleFavoriteUseCase = toggleFavoriteUseCase
            ) as T
        }
    }
}

