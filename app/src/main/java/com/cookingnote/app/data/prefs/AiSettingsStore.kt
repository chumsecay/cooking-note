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
    val maxTokens: Int = 1500,
    val isCustom: Boolean = false
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
        val isCustom = prefs.getBoolean(KEY_IS_CUSTOM, false)
        if (!isCustom) {
            return AiSettings(
                provider = AiCloudDefaults.CLOUD_PROVIDER,
                baseUrl = AiCloudDefaults.CLOUD_BASE_URL,
                apiKey = AiCloudDefaults.CLOUD_API_KEY,
                model = AiCloudDefaults.CLOUD_MODEL,
                maxTokens = AiCloudDefaults.CLOUD_MAX_TOKENS,
                isCustom = false
            )
        }
        val defaultProvider = if (AiCloudDefaults.CLOUD_ENABLED_BY_DEFAULT) {
            AiCloudDefaults.CLOUD_PROVIDER.name
        } else {
            AiProviderType.RULE_BASED.name
        }
        val providerName = prefs.getString(KEY_PROVIDER, defaultProvider) ?: defaultProvider
        val provider = runCatching { AiProviderType.valueOf(providerName) }
            .getOrDefault(AiCloudDefaults.CLOUD_PROVIDER)
        return AiSettings(
            provider = provider,
            baseUrl = prefs.getString(KEY_BASE_URL, "").orEmpty()
                .ifBlank { AiCloudDefaults.baseUrlFor(provider) },
            apiKey = prefs.getString(KEY_API_KEY, "").orEmpty()
                .ifBlank { if (provider == AiCloudDefaults.CLOUD_PROVIDER) AiCloudDefaults.CLOUD_API_KEY else "" },
            model = prefs.getString(KEY_MODEL, "").orEmpty()
                .ifBlank { AiCloudDefaults.modelFor(provider) },
            maxTokens = prefs.getInt(KEY_MAX_TOKENS, AiCloudDefaults.CLOUD_MAX_TOKENS)
                .coerceIn(0, 32768),
            isCustom = true
        )
    }

    suspend fun update(transform: (AiSettings) -> AiSettings) = withContext(Dispatchers.IO) {
        val next = transform(_settings.value)
        prefs.edit()
            .putBoolean(KEY_IS_CUSTOM, next.isCustom)
            .putString(KEY_PROVIDER, next.provider.name)
            .putString(KEY_BASE_URL, next.baseUrl)
            .putString(KEY_API_KEY, next.apiKey)
            .putString(KEY_MODEL, next.model)
            .putInt(KEY_MAX_TOKENS, next.maxTokens)
            .apply()
        _settings.value = next
    }

    /**
     * Merge cấu hình từ xa vào store. Chỉ đụng provider/baseUrl/model/maxTokens,
     * KHÔNG bao giờ đụng apiKey. Trường nào remote thiếu thì giữ giá trị cũ.
     * [AiRemoteConfig.forceOffline] ép toàn bộ máy về offline (công tắc khẩn cấp).
     */
    suspend fun applyRemoteConfig(remote: AiRemoteConfig) {
        if (remote.forceOffline) {
            update { it.copy(provider = AiProviderType.RULE_BASED) }
            return
        }
        update { current ->
            val provider = remote.provider ?: current.provider
            current.copy(
                provider = provider,
                baseUrl = remote.baseUrl ?: current.baseUrl
                    .ifBlank { AiCloudDefaults.baseUrlFor(provider) },
                model = remote.model ?: current.model
                    .ifBlank { AiCloudDefaults.modelFor(provider) },
                maxTokens = remote.maxTokens ?: current.maxTokens
            )
        }
    }

    companion object {
        private const val KEY_IS_CUSTOM = "is_custom_endpoint"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_MAX_TOKENS = "max_tokens"
    }
}
