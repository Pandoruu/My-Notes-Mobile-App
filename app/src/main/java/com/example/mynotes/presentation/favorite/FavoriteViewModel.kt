package com.example.mynotes.presentation.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.example.mynotes.domain.usecase.notes.ObserveFavoriteNotesUseCase
import com.example.mynotes.domain.usecase.notes.ToggleFavoriteUseCase
import com.example.mynotes.domain.usecase.notes.TogglePinUseCase
import kotlinx.coroutines.launch

class FavoriteViewModel(
    observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
    private val observeFavoriteNotesUseCase: ObserveFavoriteNotesUseCase,
    private val togglePinUseCase: TogglePinUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val currentUserIdLiveData = observeCurrentUserIdUseCase().asLiveData()

    fun observeFavoriteNotes(): LiveData<List<Note>> =
        currentUserIdLiveData.switchMap { userId ->
            if (userId == null) MutableLiveData(emptyList()) else observeFavoriteNotesUseCase(userId)
        }

    fun togglePin(note: Note) {
        viewModelScope.launch { togglePinUseCase(note) }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch { toggleFavoriteUseCase(note) }
    }

    class Factory(
        private val observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
        private val observeFavoriteNotesUseCase: ObserveFavoriteNotesUseCase,
        private val togglePinUseCase: TogglePinUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoriteViewModel(
                observeCurrentUserIdUseCase = observeCurrentUserIdUseCase,
                observeFavoriteNotesUseCase = observeFavoriteNotesUseCase,
                togglePinUseCase = togglePinUseCase,
                toggleFavoriteUseCase = toggleFavoriteUseCase
            ) as T
        }
    }
}

