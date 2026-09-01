package com.cookingnote.app.ai

import android.util.Log
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
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

    override val providerLabel: String
        get() = settingsStore.settings.value.provider.label

    override val isCloudConfigured: Boolean
        get() = settingsStore.settings.value.provider != AiProviderType.RULE_BASED &&
            settingsStore.settings.value.apiKey.isNotBlank()

    override suspend fun chat(
        prompt: String,
        history: List<Pair<String, String>>
    ): AiSuggestion = withContext(Dispatchers.IO) {
        val settings = settingsStore.settings.value
        if (settings.provider == AiProviderType.RULE_BASED || settings.apiKey.isBlank()) {
            return@withContext ruleFallback("AI chưa cấu hình", prompt)
        }
        try {
            val raw = when (settings.provider) {
                AiProviderType.OPENAI_CHAT -> callChatCompletions(settings, prompt, history)
                AiProviderType.OPENAI_RESPONSES -> callResponses(settings, prompt, history)
                AiProviderType.ANTHROPIC -> callAnthropic(settings, prompt, history)
                AiProviderType.GEMINI -> callGeminiText(settings, prompt, history)
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
            if (settings.provider == AiProviderType.RULE_BASED || settings.apiKey.isBlank()) {
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
            if (settings.provider == AiProviderType.RULE_BASED || settings.apiKey.isBlank()) {
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
        history: List<Pair<String, String>>
    ): AiRaw {
        val url = settings.baseUrl.trimEnd('/') + "/chat/completions"
        val body = buildString {
            append("{")
            append("\"model\":\"").append(settings.model).append("\",")
            append("\"max_tokens\":").append(settings.maxTokens).append(",")
            append("\"messages\":[")
            append("{\"role\":\"system\",\"content\":\"Bạn là trợ lý nấu ăn tiếng Việt, gợi ý ngắn gọn.\"},")
            history.forEach { (role, content) ->
                append("{\"role\":\"").append(role).append("\",\"content\":")
                append(jsonEscape(content)).append("},")
            }
            append("{\"role\":\"user\",\"content\":").append(jsonEscape(prompt)).append("}]")
            append("}")
        }
        val raw = postJson(url, settings.apiKey, body, useBearer = true)
        val content = extractJsonStringAfter(raw, "\"content\":")
        val reasoning = extractJsonStringAfter(raw, "\"reasoning_content\":")
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
            android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val body = """
            {
              "model": "${settings.model}",
              "max_tokens": ${settings.maxTokens},
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
        history: List<Pair<String, String>>
    ): AiRaw {
        val url = settings.baseUrl.trimEnd('/') + "/responses"
        val input = StringBuilder("[")
        history.forEach { (role, content) ->
            input.append("{\"role\":\"").append(role).append("\",\"content\":")
                .append(jsonEscape(content)).append("},")
        }
        input.append("{\"role\":\"user\",\"content\":").append(jsonEscape(prompt)).append("}")
        input.append("]")
        val body = """
            {
              "model": "${settings.model}",
              "max_output_tokens": ${settings.maxTokens},
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
            android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val body = """
            {
              "model": "${settings.model}",
              "max_output_tokens": ${settings.maxTokens},
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
        history: List<Pair<String, String>>
    ): AiRaw {
        val url = settings.baseUrl.trimEnd('/') + "/v1/messages"
        val messages = history.map { (role, content) ->
            """{"role":"$role","content":${jsonEscape(content)}}"""
        }
        val body = """
            {
              "model": "${settings.model}",
              "max_tokens": ${settings.maxTokens},
              "system": "Bạn là trợ lý nấu ăn tiếng Việt, gợi ý ngắn gọn.",
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
        history: List<Pair<String, String>>
    ): AiRaw {
        val model = settings.model.ifBlank { "gemini-1.5-flash" }
        val url = "${settings.baseUrl.trimEnd('/')}/v1beta/models/$model:generateContent?key=${settings.apiKey}"
        val parts = StringBuilder("[\"")
        history.forEach { (role, content) ->
            parts.append(role).append(": ").append(content.replace("\"", "'")).append("\\n")
        }
        parts.append(prompt.replace("\"", "'")).append("\"]")
        val contents = "[{\"role\":\"user\",\"parts\":[{\"text\":" +
            jsonEscape("$prompt") +
            "}]}]"
        val body = """
            {
              "contents": ${contents},
              "generationConfig": {"maxOutputTokens": ${settings.maxTokens}}
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
        val b64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val body = """
            {
              "contents":[{
                "role":"user",
                "parts":[
                  {"text":${jsonEscape(prompt)}},
                  {"inline_data":{"mime_type":"image/jpeg","data":"$b64"}}
                ]
              }],
              "generationConfig":{"maxOutputTokens":${settings.maxTokens}}
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
        val requestBuilder = Request.Builder()
            .url(url)
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