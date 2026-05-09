package com.example.mynotes.domain.usecase.settings

import com.example.mynotes.domain.model.ThemeMode
import com.example.mynotes.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<ThemeMode> = settingsRepository.themeMode
}

