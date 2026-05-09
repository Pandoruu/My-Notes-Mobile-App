package com.example.mynotes.domain.usecase.settings

import com.example.mynotes.domain.repository.SettingsRepository

class SetLanguageUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(language: String) {
        settingsRepository.setLanguage(language)
    }
}

