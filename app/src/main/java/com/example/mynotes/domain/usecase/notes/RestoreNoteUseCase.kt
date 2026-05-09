package com.example.mynotes.domain.usecase.notes

import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.NoteRepository

class RestoreNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        noteRepository.updateNote(note.copy(isTrashed = false, trashedAt = null))
    }
}

