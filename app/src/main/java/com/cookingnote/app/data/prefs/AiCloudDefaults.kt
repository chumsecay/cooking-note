package com.cookingnote.app.data.prefs

/**
 * Điểm cấu hình sẵn DUY NHẤT cho AI đám mây trước khi phát hành.
 *
 * Cách dùng:
 * 1. Trước khi build bản release: sửa [CLOUD_PROVIDER], [CLOUD_BASE_URL],
 *    [CLOUD_MODEL], [CLOUD_MAX_TOKENS] cho đúng máy chủ/provider bạn chọn.
 *    Có thể trỏ về proxy riêng (khuyến nghị nếu muốn giấu API key phía server)
 *    hoặc endpoint chính hãng (lúc này mỗi máy phải nhập API key trong Settings).
 * 2. Muốn đổi lại từ xa sau khi app đã cài: điền [REMOTE_CONFIG_URL] trỏ tới
 *    file JSON do bạn quản lý (xem định dạng trong [AiRemoteConfig]) và bật
 *    [REMOTE_ENABLED]. App tải JSON lúc khởi động, merge đè giá trị cấu hình
 *    sẵn; mất mạng thì giữ nguyên cấu hình sẵn (offline-first).
 *
 * Nguyên tắc bảo mật: KHÔNG bao giờ điền API key vào đây hay vào JSON remote.
 * Key chỉ nằm trong [AiSettingsStore] (EncryptedSharedPreferences), do người
 * dùng nhập trong Settings hoặc do proxy server của bạn giữ.
 */
object AiCloudDefaults {
    /** Provider cloud mặc định khi người dùng bật công tắc AI đám mây. */
    val CLOUD_PROVIDER: AiProviderType = AiProviderType.GEMINI

    /** Base URL cloud cấu hình sẵn (kể cả proxy riêng của bạn). */
    const val CLOUD_BASE_URL = "https://generativelanguage.googleapis.com/"

    /** Model cloud cấu hình sẵn. */
    const val CLOUD_MODEL = "gemini-1.5-flash"

    /** maxTokens cấu hình sẵn. */
    const val CLOUD_MAX_TOKENS = 1500

    /** Cloud tắt mặc định: app mới cài chạy offline cho tới khi bật + có key. */
    const val CLOUD_ENABLED_BY_DEFAULT = false

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
