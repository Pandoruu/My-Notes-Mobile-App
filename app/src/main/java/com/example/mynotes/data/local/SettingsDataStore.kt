package com.example.mynotes.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mynotes.domain.model.ThemeMode
import com.example.mynotes.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_store")

class SettingsDataStore(
    private val context: Context
) : SettingsRepository {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("language")

    override val themeMode: Flow<ThemeMode>
        get() = context.settingsDataStore.data.map { prefs ->
            val stored = prefs[themeModeKey] ?: ThemeMode.LIGHT.name
            ThemeMode.valueOf(stored)
        }

    override val language: Flow<String>
        get() = context.settingsDataStore.data.map { prefs ->
            prefs[languageKey] ?: "system"
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    override suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[languageKey] = language
        }
    }
}

