package com.example.mynotes.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mynotes.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_store")

class SessionDataStore(
    private val context: Context
) : SessionRepository {
    private val currentUserIdKey = intPreferencesKey("current_user_id")

    override val currentUserId: Flow<Int?>
        get() = context.sessionDataStore.data.map { prefs -> prefs[currentUserIdKey] }

    override suspend fun setCurrentUserId(userId: Int) {
        context.sessionDataStore.edit { prefs -> prefs[currentUserIdKey] = userId }
    }

    override suspend fun clearCurrentUserId() {
        context.sessionDataStore.edit { prefs -> prefs.remove(currentUserIdKey) }
    }
}

