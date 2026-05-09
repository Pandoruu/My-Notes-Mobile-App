package com.example.mynotes.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mynotes.domain.model.ThemeMode
import com.example.mynotes.domain.usecase.settings.ObserveLanguageUseCase
import com.example.mynotes.domain.usecase.settings.ObserveThemeModeUseCase
import com.example.mynotes.domain.usecase.settings.SetLanguageUseCase
import com.example.mynotes.domain.usecase.settings.SetThemeModeUseCase
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    observeLanguageUseCase: ObserveLanguageUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setLanguageUseCase: SetLanguageUseCase
) : ViewModel() {

    val themeMode: LiveData<ThemeMode> = observeThemeModeUseCase().asLiveData()
    val language: LiveData<String> = observeLanguageUseCase().asLiveData()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { setLanguageUseCase(language) }
    }

    class Factory(
        private val observeThemeModeUseCase: ObserveThemeModeUseCase,
        private val observeLanguageUseCase: ObserveLanguageUseCase,
        private val setThemeModeUseCase: SetThemeModeUseCase,
        private val setLanguageUseCase: SetLanguageUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                observeThemeModeUseCase = observeThemeModeUseCase,
                observeLanguageUseCase = observeLanguageUseCase,
                setThemeModeUseCase = setThemeModeUseCase,
                setLanguageUseCase = setLanguageUseCase
            ) as T
        }
    }
}

