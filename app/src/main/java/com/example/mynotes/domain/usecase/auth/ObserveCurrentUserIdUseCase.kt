package com.example.mynotes.domain.usecase.auth

import com.example.mynotes.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserIdUseCase(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<Int?> = sessionRepository.currentUserId
}

