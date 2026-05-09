package com.example.mynotes.domain.repository

import com.example.mynotes.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val language: Flow<String>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: String)
}

