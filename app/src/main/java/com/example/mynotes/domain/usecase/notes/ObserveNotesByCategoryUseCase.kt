package com.example.mynotes.domain.usecase.notes

import androidx.lifecycle.LiveData
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.NoteRepository

class ObserveNotesByCategoryUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(userId: Int, categoryId: Int): LiveData<List<Note>> =
        noteRepository.observeNotesByCategory(userId, categoryId)
}

