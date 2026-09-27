package com.cookingnote.app.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class UserSession(
    val isLoggedIn: Boolean = false,
    val token: String? = null,
    val refreshToken: String? = null,
    val userId: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val role: String? = null
)

class UserSessionStore(context: Context) {
    private val appContext = context.applicationContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            "user_session_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<UserSession> = _session.asStateFlow()

    private fun loadSession(): UserSession {
        val token = prefs.getString(KEY_TOKEN, null)
        val isLoggedIn = !token.isNullOrBlank()
        return UserSession(
            isLoggedIn = isLoggedIn,
            token = token,
            refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null),
            userId = prefs.getString(KEY_USER_ID, null),
            email = prefs.getString(KEY_EMAIL, null),
            fullName = prefs.getString(KEY_FULL_NAME, null),
            role = prefs.getString(KEY_ROLE, null)
        )
    }

    suspend fun saveSession(
        token: String,
        refreshToken: String?,
        userId: String?,
        email: String?,
        fullName: String?,
        role: String?
    ) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_ROLE, role)
            .apply()
        _session.value = UserSession(
            isLoggedIn = true,
            token = token,
            refreshToken = refreshToken,
            userId = userId,
            email = email,
            fullName = fullName,
            role = role
        )
    }

    suspend fun clearSession() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
        _session.value = UserSession()
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_FULL_NAME = "user_full_name"
        private const val KEY_ROLE = "user_role"
    }
}
