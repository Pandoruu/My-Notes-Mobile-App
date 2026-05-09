package com.example.mynotes.domain.usecase.auth

import com.example.mynotes.domain.model.User
import com.example.mynotes.domain.repository.SessionRepository
import com.example.mynotes.domain.repository.UserRepository
import com.example.mynotes.domain.usecase.user.EnsureUserDataUseCase

class LoginUseCase(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val ensureUserDataUseCase: EnsureUserDataUseCase
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        val user = userRepository.getUserByUsernameAndPassword(username, password)
            ?: return Result.failure(IllegalArgumentException("Invalid credentials"))
        sessionRepository.setCurrentUserId(user.id)
        ensureUserDataUseCase(user.id)
        return Result.success(user)
    }
}

