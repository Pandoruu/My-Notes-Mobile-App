package com.example.mynotes.domain.usecase.settings

import com.example.mynotes.domain.model.ThemeMode
import com.example.mynotes.domain.repository.SettingsRepository

class SetThemeModeUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
    }
}

