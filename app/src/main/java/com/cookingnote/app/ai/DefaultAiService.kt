package com.cookingnote.app.ai

import android.util.Log
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Base64
import java.util.concurrent.TimeUnit

private const val TAG = "DefaultAiService"

class DefaultAiService(
    private val settingsStore: AiSettingsStore,
    private val repository: CookbookRepository
) : AiService {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    private val rule = RuleBasedAi(repository)

private fun isLocalProxy(baseUrl: String): Boolean =
        baseUrl.contains("localhost") ||
            baseUrl.contains("127.0.0.1") ||
            baseUrl.contains("10.0.2.2")

    private fun hasKeyOrProxy(settings: AiSettings): Boolean =
        settings.apiKey.isNotBlank() || isLocalProxy(settings.baseUrl)

    private fun resolveUrl(url: String): String {
        val isEmulator = runCatching {
            android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.MODEL.contains("google_sdk") ||
                android.os.Build.HARDWARE.contains("goldfish") ||
                android.os.Build.HARDWARE.contains("ranchu")
        }.getOrDefault(false)
        return if (isEmulator && (url.contains("://localhost:") || url.contains("://127.0.0.1:"))) {
            url.replace("://localhost:", "://10.0.2.2:").replace("://127.0.0.1:", "://10.0.2.2:")
        } else {
            url
        }
    }

    override val providerLabel: String
        get() = settingsStore.settings.value.provider.label

    override val isCloudConfigured: Boolean
        get() = settingsStore.settings.value.provider != AiProviderType.RULE_BASED &&
            hasKeyOrProxy(settingsStore.settings.value)

    override suspend fun chat(
        prompt: String,
        history: List<Pair<String, String>>
    ): AiSuggestion = withContext(Dispatchers.IO) {
        val settings = settingsStore.settings.value
        if (settings.provider == AiProviderType.RULE_BASED || !hasKeyOrProxy(settings)) {
            return@withContext ruleFallback("AI chưa cấu hình", prompt)
        }
        val systemContext = runCatching { buildSystemContext() }.getOrNull()
        try {
            val raw = when (settings.provider) {
                AiProviderType.OPENAI_CHAT -> callChatCompletions(settings, prompt, history, systemContext)
                AiProviderType.OPENAI_RESPONSES -> callResponses(settings, prompt, history, systemContext)
                AiProviderType.ANTHROPIC -> callAnthropic(settings, prompt, history, systemContext)
                AiProviderType.GEMINI -> callGeminiText(settings, prompt, history, systemContext)
                AiProviderType.RULE_BASED -> unreachable()
            }
            raw.toAiSuggestion(settings.provider)
        } catch (e: IOException) {
            Log.w(TAG, "AI call failed: ${settings.provider} ${e.message}")
            ruleFallback("Mạng lỗi: ${e.message ?: "unknown"}", prompt)
        } catch (e: Exception) {
            Log.w(TAG, "AI parse failed: ${settings.provider}", e)
            ruleFallback("Phản hồi không đọc được", prompt)
        }
    }

    override suspend fun testConnection(): String = withContext(Dispatchers.IO) {
        val settings = settingsStore.settings.value
        if (settings.provider == AiProviderType.RULE_BASED) {
            return@withContext "Chế độ Rule-based (Cục bộ): Không sử dụng kết nối mạng."
        }
        if (!hasKeyOrProxy(settings) && settings.provider.requiresApiKey) {
            throw IOException("Chưa nhập API Key cho ${settings.provider.label}.")
        }
        val raw = when (settings.provider) {
            AiProviderType.OPENAI_CHAT -> callChatCompletions(settings, "Xin chào, phản hồi ngắn gọn 1 câu để xác nhận kết nối.", emptyList(), null)
            AiProviderType.OPENAI_RESPONSES -> callResponses(settings, "Xin chào, phản hồi ngắn gọn 1 câu để xác nhận kết nối.", emptyList(), null)
            AiProviderType.ANTHROPIC -> callAnthropic(settings, "Xin chào, phản hồi ngắn gọn 1 câu để xác nhận kết nối.", emptyList(), null)
            AiProviderType.GEMINI -> callGeminiText(settings, "Xin chào, phản hồi ngắn gọn 1 câu để xác nhận kết nối.", emptyList(), null)
            AiProviderType.RULE_BASED -> unreachable()
        }
        val reply = raw.text.ifBlank { raw.reasoning }.orEmpty().trim()
        if (reply.isBlank()) {
            throw IOException("Máy chủ trả về phản hồi rỗng (finish: ${raw.finish}).")
        }
        "Kết nối thành công tới ${settings.provider.label}!\nModel: ${raw.model.ifBlank { settings.model }}\nPhản hồi: \"$reply\""
    }

    private suspend fun buildSystemContext(): String {
        val pantry = repository.observePantry().first()
        val favorites = repository.recentFavorites(5)
        val recent = repository.recentCooked(3)
        val sb = StringBuilder()
        sb.appendLine("Bạn là trợ lý ẩm thực thông minh và thân thiện của ứng dụng Cooking Note.")
        sb.appendLine("Nhiệm vụ của bạn là đồng hành, tư vấn thực đơn, chia sẻ công thức và mẹo nấu ăn ngon cho người dùng bằng tiếng Việt.")
        sb.appendLine()
        sb.appendLine("=== THÔNG TIN NGỮ CẢNH CỦA NGƯỜI DÙNG ===")
        if (pantry.isNotEmpty()) {
            sb.appendLine("• Tủ lạnh hiện có (${pantry.size} món):")
            pantry.forEach { sb.appendLine("  - ${it.name}: ${it.amount} ${it.unit}") }
        } else {
            sb.appendLine("• Tủ lạnh: Hiện chưa có nguyên liệu nào được lưu.")
        }
        if (favorites.isNotEmpty()) {
            sb.appendLine("• Món yêu thích gần đây: ${favorites.joinToString(", ") { it.name }}")
        }
        if (recent.isNotEmpty()) {
            sb.appendLine("• Món đã nấu gần đây: ${recent.joinToString(", ") { it.recipe.name }}")
        }
        sb.appendLine()
        sb.appendLine("=== QUY TẮC PHẢN HỒI ===")
        sb.appendLine("1. Khi người dùng hỏi kiểm tra tủ lạnh: Liệt kê rõ ràng các nguyên liệu đang có kèm số lượng. Nếu tủ lạnh trống, hãy nhắc họ vào mục 'Tủ lạnh' để thêm vào.")
        sb.appendLine("2. Khi người dùng muốn gợi ý món: Ưu tiên tận dụng tối đa các nguyên liệu đang có trong tủ lạnh và hợp khẩu vị người dùng.")
        sb.appendLine("3. Hướng dẫn công thức nấu: Trình bày rõ ràng gồm Tên món, Nguyên liệu cần chuẩn bị, Thời gian nấu và các Bước thực hiện ngắn gọn, dễ làm.")
        sb.appendLine("4. Văn phong: Tự nhiên, nhiệt tình, gần gũi, dùng tiếng Việt chuẩn mực.")
        sb.appendLine("5. Trả lời trực tiếp, cô đọng, đi thẳng vào món ăn và công thức, không suy nghĩ hay diễn giải dài dòng.")
        sb.appendLine("6. Không dùng ký tự tiêu đề markdown (#, ##, ###). Trình bày tiêu đề và tên món bằng chữ in đậm (**Tên món**), gạch đầu dòng (-) hoặc số thứ tự (1, 2, 3).")
        return sb.toString().trim()
    }

    override suspend fun suggestFromIngredients(ingredients: List<String>): List<AiSuggestion> =
        withContext(Dispatchers.IO) {
            val local = repository.suggestFromPantry().map { rec ->
                AiSuggestion(
                    title = rec.name,
                    summary = rec.description,
                    matchedRecipe = rec,
                    source = "pantry"
                )
            }
            val settings = settingsStore.settings.value
            if (settings.provider == AiProviderType.RULE_BASED || !hasKeyOrProxy(settings)) {
                return@withContext local
            }
            val prompt = "Gợi ý 5 món từ nguyên liệu: ${ingredients.joinToString(", ")}. " +
                "Mỗi món 1 dòng, bắt đầu bằng '- '."
            try {
                val single = chat(prompt)
                if (single.matchedRecipe != null) listOf(single) + local else local
            } catch (e: Exception) {
                local
            }
        }

    override suspend fun suggestFromImage(imageBytes: ByteArray): AiSuggestion =
        withContext(Dispatchers.IO) {
            val settings = settingsStore.settings.value
            if (settings.provider == AiProviderType.RULE_BASED || !hasKeyOrProxy(settings)) {
                return@withContext AiSuggestion(
                    title = "AI vision chưa cấu hình",
                    summary = "Thêm API key để dùng nhận diện ảnh.",
                    source = "rule-based"
                )
            }
            val prompt = "Đây là ảnh một món ăn. Đoán tên món và gợi ý ngắn gồm nguyên liệu + 3 bước chính."
            try {
                val raw = when (settings.provider) {
                    AiProviderType.OPENAI_CHAT -> callChatCompletionsVision(settings, prompt, imageBytes)
                    AiProviderType.OPENAI_RESPONSES -> callResponsesVision(settings, prompt, imageBytes)
                    AiProviderType.GEMINI -> callGeminiVision(settings, prompt, imageBytes)
                    AiProviderType.ANTHROPIC -> AiRaw(
                        text = "Anthropic chưa hỗ trợ vision qua endpoint này. Dùng OpenAI/Gemini.",
                        reasoning = null,
                        model = settings.model,
                        finish = "skipped"
                    )
                    AiProviderType.RULE_BASED -> unreachable()
                }
                raw.toAiSuggestion(settings.provider, visionTitle = "Phân tích ảnh")
            } catch (e: Exception) {
                Log.w(TAG, "Vision failed: ${settings.provider}", e)
                AiSuggestion(
                    title = "Vision lỗi",
                    summary = e.message ?: "unknown",
                    source = "fallback"
                )
            }
        }

    private fun unreachable(): AiRaw = AiRaw(text = "Provider không hợp lệ",
        reasoning = null, model = "", finish = "error")

    private suspend fun ruleFallback(reason: String, prompt: String): AiSuggestion {
        val pick = rule.suggest(prompt).firstOrNull()
        return pick?.copy(
            title = pick.title,
            summary = "$reason · Đang dùng gợi ý cục bộ.",
            source = "rule-based"
        ) ?: AiSuggestion(
            title = "Chưa có gợi ý",
            summary = reason,
            source = "rule-based"
        )
    }

    // ============================================================
    // 1) OpenAI Chat Completions (/v1/chat/completions)
    // ============================================================
    private fun callChatCompletions(
        settings: AiSettings,
        prompt: String,
        history: List<Pair<String, String>>,
        systemContext: String?
    ): AiRaw {
        val url = settings.baseUrl.trimEnd('/') + "/chat/completions"
        val systemText = systemContext?.takeIf { it.isNotBlank() }
            ?: "Bạn là trợ lý nấu ăn tiếng Việt của Cooking Note, tư vấn món ăn và gợi ý công thức ngắn gọn, dễ làm."
        val body = buildString {
            append("{")
            append("\"model\":\"").append(settings.model).append("\",")
            append("\"stream\":false,")
            if (settings.maxTokens > 0) {
                append("\"max_tokens\":").append(settings.maxTokens).append(",")
            }
            append("\"messages\":[")
            append("{\"role\":\"system\",\"content\":").append(jsonEscape(systemText)).append("},")
            history.forEach { (role, content) ->
                append("{\"role\":\"").append(role).append("\",\"content\":")
                append(jsonEscape(content)).append("},")
            }
            append("{\"role\":\"user\",\"content\":").append(jsonEscape(prompt)).append("}]")
            append("}")
        }
        val raw = postJson(url, settings.apiKey, body, useBearer = true)
        val content = extractJsonStringAfter(raw, "\"content\":")
            ?: extractAllStringsAfter(raw, "\"content\":").joinToString("").takeIf { it.isNotBlank() }
        val reasoning = extractJsonStringAfter(raw, "\"reasoning_content\":")
            ?: extractJsonStringAfter(raw, "\"reasoning\":")
        val finish = extractJsonStringAfter(raw, "\"finish_reason\":")
        return AiRaw(
            text = content?.takeIf { it.isNotBlank() && it != "null" } ?: "",
            reasoning = reasoning?.takeIf { it.isNotBlank() && it != "null" },
            model = settings.model,
            finish = finish ?: ""
        )
    }

    private fun callChatCompletionsVision(
        settings: AiSettings,
        prompt: String,
        imageBytes: ByteArray
    ): AiRaw {
        val url = settings.baseUrl.trimEnd('/') + "/chat/completions"
        val dataUrl = "data:image/jpeg;base64," +
            Base64.getEncoder().encodeToString(imageBytes)
        val maxTokensClause = if (settings.maxTokens > 0) "\"max_tokens\": ${settings.maxTokens}," else ""
        val body = """
            {
              "model": "${settings.model}",
              $maxTokensClause
              "messages": [{
                "role": "user",
                "content": [
                  {"type":"text","text": ${jsonEscape(prompt)}},
                  {"type":"image_url","image_url":{"url":"$dataUrl"}}
                ]
              }]
            }
        """.trimIndent()
        val raw = postJson(url, settings.apiKey, body, useBearer = true)
        val content = extractJsonStringAfter(raw, "\"content\":")
        val reasoning = extractJsonStringAfter(raw, "\"reasoning_content\":")
            ?: extractJsonStringAfter(raw, "\"reasoning\":")
        return AiRaw(
            text = content?.takeIf { it.isNotBlank() && it != "null" } ?: "",
            reasoning = reasoning?.takeIf { it.isNotBlank() && it != "null" },
            model = settings.model,
            finish = extractJsonStringAfter(raw, "\"finish_reason\":") ?: ""
        )
    }

    // ============================================================
    // 2) OpenAI Responses (/v1/responses)
    // ============================================================
    private fun callResponses(
        settings: AiSettings,
        prompt: String,
        history: List<Pair<String, String>>,
        systemContext: String?
    ): AiRaw {
        val url = settings.baseUrl.trimEnd('/') + "/responses"
        val input = StringBuilder("[")
        if (!systemContext.isNullOrBlank()) {
            input.append("{\"role\":\"system\",\"content\":")
                .append(jsonEscape(systemContext)).append("},")
        }
        history.forEach { (role, content) ->
            input.append("{\"role\":\"").append(role).append("\",\"content\":")
                .append(jsonEscape(content)).append("},")
        }
        input.append("{\"role\":\"user\",\"content\":").append(jsonEscape(prompt)).append("}")
        input.append("]")
        val maxTokensClause = if (settings.maxTokens > 0) "\"max_output_tokens\": ${settings.maxTokens}," else ""
        val body = """
            {
              "model": "${settings.model}",
              $maxTokensClause
              "input": $input
            }
        """.trimIndent()
        val raw = postJson(url, settings.apiKey, body, useBearer = true)
        val outputTexts = extractAllStringsAfter(raw, "\"output_text\":")
        val flat = outputTexts.joinToString("\n").trim()
        val reasoning = extractJsonStringAfter(raw, "\"reasoning_text\":")
        return AiRaw(
            text = flat,
            reasoning = reasoning?.takeIf { it.isNotBlank() && it != "null" },
            model = settings.model,
            finish = extractJsonStringAfter(raw, "\"status\":") ?: ""
        )
    }

    private fun callResponsesVision(
        settings: AiSettings,
        prompt: String,
        imageBytes: ByteArray
    ): AiRaw {
        val url = settings.baseUrl.trimEnd('/') + "/responses"
        val dataUrl = "data:image/jpeg;base64," +
            Base64.getEncoder().encodeToString(imageBytes)
        val maxTokensClause = if (settings.maxTokens > 0) "\"max_output_tokens\": ${settings.maxTokens}," else ""
        val body = """
            {
              "model": "${settings.model}",
              $maxTokensClause
              "input": [{
                "role": "user",
                "content": [
                  {"type":"input_text","text": ${jsonEscape(prompt)}},
                  {"type":"input_image","image_url":"$dataUrl"}
                ]
              }]
            }
        """.trimIndent()
        val raw = postJson(url, settings.apiKey, body, useBearer = true)
        val outputTexts = extractAllStringsAfter(raw, "\"output_text\":")
        val flat = outputTexts.joinToString("\n").trim()
        return AiRaw(
            text = flat,
            reasoning = extractJsonStringAfter(raw, "\"reasoning_text\":")
                ?.takeIf { it.isNotBlank() && it != "null" },
            model = settings.model,
            finish = extractJsonStringAfter(raw, "\"status\":") ?: ""
        )
    }

    // ============================================================
    // 3) Anthropic Messages (/v1/messages)
    // ============================================================
    private fun callAnthropic(
        settings: AiSettings,
        prompt: String,
        history: List<Pair<String, String>>,
        systemContext: String?
    ): AiRaw {
        val url = settings.baseUrl.trimEnd('/') + "/v1/messages"
        val messages = history.map { (role, content) ->
            """{"role":"$role","content":${jsonEscape(content)}}"""
        }
        val systemText = systemContext?.takeIf { it.isNotBlank() }
            ?: "Bạn là trợ lý nấu ăn tiếng Việt của Cooking Note, tư vấn món ăn và gợi ý công thức ngắn gọn, dễ làm."
        val maxTokensClause = if (settings.maxTokens > 0) "\"max_tokens\": ${settings.maxTokens}," else "\"max_tokens\": 8192,"
        val body = """
            {
              "model": "${settings.model}",
              $maxTokensClause
              "system": ${jsonEscape(systemText)},
              "messages": [
                ${messages.joinToString(",")},
                {"role":"user","content":${jsonEscape(prompt)}}
              ]
            }
        """.trimIndent()
        val raw = postJson(url, settings.apiKey, body, useBearer = false,
            extraHeaders = mapOf("anthropic-version" to "2023-06-01"))
        val textBlocks = extractAllStringsAfter(raw, "\"text\":")
        val joined = textBlocks.joinToString("\n").trim()
        return AiRaw(
            text = joined,
            reasoning = null,
            model = settings.model,
            finish = extractJsonStringAfter(raw, "\"stop_reason\":") ?: ""
        )
    }

    // ============================================================
    // 4) Gemini Generate Content
    // ============================================================
    private fun callGeminiText(
        settings: AiSettings,
        prompt: String,
        history: List<Pair<String, String>>,
        systemContext: String?
    ): AiRaw {
        val model = settings.model.ifBlank { "gemini-1.5-flash" }
        val url = "${settings.baseUrl.trimEnd('/')}/v1beta/models/$model:generateContent?key=${settings.apiKey}"
        val sb = StringBuilder()
        sb.append("[")
        if (!systemContext.isNullOrBlank()) {
            sb.append("{\"role\":\"user\",\"parts\":[{\"text\":")
                .append(jsonEscape("[Hệ thống] " + systemContext))
                .append("}]},")
            sb.append("{\"role\":\"model\",\"parts\":[{\"text\":\"Đã nhận ngữ cảnh.\"}]},")
        }
        history.forEach { (role, content) ->
            val mapped = if (role == "user") "user" else "model"
            sb.append("{\"role\":\"").append(mapped).append("\",\"parts\":[{\"text\":")
                .append(jsonEscape(content)).append("}]},")
        }
        sb.append("{\"role\":\"user\",\"parts\":[{\"text\":")
            .append(jsonEscape(prompt)).append("}]}")
        sb.append("]")
        val genConfigClause = if (settings.maxTokens > 0) ",\"generationConfig\": {\"maxOutputTokens\": ${settings.maxTokens}}" else ""
        val body = """
            {
              "contents": ${sb.toString()}
              $genConfigClause
            }
        """.trimIndent()
        val raw = postJson(url, "", body, useBearer = false)
        val textBlocks = extractAllStringsAfter(raw, "\"text\":")
        val joined = textBlocks.joinToString("\n").trim()
        return AiRaw(
            text = joined,
            reasoning = null,
            model = model,
            finish = extractJsonStringAfter(raw, "\"finishReason\":") ?: ""
        )
    }

    private fun callGeminiVision(
        settings: AiSettings,
        prompt: String,
        imageBytes: ByteArray
    ): AiRaw {
        val model = settings.model.ifBlank { "gemini-1.5-flash" }
        val url = "${settings.baseUrl.trimEnd('/')}/v1beta/models/$model:generateContent?key=${settings.apiKey}"
        val b64 = Base64.getEncoder().encodeToString(imageBytes)
        val genConfigClause = if (settings.maxTokens > 0) ",\"generationConfig\":{\"maxOutputTokens\":${settings.maxTokens}}" else ""
        val body = """
            {
              "contents":[{
                "role":"user",
                "parts":[
                  {"text":${jsonEscape(prompt)}},
                  {"inline_data":{"mime_type":"image/jpeg","data":"$b64"}}
                ]
              }]
              $genConfigClause
            }
        """.trimIndent()
        val raw = postJson(url, "", body, useBearer = false)
        val textBlocks = extractAllStringsAfter(raw, "\"text\":")
        return AiRaw(
            text = textBlocks.joinToString("\n").trim(),
            reasoning = null,
            model = model,
            finish = extractJsonStringAfter(raw, "\"finishReason\":") ?: ""
        )
    }

    // ============================================================
    // HTTP
    // ============================================================
    private fun postJson(
        url: String,
        apiKey: String,
        body: String,
        useBearer: Boolean,
        extraHeaders: Map<String, String> = emptyMap()
    ): String {
        val resolvedUrl = resolveUrl(url)
        val requestBuilder = Request.Builder()
            .url(resolvedUrl)
            .addHeader("Content-Type", "application/json")
        if (apiKey.isNotBlank()) {
            if (useBearer) requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            else requestBuilder.addHeader("x-api-key", apiKey)
        }
        extraHeaders.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
        val request = requestBuilder
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${text.take(500)}")
            }
            return text
        }
    }

    // ============================================================
    // JSON helpers
    // ============================================================
    private fun jsonEscape(text: String): String {
        val sb = StringBuilder("\"")
        text.forEach { c ->
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (c.code < 0x20) sb.append("\\u%04x".format(c.code)) else sb.append(c)
            }
        }
        sb.append('"')
        return sb.toString()
    }

    private fun extractJsonStringAfter(body: String, key: String): String? {
        val idx = body.indexOf(key)
        if (idx < 0) return null
        return extractJsonString(body, idx + key.length)
    }

    private fun extractAllStringsAfter(body: String, key: String): List<String> {
        val out = mutableListOf<String>()
        var start = 0
        while (true) {
            val idx = body.indexOf(key, start)
            if (idx < 0) break
            val value = extractJsonString(body, idx + key.length)
            start = idx + key.length
            if (value != null) out.add(value)
        }
        return out
    }

    private fun extractJsonString(body: String, start: Int): String? {
        var i = start
        while (i < body.length && body[i].isWhitespace()) i++
        if (i >= body.length || body[i] != '"') return null
        i++
        val sb = StringBuilder()
        while (i < body.length) {
            val c = body[i]
            if (c == '\\' && i + 1 < body.length) {
                when (val n = body[i + 1]) {
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
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
}

private data class AiRaw(
    val text: String,
    val reasoning: String?,
    val model: String,
    val finish: String
)

private fun AiRaw.toAiSuggestion(provider: AiProviderType, visionTitle: String = "Gợi ý AI"): AiSuggestion {
    val title = if (visionTitle == "Phân tích ảnh") visionTitle else "Gợi ý AI"
    return when {
        text.isNotBlank() && reasoning.isNullOrBlank() -> AiSuggestion(
            title = title,
            summary = text.take(400),
            detail = text,
            source = "${provider.label} · model=${model.ifBlank { "?" }} · finish=${finish.ifBlank { "?" }}"
        )
        text.isNotBlank() && reasoning != null -> AiSuggestion(
            title = title,
            summary = text.take(400),
            detail = text,
            source = "${provider.label} [có reasoning] · model=${model.ifBlank { "?" }}"
        )
        text.isBlank() && reasoning != null -> AiSuggestion(
            title = "$title · [tự suy luận]",
            summary = reasoning.take(400),
            detail = reasoning,
            source = "${provider.label} reasoning-only · model=${model.ifBlank { "?" }}"
        )
        else -> AiSuggestion(
            title = "Phản hồi rỗng",
            summary = "Model không xuất output (finish=${finish.ifBlank { "?" }}).",
            detail = null,
            source = "${provider.label} · model=${model.ifBlank { "?" }}"
        )
    }
}