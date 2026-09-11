package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.ai.AiService
import com.cookingnote.app.data.entity.ChatMessageEntity
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Identifier for quick prompt suggestions.
 */
enum class SuggestionType {
    FROM_PANTRY,
    QUICK_MEAL,
    VEGETARIAN,
    SPICY
}

/**
 * Model representing a quick prompt chip for the AI Chat screen.
 */
data class PromptSuggestion(
    val type: SuggestionType,
    val label: String,
    val prompt: String
)

/**
 * Default prompt suggestions displayed in the AI assistant screen.
 */
val DEFAULT_PROMPT_SUGGESTIONS = listOf(
    PromptSuggestion(
        type = SuggestionType.FROM_PANTRY,
        label = "Từ tủ lạnh",
        prompt = "Dựa trên tủ lạnh hiện tại, gợi ý 3 món tôi có thể nấu ngay. Mỗi món 2-3 dòng."
    ),
    PromptSuggestion(
        type = SuggestionType.QUICK_MEAL,
        label = "Món nhanh 15 phút",
        prompt = "Gợi ý 5 món Việt nấu trong 15 phút, đơn giản, nguyên liệu dễ mua."
    ),
    PromptSuggestion(
        type = SuggestionType.VEGETARIAN,
        label = "Món chay",
        prompt = "Gợi ý 5 món chay ngon, dễ nấu, phù hợp bữa cơm gia đình."
    ),
    PromptSuggestion(
        type = SuggestionType.SPICY,
        label = "Món cay",
        prompt = "Gợi ý 5 món Việt cay, có thể làm tại nhà, kèm nguyên liệu chính."
    )
)

/**
 * Immutable UI State for the AI Assistant Chat screen.
 */
data class AiUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val pantryCount: Int = 0,
    val isBusy: Boolean = false,
    val isCloudConfigured: Boolean = false,
    val keyRequired: Boolean = false,
    val providerLabel: String = "",
    val suggestions: List<PromptSuggestion> = DEFAULT_PROMPT_SUGGESTIONS,
    val statusBannerText: String = "Đang dùng gợi ý cục bộ (rule-based).",
    val errorMessage: String? = null
)

/**
 * ViewModel managing AI conversation history, query dispatch, offline fallback, and status state.
 */
class AiViewModel(
    private val aiService: AiService,
    private val aiSettingsStore: AiSettingsStore,
    private val repository: CookbookRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<AiUiState> = combine(
        repository.observeChat(),
        repository.observePantry(),
        aiSettingsStore.settings,
        _isBusy,
        _errorMessage
    ) { messages, pantry, settings, isBusy, error ->
        val isCloud = settings.provider != AiProviderType.RULE_BASED && settings.apiKey.isNotBlank()
        val keyReq = settings.provider != AiProviderType.RULE_BASED && settings.provider.requiresApiKey
        val banner = when {
            !isCloud && keyReq -> "AI chưa cấu hình. Vào Cài đặt → chọn provider → nhập API key."
            !isCloud -> "Đang dùng gợi ý cục bộ (rule-based)."
            else -> "Đã nhớ: ${messages.size} tin nhắn · AI thấy tủ lạnh: ${pantry.size} món."
        }
        AiUiState(
            messages = messages,
            pantryCount = pantry.size,
            isBusy = isBusy,
            isCloudConfigured = isCloud,
            keyRequired = keyReq,
            providerLabel = settings.provider.label,
            suggestions = DEFAULT_PROMPT_SUGGESTIONS,
            statusBannerText = banner,
            errorMessage = error
        )
    }.catch { throwable ->
        emit(
            AiUiState(
                statusBannerText = "Lỗi khi tải dữ liệu trợ lý",
                errorMessage = throwable.message ?: "Lỗi không xác định"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiUiState()
    )

    /**
     * Dispatches a user query to the AI service on [ioDispatcher].
     *
     * Appends the user message to Room, gathers recent context history, requests a response from
     * [AiService] (with built-in rule-based fallback if offline/unconfigured), and appends the
     * resulting assistant response to Room.
     */
    fun sendMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || _isBusy.value) return

        viewModelScope.launch(ioDispatcher) {
            _isBusy.value = true
            _errorMessage.value = null
            try {
                // 1. Record user message
                repository.appendMessage(ChatMessageEntity.ROLE_USER, trimmed)

                // 2. Fetch recent conversation history (up to 20 messages)
                val history = uiState.value.messages.takeLast(20).map { it.role to it.content }

                // 3. Query AI service (handles rule fallback internally if unconfigured or on network error)
                val suggestion = aiService.chat(trimmed, history)
                val responseContent = suggestion.detail ?: suggestion.summary

                // 4. Record assistant response
                repository.appendMessage(ChatMessageEntity.ROLE_ASSISTANT, responseContent)
            } catch (e: Exception) {
                val errorText = "Lỗi: ${e.message ?: "không xác định"}"
                repository.appendMessage(ChatMessageEntity.ROLE_ASSISTANT, errorText)
                _errorMessage.value = e.message
            } finally {
                _isBusy.value = false
            }
        }
    }

    /**
     * Sends a selected suggestion prompt to the AI service.
     */
    fun sendSuggestion(suggestion: PromptSuggestion) {
        sendMessage(suggestion.prompt)
    }

    /**
     * Clears all stored chat conversation messages.
     */
    fun clearChat() {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.clearChat()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Resets any active transient error messages.
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
