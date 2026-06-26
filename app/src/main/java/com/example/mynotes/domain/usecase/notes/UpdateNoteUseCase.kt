package com.example.mynotes.domain.usecase.notes

import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.NoteRepository
import com.example.mynotes.domain.util.TextNormalizer
import java.util.Date

class UpdateNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        val updated = note.copy(
            titlePlain = TextNormalizer.removeAccents(note.title),
            detailPlain = note.detail?.let { TextNormalizer.removeAccents(it) },
            updatedAt = Date()
            // contentBlocks được giữ nguyên từ note.copy()
        )
        noteRepository.updateNote(updated)
    }
}
