package com.cookingnote.app.data.prefs

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private const val TAG = "AiRemoteConfig"

/**
 * Cấu hình AI đám mây tải từ xa (JSON do bạn host, ví dụ trên server riêng
 * hoặc file tĩnh). Mọi trường đều tùy chọn: thiếu trường nào thì giữ giá trị
 * cấu hình sẵn trong [AiCloudDefaults] (hoặc giá trị người dùng đã lưu).
 *
 * Định dạng JSON:
 * ```
 * {
 *   "provider": "GEMINI",
 *   "baseUrl": "https://your-proxy.example/v1/",
 *   "model": "gemini-1.5-flash",
 *   "maxTokens": 1500,
 *   "cloudEnabledByDefault": false,
 *   "forceOffline": false
 * }
 * ```
 * - "provider" phải trùng tên enum [AiProviderType], sai thì bỏ qua.
 * - "baseUrl" chỉ nhận https (hoặc http localhost để dev), sai thì bỏ qua.
 * - "forceOffline": true ép toàn bộ máy về offline (công tắc khẩn cấp).
 * - TUYỆT ĐỐI không đặt API key vào JSON này.
 */
data class AiRemoteConfig(
    val provider: AiProviderType? = null,
    val baseUrl: String? = null,
    val model: String? = null,
    val maxTokens: Int? = null,
    val cloudEnabledByDefault: Boolean? = null,
    val forceOffline: Boolean = false
) {
    companion object {
        /** Parse thủ công bằng Kotlin thuần để tương thích unit test JVM. */
        fun parse(json: String): AiRemoteConfig? {
            if (json.isBlank()) return null
            val provider = extractString(json, "provider")?.let {
                runCatching { AiProviderType.valueOf(it.trim().uppercase()) }.getOrNull()
            }
            val baseUrl = extractString(json, "baseUrl")?.trim()?.takeIf {
                it.length <= 200 && (it.startsWith("https://") ||
                    it.startsWith("http://localhost") ||
                    it.startsWith("http://127.0.0.1"))
            }
            val model = extractString(json, "model")?.trim()
                ?.takeIf { it.isNotBlank() && it.length <= 100 }
            val maxTokens = extractInt(json, "maxTokens")?.coerceIn(256, 8192)
            val cloudEnabledByDefault = extractBoolean(json, "cloudEnabledByDefault")
            val forceOffline = extractBoolean(json, "forceOffline") ?: false
            if (provider == null && baseUrl == null && model == null &&
                maxTokens == null && cloudEnabledByDefault == null && !forceOffline
            ) {
                return null
            }
            return AiRemoteConfig(
                provider = provider,
                baseUrl = baseUrl,
                model = model,
                maxTokens = maxTokens,
                cloudEnabledByDefault = cloudEnabledByDefault,
                forceOffline = forceOffline
            )
        }

        /**
         * Tải JSON từ [url] trên IO. Trả về null khi [url] trống, lỗi mạng,
         * HTTP không thành công hoặc body không parse được (giữ cấu hình cũ).
         */
        suspend fun fetch(url: String): AiRemoteConfig? {
            if (url.isBlank()) return null
            return withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build()
                    val request = Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("Accept", "application/json")
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Log.w(TAG, "Remote AI config HTTP " + response.code)
                            return@withContext null
                        }
                        val body = response.body?.string().orEmpty()
                        if (body.isBlank() || body.length > 8192) return@withContext null
                        parse(body)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Remote AI config fetch failed: " + e.message)
                    null
                }
            }
        }

        private fun extractString(json: String, key: String): String? {
            val keyIdx = json.indexOf("\"$key\"")
            if (keyIdx < 0) return null
            val colon = json.indexOf(':', keyIdx + key.length + 2)
            if (colon < 0) return null
            var i = colon + 1
            while (i < json.length && json[i].isWhitespace()) i++
            if (i >= json.length || json[i] != '"') return null
            i++
            val sb = StringBuilder()
            while (i < json.length) {
                val c = json[i]
                if (c == '\\' && i + 1 < json.length) {
                    when (val n = json[i + 1]) {
                        '"', '\\', '/' -> sb.append(n)
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        else -> sb.append(n)
                    }
                    i += 2
                } else if (c == '"') {
                    return sb.toString()
                } else {
                    sb.append(c)
                    i++
                }
            }
            return null
        }

        private fun extractInt(json: String, key: String): Int? {
            val keyIdx = json.indexOf("\"$key\"")
            if (keyIdx < 0) return null
            val colon = json.indexOf(':', keyIdx + key.length + 2)
            if (colon < 0) return null
            var i = colon + 1
            while (i < json.length && json[i].isWhitespace()) i++
            var j = i
            if (j < json.length && json[j] == '-') j++
            while (j < json.length && json[j].isDigit()) j++
            if (j == i) return null
            return json.substring(i, j).toIntOrNull()
        }

        private fun extractBoolean(json: String, key: String): Boolean? {
            val keyIdx = json.indexOf("\"$key\"")
            if (keyIdx < 0) return null
            val colon = json.indexOf(':', keyIdx + key.length + 2)
            if (colon < 0) return null
            var i = colon + 1
            while (i < json.length && json[i].isWhitespace()) i++
            return when {
                json.startsWith("true", i) -> true
                json.startsWith("false", i) -> false
                else -> null
            }
        }
    }
}
