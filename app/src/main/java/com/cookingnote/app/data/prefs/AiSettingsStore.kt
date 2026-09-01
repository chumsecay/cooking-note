package com.cookingnote.app.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class AiProviderType(
    val label: String,
    val endpointPath: String?,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val requiresApiKey: Boolean
) {
    RULE_BASED("Rule-based (offline)", null, "", "", false),

    OPENAI_CHAT(
        "OpenAI Chat Completions (/v1/chat/completions)",
        "/chat/completions",
        "https://api.openai.com/v1/",
        "gpt-4o-mini",
        true
    ),

    OPENAI_RESPONSES(
        "OpenAI Responses (/v1/responses)",
        "/responses",
        "https://api.openai.com/v1/",
        "gpt-4o-mini",
        true
    ),

    ANTHROPIC(
        "Anthropic Messages (/v1/messages)",
        "/v1/messages",
        "https://api.anthropic.com/",
        "claude-3-5-haiku-latest",
        true
    ),

    GEMINI(
        "Gemini Generate Content",
        "/v1beta/models/{model}:generateContent",
        "https://generativelanguage.googleapis.com/",
        "gemini-1.5-flash",
        true
    )
}

data class AiSettings(
    val provider: AiProviderType = AiProviderType.RULE_BASED,
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val maxTokens: Int = 1500
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
            baseUrl = prefs.getString(KEY_BASE_URL, "").orEmpty()
                .ifBlank { provider.defaultBaseUrl },
            apiKey = prefs.getString(KEY_API_KEY, "").orEmpty(),
            model = prefs.getString(KEY_MODEL, "").orEmpty()
                .ifBlank { provider.defaultModel },
            maxTokens = prefs.getInt(KEY_MAX_TOKENS, 1500).coerceIn(256, 8192)
        )
    }

    suspend fun update(transform: (AiSettings) -> AiSettings) = withContext(Dispatchers.IO) {
        val next = transform(_settings.value)
        prefs.edit()
            .putString(KEY_PROVIDER, next.provider.name)
            .putString(KEY_BASE_URL, next.baseUrl)
            .putString(KEY_API_KEY, next.apiKey)
            .putString(KEY_MODEL, next.model)
            .putInt(KEY_MAX_TOKENS, next.maxTokens)
            .apply()
        _settings.value = next
    }

    companion object {
        private const val KEY_PROVIDER = "provider"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_MAX_TOKENS = "max_tokens"
    }
}
