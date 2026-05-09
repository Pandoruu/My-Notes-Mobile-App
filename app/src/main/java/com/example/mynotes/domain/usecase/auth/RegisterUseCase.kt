package com.example.mynotes.domain.usecase.auth

import com.example.mynotes.domain.model.User
import com.example.mynotes.domain.repository.UserRepository

class RegisterUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        phone: String,
        username: String,
        password: String
    ): Result<User> {
        if (fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Full name required"))
        }
        if (email.isBlank() || !isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email"))
        }
        if (!isValidPhone(phone)) {
            return Result.failure(IllegalArgumentException("Invalid phone"))
        }
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password required"))
        }
        val existing = userRepository.getUserByUsername(username)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username already exists"))
        }
        val id = userRepository.addUser(
            User(
                username = username,
                password = password,
                fullName = fullName,
                email = email,
                phone = phone
            )
        ).toInt()
        val user = User(
            id = id,
            username = username,
            password = password,
            fullName = fullName,
            email = email,
            phone = phone
        )
        return Result.success(user)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return emailRegex.matches(email)
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.all { it.isDigit() } && phone.length in 8..15
    }
}
