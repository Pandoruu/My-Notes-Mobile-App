package com.example.mynotes.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynotes.database.table.Note

@Dao
interface NoteDao {
    @Insert suspend fun insert(note: Note): Long
    @Update suspend fun update(note: Note)
    @Delete suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_trashed = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun observeActiveNotes(userId: Int): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_trashed = 1 ORDER BY trashed_at DESC")
    fun observeTrashedNotes(userId: Int): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE (title_plain LIKE '%' || :query || '%' OR detail_plain LIKE '%' || :query || '%') AND user_id = :userId AND is_trashed = 0")
    fun observeSearchNotes(userId: Int, query: String): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    fun observeNoteById(noteId: Int): LiveData<Note?>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_trashed = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun observeAllNotes(userId: Int): LiveData<List<Note>>

    @Query("""
        SELECT n.* FROM notes n
        INNER JOIN categories c ON n.category_id = c.id
        WHERE n.user_id = :userId AND c.name = :categoryName AND n.is_trashed = 0
        ORDER BY n.is_pinned DESC, n.updated_at DESC
    """)
    fun observeNotesByCategory(userId: Int, categoryName: String): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE user_id = :userId")
    suspend fun getAllNotesOnce(userId: Int): List<Note>

//    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_favorite = 1 AND is_trashed = 0 ORDER BY updated_at DESC")
//    fun observeFavoriteNotes(userId: Int): LiveData<List<Note>>

}
