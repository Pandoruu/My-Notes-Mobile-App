package com.example.mynotes.data.local

import androidx.room.*
import com.example.mynotes.data.local.dao.CategoryDao
import com.example.mynotes.data.local.dao.NoteDao
import com.example.mynotes.data.local.dao.UserDao
import com.example.mynotes.data.local.entity.Category
import com.example.mynotes.data.local.entity.Note
import com.example.mynotes.data.local.entity.User

@Database(
    entities = [User::class, Category::class, Note::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun noteDao(): NoteDao
}
