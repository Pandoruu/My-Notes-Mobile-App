package com.example.mynotes.domain.repository

import androidx.lifecycle.LiveData
import com.example.mynotes.domain.model.Note

interface NoteRepository {
    suspend fun addNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)

    fun observeActiveNotes(userId: Int): LiveData<List<Note>>
    fun observeTrashedNotes(userId: Int): LiveData<List<Note>>
    fun observeSearchNotes(userId: Int, query: String): LiveData<List<Note>>
    fun observeNoteById(noteId: Int): LiveData<Note?>
    fun observeAllNotes(userId: Int): LiveData<List<Note>>
    fun observeNotesByCategory(userId: Int, categoryId: Int): LiveData<List<Note>>
    fun observeFavoriteNotes(userId: Int): LiveData<List<Note>>

    suspend fun getAllNotesOnce(userId: Int): List<Note>
}

