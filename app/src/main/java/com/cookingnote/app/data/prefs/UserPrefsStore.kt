package com.cookingnote.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore by preferencesDataStore("user_prefs")

class UserPrefsStore(private val context: Context) {
    private object Keys {
        val darkMode = booleanPreferencesKey("dark_mode")
        val language = stringPreferencesKey("language")
        val onboardingDone = booleanPreferencesKey("onboarding_done")
    }

    val darkMode: Flow<Boolean?> = context.userDataStore.data.map { it[Keys.darkMode] }
    val language: Flow<String> = context.userDataStore.data.map { it[Keys.language] ?: "vi" }
    val onboardingDone: Flow<Boolean> =
        context.userDataStore.data.map { it[Keys.onboardingDone] ?: false }

    suspend fun setDarkMode(enabled: Boolean?) {
        context.userDataStore.edit { prefs ->
            if (enabled == null) prefs.remove(Keys.darkMode) else prefs[Keys.darkMode] = enabled
        }
    }

    suspend fun setLanguage(code: String) {
        context.userDataStore.edit { it[Keys.language] = code }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.userDataStore.edit { it[Keys.onboardingDone] = done }
    }
}
