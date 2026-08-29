package com.cookingnote.app.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class AiProviderType(val label: String, val defaultBaseUrl: String, val defaultModel: String) {
    OPENAI_COMPATIBLE("OpenAI Compatible", "https://api.openai.com/v1/", "gpt-4o-mini"),
    OPENAI("OpenAI", "https://api.openai.com/v1/", "gpt-4o-mini"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1/", "openai/gpt-4o-mini"),
    GEMINI("Gemini", "https://generativelanguage.googleapis.com/", "gemini-1.5-flash"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/", "claude-3-5-haiku-latest"),
    RULE_BASED("Rule-based (offline)", "", "")
}

data class AiSettings(
    val provider: AiProviderType = AiProviderType.RULE_BASED,
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val temperature: Float = 0.7f
)

class AiSettingsStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            "ai_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AiSettings> = _settings.asStateFlow()

    private fun load(): AiSettings {
        val providerName = prefs.getString(KEY_PROVIDER, AiProviderType.RULE_BASED.name)
            ?: AiProviderType.RULE_BASED.name
        val provider = runCatching { AiProviderType.valueOf(providerName) }
            .getOrDefault(AiProviderType.RULE_BASED)
        return AiSettings(
            provider = provider,
            baseUrl = prefs.getString(KEY_BASE_URL, provider.defaultBaseUrl).orEmpty(),
            apiKey = prefs.getString(KEY_API_KEY, "").orEmpty(),
            model = prefs.getString(KEY_MODEL, provider.defaultModel).orEmpty(),
            temperature = prefs.getFloat(KEY_TEMP, 0.7f)
        )
    }

    suspend fun update(transform: (AiSettings) -> AiSettings) = withContext(Dispatchers.IO) {
        val next = transform(_settings.value)
        prefs.edit()
            .putString(KEY_PROVIDER, next.provider.name)
            .putString(KEY_BASE_URL, next.baseUrl)
            .putString(KEY_API_KEY, next.apiKey)
            .putString(KEY_MODEL, next.model)
            .putFloat(KEY_TEMP, next.temperature)
            .apply()
        _settings.value = next
    }

    companion object {
        private const val KEY_PROVIDER = "provider"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_TEMP = "temperature"
    }
}
