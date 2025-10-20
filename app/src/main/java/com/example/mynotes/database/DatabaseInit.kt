package com.example.mynotes.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseInit {
    @Volatile
    private var INSTANCE: NotesDatabase? = null
    fun getDatabase(context: Context?): NotesDatabase {
        val tmp = INSTANCE
        if (tmp != null) return tmp
        synchronized(this) {
            val again = INSTANCE
            if (again != null) return again

            val instance = Room.databaseBuilder(
                context!!.applicationContext,
                NotesDatabase::class.java,
                "notes_database"
            ).build()

            INSTANCE = instance
            return instance
        }

    }
}