package com.example.mynotes.domain.usecase.notes

import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.NoteRepository
import java.util.Date

class MoveNoteToTrashUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        noteRepository.updateNote(note.copy(isTrashed = true, trashedAt = Date()))
    }
}

