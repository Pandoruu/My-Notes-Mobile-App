package com.example.mynotes.database.repo

import androidx.lifecycle.LiveData
import com.example.mynotes.database.dao.CategoryDao
import com.example.mynotes.database.dao.NoteDao
import com.example.mynotes.database.dao.UserDao
import com.example.mynotes.database.table.Category
import com.example.mynotes.database.table.Note
import com.example.mynotes.database.table.User

class NotesRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val noteDao: NoteDao
) {
    // User
    suspend fun addUser(user: User) = userDao.insert(user)
    suspend fun updateUser(user: User) = userDao.update(user)
    suspend fun deleteUser(user: User) = userDao.delete(user)

    fun observeUserById(id: Int): LiveData<User?> = userDao.observeUserById(id)
    fun observeAllUsers(): LiveData<List<User>> = userDao.observeAllUsers()

    // Category
    suspend fun addCategory(category: Category) = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    fun observeCategoriesByUser(userId: Int): LiveData<List<Category>> =
        categoryDao.observeByUser(userId)

    // Note
    suspend fun addNote(note: Note) = noteDao.insert(note)
    suspend fun updateNote(note: Note) = noteDao.update(note)
    suspend fun deleteNote(note: Note) = noteDao.delete(note)

    fun observeActiveNotes(userId: Int): LiveData<List<Note>> =
        noteDao.observeActiveNotes(userId)

    fun observeTrashedNotes(userId: Int): LiveData<List<Note>> =
        noteDao.observeTrashedNotes(userId)

    fun observeSearchNotes(userId: Int, query: String): LiveData<List<Note>> =
        noteDao.observeSearchNotes(userId, query)

    fun observeNoteById(noteId: Int): LiveData<Note?> =
        noteDao.observeNoteById(noteId)

    fun observeAllNotes(userId: Int): LiveData<List<Note>> = noteDao.observeAllNotes(userId)

    fun observeNotesByCategory(userId: Int, categoryName: String): LiveData<List<Note>> =
        noteDao.observeNotesByCategory(userId, categoryName)
//    fun observeFavoriteNotes(userId: Int) =
//        noteDao.observeFavoriteNotes(userId)
}