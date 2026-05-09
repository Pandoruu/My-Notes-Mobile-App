package com.example.mynotes.domain.usecase.user

import androidx.lifecycle.LiveData
import com.example.mynotes.domain.model.User
import com.example.mynotes.domain.repository.UserRepository

class ObserveUserByIdUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: Int): LiveData<User?> = userRepository.observeUserById(userId)
}

