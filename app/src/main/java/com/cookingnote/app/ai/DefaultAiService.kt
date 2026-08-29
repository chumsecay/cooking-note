package com.cookingnote.app.ai

import com.cookingnote.app.data.prefs.AiProviderType
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

class DefaultAiService(
    private val settingsStore: AiSettingsStore,
    private val repository: CookbookRepository
) : AiService {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
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
            val pick = rule.suggest(prompt).firstOrNull()
            return@withContext pick ?: AiSuggestion(
                title = "Chưa có gợi ý",
                summary = "Thêm nguyên liệu vào tủ lạnh hoặc cấu hình AI để nhận gợi ý.",
                source = "rule-based"
            )
        }
        try {
            when (settings.provider) {
                AiProviderType.GEMINI -> callGemini(settings, prompt, history)
                AiProviderType.ANTHROPIC -> callAnthropic(settings, prompt, history)
                AiProviderType.OPENAI,
                AiProviderType.OPENAI_COMPATIBLE,
                AiProviderType.OPENROUTER -> callOpenAiCompatible(settings, prompt, history)
                AiProviderType.RULE_BASED -> unreachable()
            }
        } catch (e: IOException) {
            rule.suggest(prompt).firstOrNull() ?: AiSuggestion(
                title = "Lỗi mạng",
                summary = "Không gọi được AI (${e.message}). Đang dùng gợi ý cục bộ.",
                source = "fallback"
            )
        }
    }

    override suspend fun suggestFromIngredients(ingredients: List<String>): List<AiSuggestion> =
        withContext(Dispatchers.IO) {
            val local = repository.suggestFromPantry().map { rec ->
                AiSuggestion(
                    title = rec.name,
                    summary = rec.description,
                    matchedRecipe = rec,
                    source = "rule-based"
                )
            }
            if (!isCloudConfigured) return@withContext local
            val prompt = buildString {
                append("Gợi ý 5 món từ các nguyên liệu: ${ingredients.joinToString(", ")}. ")
                append("Trả về JSON {items:[{title, summary, ingredients, steps}]}.")
            }
            try {
                val suggestion = chat(prompt)
                if (suggestion.matchedRecipe != null) listOf(suggestion) + local
                else local
            } catch (e: Exception) {
                local
            }
        }

    override suspend fun suggestFromImage(imageBytes: ByteArray): AiSuggestion =
        withContext(Dispatchers.IO) {
            if (!isCloudConfigured) {
                return@withContext AiSuggestion(
                    title = "AI vision chưa cấu hình",
                    summary = "Thêm API key OpenAI/Gemini để dùng nhận diện ảnh.",
                    source = "rule-based"
                )
            }
            val prompt = "Đây là ảnh một món ăn. Đoán tên món và gợi ý công thức ngắn gồm nguyên liệu + 3 bước chính."
            when (settingsStore.settings.value.provider) {
                AiProviderType.GEMINI -> callGeminiVision(prompt, imageBytes)
                AiProviderType.OPENAI, AiProviderType.OPENAI_COMPATIBLE, AiProviderType.OPENROUTER ->
                    callOpenAiVision(prompt, imageBytes)
                AiProviderType.ANTHROPIC -> AiSuggestion(
                    title = "Vision chưa hỗ trợ",
                    summary = "Anthropic chưa hỗ trợ vision qua endpoint này. Dùng OpenAI/Gemini.",
                    source = "fallback"
                )
                AiProviderType.RULE_BASED -> AiSuggestion(
                    title = "AI chưa cấu hình",
                    summary = "Bật provider có hỗ trợ vision.",
                    source = "rule-based"
                )
            }
        }

    private fun unreachable(): AiSuggestion =
        AiSuggestion("Lỗi", "Provider không hợp lệ", source = "fallback")

    private fun callOpenAiCompatible(
        settings: com.cookingnote.app.data.prefs.AiSettings,
        prompt: String,
        history: List<Pair<String, String>>
    ): AiSuggestion {
        val url = settings.baseUrl.trimEnd('/') + "/chat/completions"
        val body = buildString {
            append("{")
            append("\"model\":\"").append(settings.model).append("\",")
            append("\"temperature\":").append(settings.temperature).append(",")
            append("\"messages\":[")
            append("{\"role\":\"system\",\"content\":\"Bạn là trợ lý nấu ăn tiếng Việt, gợi ý ngắn gọn.\"},")
            history.forEach { (role, content) ->
                append("{\"role\":\"").append(role).append("\",\"content\":")
                append(jsonEscape(content)).append("},")
            }
            append("{\"role\":\"user\",\"content\":").append(jsonEscape(prompt)).append("}]")
            append("}")
        }
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${settings.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $text")
            val content = extractOpenAiContent(text) ?: throw IOException("Không parse được response")
            return AiSuggestion(
                title = "Gợi ý AI",
                summary = content.take(400),
                detail = content,
                source = settings.provider.label
            )
        }
    }

    private fun callOpenAiVision(prompt: String, imageBytes: ByteArray): AiSuggestion {
        val settings = settingsStore.settings.value
        val url = settings.baseUrl.trimEnd('/') + "/chat/completions"
        val dataUrl = "data:image/jpeg;base64," + android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val body = """
            {
              "model": "${settings.model}",
              "messages": [{
                "role": "user",
                "content": [
                  {"type":"text","text": ${jsonEscape(prompt)}},
                  {"type":"image_url","image_url":{"url": "$dataUrl"}}
                ]
              }]
            }
        """.trimIndent()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${settings.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $text")
            val content = extractOpenAiContent(text) ?: throw IOException("Không parse được vision response")
            return AiSuggestion(
                title = "Phân tích ảnh",
                summary = content.take(400),
                detail = content,
                source = settings.provider.label
            )
        }
    }

    private fun callGemini(
        settings: com.cookingnote.app.data.prefs.AiSettings,
        prompt: String,
        history: List<Pair<String, String>>
    ): AiSuggestion {
        val model = settings.model.ifBlank { "gemini-1.5-flash" }
        val url = "${settings.baseUrl.trimEnd('/')}/v1beta/models/$model:generateContent?key=${settings.apiKey}"
        val body = buildString {
            append("{")
            append("\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":")
            append(jsonEscape(prompt)).append("}]}]")
            append(",\"generationConfig\":{\"temperature\":").append(settings.temperature).append("}")
            append("}")
        }
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $text")
            val content = extractGeminiText(text) ?: throw IOException("Không parse Gemini response")
            return AiSuggestion(
                title = "Gemini",
                summary = content.take(400),
                detail = content,
                source = "Gemini"
            )
        }
    }

    private fun callGeminiVision(prompt: String, imageBytes: ByteArray): AiSuggestion {
        val settings = settingsStore.settings.value
        val model = settings.model.ifBlank { "gemini-1.5-flash" }
        val url = "${settings.baseUrl.trimEnd('/')}/v1beta/models/$model:generateContent?key=${settings.apiKey}"
        val b64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val body = """
            {
              "contents":[{
                "role":"user",
                "parts":[
                  {"text": ${jsonEscape(prompt)}},
                  {"inline_data":{"mime_type":"image/jpeg","data":"$b64"}}
                ]
              }]
            }
        """.trimIndent()
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $text")
            val content = extractGeminiText(text) ?: throw IOException("Gemini vision fail")
            return AiSuggestion(
                title = "Gemini Vision",
                summary = content.take(400),
                detail = content,
                source = "Gemini"
            )
        }
    }

    private fun callAnthropic(
        settings: com.cookingnote.app.data.prefs.AiSettings,
        prompt: String,
        history: List<Pair<String, String>>
    ): AiSuggestion {
        val url = settings.baseUrl.trimEnd('/') + "/v1/messages"
        val messages = history.map { (role, content) ->
            """{"role":"$role","content":${jsonEscape(content)}}"""
        }
        val body = """
            {
              "model": "${settings.model}",
              "max_tokens": 1024,
              "temperature": ${settings.temperature},
              "messages": [${messages.joinToString(",")},
                {"role":"user","content":${jsonEscape(prompt)}}
              ]
            }
        """.trimIndent()
        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", settings.apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $text")
            val content = extractAnthropicText(text) ?: throw IOException("Anthropic parse fail")
            return AiSuggestion(
                title = "Anthropic",
                summary = content.take(400),
                detail = content,
                source = "Anthropic"
            )
        }
    }

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

    private fun extractOpenAiContent(body: String): String? {
        val key = "\"content\":"
        val idx = body.indexOf(key)
        if (idx < 0) return null
        return extractJsonString(body, idx + key.length)
    }

    private fun extractGeminiText(body: String): String? {
        val key = "\"text\":"
        val idx = body.indexOf(key)
        if (idx < 0) return null
        return extractJsonString(body, idx + key.length)
    }

    private fun extractAnthropicText(body: String): String? {
        val key = "\"text\":"
        val idx = body.indexOf(key)
        if (idx < 0) return null
        return extractJsonString(body, idx + key.length)
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