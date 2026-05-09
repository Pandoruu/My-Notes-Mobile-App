package com.example.mynotes.presentation.trash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.example.mynotes.domain.usecase.notes.DeleteNoteUseCase
import com.example.mynotes.domain.usecase.notes.ObserveTrashedNotesUseCase
import com.example.mynotes.domain.usecase.notes.RestoreNoteUseCase
import kotlinx.coroutines.launch

class TrashViewModel(
    observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
    private val observeTrashedNotesUseCase: ObserveTrashedNotesUseCase,
    private val restoreNoteUseCase: RestoreNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val currentUserIdLiveData = observeCurrentUserIdUseCase().asLiveData()

    fun observeTrashedNotes(): LiveData<List<Note>> =
        currentUserIdLiveData.switchMap { userId ->
            if (userId == null) MutableLiveData(emptyList()) else observeTrashedNotesUseCase(userId)
        }

    fun restoreNote(note: Note) {
        viewModelScope.launch { restoreNoteUseCase(note) }
    }

    fun deleteNotePermanently(note: Note) {
        viewModelScope.launch { deleteNoteUseCase(note) }
    }

    class Factory(
        private val observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
        private val observeTrashedNotesUseCase: ObserveTrashedNotesUseCase,
        private val restoreNoteUseCase: RestoreNoteUseCase,
        private val deleteNoteUseCase: DeleteNoteUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrashViewModel(
                observeCurrentUserIdUseCase = observeCurrentUserIdUseCase,
                observeTrashedNotesUseCase = observeTrashedNotesUseCase,
                restoreNoteUseCase = restoreNoteUseCase,
                deleteNoteUseCase = deleteNoteUseCase
            ) as T
        }
    }
}

