package com.example.mynotes.domain.usecase.settings

import com.example.mynotes.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveLanguageUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<String> = settingsRepository.language
}

