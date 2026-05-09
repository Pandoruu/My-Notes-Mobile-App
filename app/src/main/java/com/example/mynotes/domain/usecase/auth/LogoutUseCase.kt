package com.example.mynotes.domain.usecase.auth

import com.example.mynotes.domain.repository.SessionRepository

class LogoutUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        sessionRepository.clearCurrentUserId()
    }
}

