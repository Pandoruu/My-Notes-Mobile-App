package com.example.mynotes.presentation.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.example.mynotes.domain.model.User
import com.example.mynotes.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.example.mynotes.domain.usecase.notes.ObserveAllNotesUseCase
import com.example.mynotes.domain.usecase.user.ObserveUserByIdUseCase

class AccountViewModel(
    observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
    private val observeUserByIdUseCase: ObserveUserByIdUseCase,
    private val observeAllNotesUseCase: ObserveAllNotesUseCase
) : ViewModel() {

    private val currentUserId = observeCurrentUserIdUseCase().asLiveData()

    val user: LiveData<User?> = currentUserId.switchMap { userId ->
        if (userId == null) {
            liveData { emit(null) }
        } else {
            observeUserByIdUseCase(userId)
        }
    }

    val noteCount: LiveData<Int> = currentUserId.switchMap { userId ->
        if (userId == null) {
            liveData { emit(0) }
        } else {
            observeAllNotesUseCase(userId).map { it.size }
        }
    }

    class Factory(
        private val observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
        private val observeUserByIdUseCase: ObserveUserByIdUseCase,
        private val observeAllNotesUseCase: ObserveAllNotesUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountViewModel(
                observeCurrentUserIdUseCase = observeCurrentUserIdUseCase,
                observeUserByIdUseCase = observeUserByIdUseCase,
                observeAllNotesUseCase = observeAllNotesUseCase
            ) as T
        }
    }
}
