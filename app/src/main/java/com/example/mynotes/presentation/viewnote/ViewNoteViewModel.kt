package com.example.mynotes.presentation.viewnote

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
import com.example.mynotes.domain.usecase.notes.AddNoteUseCase
import com.example.mynotes.domain.usecase.notes.MoveNoteToTrashUseCase
import com.example.mynotes.domain.usecase.notes.ObserveNoteByIdUseCase
import com.example.mynotes.domain.usecase.notes.UpdateNoteUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ViewNoteViewModel(
    observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
    private val observeNoteByIdUseCase: ObserveNoteByIdUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val moveNoteToTrashUseCase: MoveNoteToTrashUseCase
) : ViewModel() {

    private val currentUserIdFlow = observeCurrentUserIdUseCase()
    val currentUserId: LiveData<Int?> = currentUserIdFlow.asLiveData()

    fun observeNoteById(noteId: Int): LiveData<Note?> = observeNoteByIdUseCase(noteId)

    fun observeCategories(): LiveData<List<Category>> =
        currentUserId.switchMap { userId ->
            if (userId == null) MutableLiveData(emptyList()) else observeCategoriesUseCase(userId)
        }

    fun addNote(categoryId: Int?, title: String, detail: String?, contentBlocks: List<com.example.mynotes.domain.model.NoteBlock> = emptyList()) {
        viewModelScope.launch {
            val userId = currentUserIdFlow.first() ?: return@launch
            addNoteUseCase(userId, categoryId, title, detail, contentBlocks)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { updateNoteUseCase(note) }
    }

    fun moveNoteToTrash(note: Note) {
        viewModelScope.launch { moveNoteToTrashUseCase(note) }
    }

    class Factory(
        private val observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
        private val observeNoteByIdUseCase: ObserveNoteByIdUseCase,
        private val observeCategoriesUseCase: ObserveCategoriesUseCase,
        private val addNoteUseCase: AddNoteUseCase,
        private val updateNoteUseCase: UpdateNoteUseCase,
        private val moveNoteToTrashUseCase: MoveNoteToTrashUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewNoteViewModel(
                observeCurrentUserIdUseCase = observeCurrentUserIdUseCase,
                observeNoteByIdUseCase = observeNoteByIdUseCase,
                observeCategoriesUseCase = observeCategoriesUseCase,
                addNoteUseCase = addNoteUseCase,
                updateNoteUseCase = updateNoteUseCase,
                moveNoteToTrashUseCase = moveNoteToTrashUseCase
            ) as T
        }
    }
}

