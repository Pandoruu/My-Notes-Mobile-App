package com.example.mynotes.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.mynotes.data.local.dao.NoteDao
import com.example.mynotes.data.mapper.toDomain
import com.example.mynotes.data.mapper.toEntity
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.NoteRepository as DomainNoteRepository

class NotesRepository(
    private val noteDao: NoteDao
) : DomainNoteRepository {

    // Note
    override suspend fun addNote(note: Note): Long = noteDao.insert(note.toEntity())
    override suspend fun updateNote(note: Note) = noteDao.update(note.toEntity())
    override suspend fun deleteNote(note: Note) = noteDao.delete(note.toEntity())

    override fun observeActiveNotes(userId: Int): LiveData<List<Note>> =
        noteDao.observeActiveNotes(userId).map { list -> list.map { it.toDomain() } }

    override fun observeTrashedNotes(userId: Int): LiveData<List<Note>> =
        noteDao.observeTrashedNotes(userId).map { list -> list.map { it.toDomain() } }

    override fun observeSearchNotes(userId: Int, query: String): LiveData<List<Note>> =
        noteDao.observeSearchNotes(userId, query).map { list -> list.map { it.toDomain() } }

    override fun observeNoteById(noteId: Int): LiveData<Note?> =
        noteDao.observeNoteById(noteId).map { it?.toDomain() }

    override fun observeAllNotes(userId: Int): LiveData<List<Note>> =
        noteDao.observeAllNotes(userId).map { list -> list.map { it.toDomain() } }

    override fun observeNotesByCategory(userId: Int, categoryId: Int): LiveData<List<Note>> =
        noteDao.observeNotesByCategory(userId, categoryId).map { list -> list.map { it.toDomain() } }

    override fun observeFavoriteNotes(userId: Int): LiveData<List<Note>> =
        noteDao.observeFavoriteNotes(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getAllNotesOnce(userId: Int): List<Note> =
        noteDao.getAllNotesOnce(userId).map { it.toDomain() }
}