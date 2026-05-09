package com.example.mynotes.domain.usecase.notes

import androidx.lifecycle.LiveData
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.NoteRepository

class ObserveNoteByIdUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(noteId: Int): LiveData<Note?> =
        noteRepository.observeNoteById(noteId)
}

