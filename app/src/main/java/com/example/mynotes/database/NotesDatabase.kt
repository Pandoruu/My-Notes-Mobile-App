package com.example.mynotes.database

import androidx.room.*
import com.example.mynotes.database.table.*
import com.example.mynotes.database.dao.*

@Database(
    entities = [User::class, Category::class, Note::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun noteDao(): NoteDao
}
