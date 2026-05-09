package com.example.mynotes.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val currentUserId: Flow<Int?>
    suspend fun setCurrentUserId(userId: Int)
    suspend fun clearCurrentUserId()
}

