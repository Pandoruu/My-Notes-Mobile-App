package com.example.mynotes.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mynotes.domain.model.User
import com.example.mynotes.domain.usecase.auth.LoginUseCase
import com.example.mynotes.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.example.mynotes.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    val currentUserId: LiveData<Int?> = observeCurrentUserIdUseCase().asLiveData()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _registerResult = MutableLiveData<User?>()
    val registerResult: LiveData<User?> = _registerResult

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = loginUseCase(username.trim(), password)
            _errorMessage.value = result.exceptionOrNull()?.message
            _loginResult.value = result.getOrNull()
        }
    }

    fun register(fullName: String, email: String, phone: String, username: String, password: String) {
        viewModelScope.launch {
            val result = registerUseCase(fullName, email, phone, username.trim(), password)
            _errorMessage.value = result.exceptionOrNull()?.message
            _registerResult.value = result.getOrNull()
        }
    }

    fun clearRegisterResult() {
        _registerResult.value = null
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }

    class Factory(
        private val observeCurrentUserIdUseCase: ObserveCurrentUserIdUseCase,
        private val loginUseCase: LoginUseCase,
        private val registerUseCase: RegisterUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(
                observeCurrentUserIdUseCase = observeCurrentUserIdUseCase,
                loginUseCase = loginUseCase,
                registerUseCase = registerUseCase
            ) as T
        }
    }
}
