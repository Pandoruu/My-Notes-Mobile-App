package com.example.mynotes.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynotes.data.local.entity.User

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

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUserByUsernameAndPasswordOnce(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdOnce(id: Int): User?

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    suspend fun getAnyUserOnce(): User?
}
