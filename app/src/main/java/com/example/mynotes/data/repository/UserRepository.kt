package com.example.mynotes.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.mynotes.data.local.dao.UserDao
import com.example.mynotes.data.mapper.toDomain
import com.example.mynotes.data.mapper.toEntity
import com.example.mynotes.domain.model.User
import com.example.mynotes.domain.repository.UserRepository as DomainUserRepository

class UserRepository(
    private val userDao: UserDao
) : DomainUserRepository {
    // User
    override suspend fun addUser(user: User): Long = userDao.insert(user.toEntity())
    override suspend fun updateUser(user: User) = userDao.update(user.toEntity())
    override suspend fun deleteUser(user: User) = userDao.delete(user.toEntity())

    override fun observeUserById(id: Int): LiveData<User?> =
        userDao.observeUserById(id).map { it?.toDomain() }

    override fun observeAllUsers(): LiveData<List<User>> =
        userDao.observeAllUsers().map { list -> list.map { it.toDomain() } }

    override suspend fun getUserByUsername(username: String): User? =
        userDao.getUserByUsernameOnce(username)?.toDomain()

    override suspend fun getUserByUsernameAndPassword(username: String, password: String): User? =
        userDao.getUserByUsernameAndPasswordOnce(username, password)?.toDomain()

    override suspend fun getUserById(id: Int): User? =
        userDao.getUserByIdOnce(id)?.toDomain()

    override suspend fun getAnyUser(): User? = userDao.getAnyUserOnce()?.toDomain()
}