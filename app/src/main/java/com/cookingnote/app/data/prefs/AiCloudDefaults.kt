package com.cookingnote.app.data.prefs

import com.cookingnote.app.BuildConfig

/**
 * Điểm cấu hình sẵn DUY NHẤT cho AI đám mây trước khi phát hành.
 * Các giá trị mặc định được tự động nạp từ file .env qua BuildConfig khi build Gradle.
 *
 * Cách dùng:
 * 1. Cấu hình file .env ở thư mục gốc (xem .env.example):
 *    AI_PROVIDER, AI_BASE_URL, AI_MODEL, AI_API_KEY, AI_MAX_TOKENS.
 *    Có thể trỏ về proxy riêng của backend v2 hoặc endpoint chính hãng.
 * 2. Muốn đổi lại từ xa sau khi app đã cài: điền [REMOTE_CONFIG_URL] trỏ tới
 *    file JSON do bạn quản lý (xem định dạng trong [AiRemoteConfig]) và bật
 *    [REMOTE_ENABLED]. App tải JSON lúc khởi động, merge đè giá trị cấu hình
 *    sẵn; mất mạng thì giữ nguyên cấu hình sẵn (offline-first).
 */
object AiCloudDefaults {
    /** Provider cloud mặc định nạp từ .env / BuildConfig. */
    val CLOUD_PROVIDER: AiProviderType = runCatching {
        AiProviderType.valueOf(BuildConfig.AI_PROVIDER)
    }.getOrDefault(AiProviderType.OPENAI_CHAT)

    /** Base URL cloud cấu hình sẵn nạp từ .env / BuildConfig. */
    val CLOUD_BASE_URL: String = runCatching { BuildConfig.AI_BASE_URL }
        .getOrDefault("https://api.openai.com/v1/")
        .ifBlank { "https://api.openai.com/v1/" }

    /** Model cloud cấu hình sẵn nạp từ .env / BuildConfig. */
    val CLOUD_MODEL: String = runCatching { BuildConfig.AI_MODEL }
        .getOrDefault("gpt-4o-mini")
        .ifBlank { "gpt-4o-mini" }

    /** maxTokens cấu hình sẵn nạp từ .env / BuildConfig (0 = không giới hạn, để model tự bung tối đa). */
    val CLOUD_MAX_TOKENS: Int = runCatching { BuildConfig.AI_MAX_TOKENS }
        .getOrDefault(0)
        .coerceIn(0, 32768)

    /** API Key mặc định từ .env / BuildConfig (nếu có). */
    val CLOUD_API_KEY: String = runCatching { BuildConfig.AI_API_KEY }
        .getOrDefault("")

    /** Cloud bật mặc định khi cài đặt. */
    const val CLOUD_ENABLED_BY_DEFAULT = true

    /**
     * URL file JSON cấu hình từ xa, ví dụ "https://example.com/ai-config.json".
     * Để trống = tắt hẳn việc tải từ xa.
     */
    const val REMOTE_CONFIG_URL = ""

    /** Bật/tắt việc tải JSON từ xa lúc khởi động app. */
    const val REMOTE_ENABLED = false

    /**
     * Base URL đi kèm một [provider]: ưu tiên giá trị cấu hình sẵn khi
     * [provider] trùng [CLOUD_PROVIDER], còn lại dùng default của enum.
     */
    fun baseUrlFor(provider: AiProviderType): String =
        if (provider == CLOUD_PROVIDER && CLOUD_BASE_URL.isNotBlank()) {
            CLOUD_BASE_URL
        } else {
            provider.defaultBaseUrl
        }

    /** Model đi kèm một [provider]: cùng quy tắc như [baseUrlFor]. */
    fun modelFor(provider: AiProviderType): String =
        if (provider == CLOUD_PROVIDER && CLOUD_MODEL.isNotBlank()) {
            CLOUD_MODEL
        } else {
            provider.defaultModel
        }
}
