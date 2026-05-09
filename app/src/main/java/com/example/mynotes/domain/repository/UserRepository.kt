package com.example.mynotes.domain.repository

import androidx.lifecycle.LiveData
import com.example.mynotes.domain.model.User

interface UserRepository {
    suspend fun addUser(user: User): Long
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)

    fun observeUserById(id: Int): LiveData<User?>
    fun observeAllUsers(): LiveData<List<User>>

    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByUsernameAndPassword(username: String, password: String): User?
    suspend fun getUserById(id: Int): User?
    suspend fun getAnyUser(): User?
}
