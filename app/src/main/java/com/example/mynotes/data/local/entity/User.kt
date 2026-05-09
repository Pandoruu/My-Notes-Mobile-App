package com.example.mynotes.data.local.entity
import androidx.room.*

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "full_name") val fullName: String = "",
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "phone") val phone: String = ""
)