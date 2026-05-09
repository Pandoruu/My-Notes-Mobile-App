package com.example.mynotes.domain.usecase.notes

import androidx.lifecycle.LiveData
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.NoteRepository

class SearchNotesUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(userId: Int, query: String): LiveData<List<Note>> =
        noteRepository.observeSearchNotes(userId, query)
}

