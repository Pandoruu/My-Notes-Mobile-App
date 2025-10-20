package com.example.mynotes.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynotes.database.table.User

@Dao
interface UserDao {
    @Insert suspend fun insert(user: User): Long
    @Update suspend fun update(user: User)
    @Delete suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUserById(id: Int): LiveData<User?>

    @Query("SELECT * FROM users")
    fun observeAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsernameOnce(username: String): User?

}
