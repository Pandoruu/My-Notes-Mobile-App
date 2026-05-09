package com.example.mynotes.domain.usecase.notes

import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.NoteRepository
import com.example.mynotes.domain.util.TextNormalizer
import java.util.Date

class AddNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(userId: Int, categoryId: Int?, title: String, detail: String?): Long {
        val now = Date()
        val note = Note(
            userId = userId,
            categoryId = categoryId,
            title = title,
            detail = detail,
            titlePlain = TextNormalizer.removeAccents(title),
            detailPlain = detail?.let { TextNormalizer.removeAccents(it) },
            createdAt = now,
            updatedAt = now
        )
        return noteRepository.addNote(note)
    }
}

