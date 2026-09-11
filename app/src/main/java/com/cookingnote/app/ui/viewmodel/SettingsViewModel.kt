package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Immutable UI State for the Settings screen.
 */
data class SettingsUiState(
    val provider: AiProviderType = AiProviderType.RULE_BASED,
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val maxTokens: String = "1500",
    val isProviderDropdownExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null,
    val isBackingUp: Boolean = false,
    val backupSuccessFile: File? = null,
    val errorMessage: String? = null
) {
    val isKeyRequired: Boolean
        get() = provider != AiProviderType.RULE_BASED && provider.requiresApiKey

    val endpointSummary: String
        get() = when (provider) {
            AiProviderType.RULE_BASED -> "Không gọi mạng (dùng thư viện cục bộ)."
            AiProviderType.OPENAI_CHAT -> "POST {baseUrl}/chat/completions (OpenAI-compatible)"
            AiProviderType.OPENAI_RESPONSES -> "POST {baseUrl}/responses (OpenAI Responses)"
            AiProviderType.ANTHROPIC -> "POST {baseUrl}/v1/messages (Anthropic)"
            AiProviderType.GEMINI -> "POST {baseUrl}/v1beta/models/{model}:generateContent?key=..."
        }
}

/**
 * ViewModel managing AI configuration forms, persistence, and SQLite database backup operations.
 */
class SettingsViewModel(
    private val aiSettingsStore: AiSettingsStore,
    private val databaseFile: File,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var isFormDirty = false

    init {
        viewModelScope.launch(ioDispatcher) {
            aiSettingsStore.settings.collect { settings ->
                if (!isFormDirty) {
                    _uiState.update { current ->
                        current.copy(
                            provider = settings.provider,
                            baseUrl = settings.baseUrl,
                            apiKey = settings.apiKey,
                            model = settings.model,
                            maxTokens = settings.maxTokens.toString()
                        )
                    }
                }
            }
        }
    }

    fun onProviderSelected(provider: AiProviderType) {
        isFormDirty = true
        _uiState.update {
            it.copy(
                provider = provider,
                baseUrl = provider.defaultBaseUrl,
                model = provider.defaultModel,
                isProviderDropdownExpanded = false
            )
        }
        viewModelScope.launch(ioDispatcher) {
            aiSettingsStore.update { current ->
                current.copy(
                    provider = provider,
                    baseUrl = provider.defaultBaseUrl,
                    model = provider.defaultModel
                )
            }
        }
    }

    fun onBaseUrlChanged(baseUrl: String) {
        isFormDirty = true
        _uiState.update { it.copy(baseUrl = baseUrl) }
    }

    fun onApiKeyChanged(apiKey: String) {
        isFormDirty = true
        _uiState.update { it.copy(apiKey = apiKey) }
    }

    fun onModelChanged(model: String) {
        isFormDirty = true
        _uiState.update { it.copy(model = model) }
    }

    fun onMaxTokensChanged(maxTokens: String) {
        val filtered = maxTokens.filter(Char::isDigit).take(5)
        isFormDirty = true
        _uiState.update { it.copy(maxTokens = filtered) }
    }

    fun setProviderDropdownExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isProviderDropdownExpanded = expanded) }
    }

    /**
     * Persists the AI configuration into [AiSettingsStore] on [ioDispatcher].
     */
    fun saveAiSettings(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccessMessage = null) }
            try {
                val current = _uiState.value
                val tokens = current.maxTokens.toIntOrNull()?.coerceIn(256, 8192) ?: 1500
                aiSettingsStore.update {
                    it.copy(
                        provider = current.provider,
                        baseUrl = current.baseUrl.trim(),
                        apiKey = current.apiKey.trim(),
                        model = current.model.trim(),
                        maxTokens = tokens
                    )
                }
                isFormDirty = false
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccessMessage = "Đã lưu cấu hình AI thành công.",
                        maxTokens = tokens.toString()
                    )
                }
                onSuccess?.invoke()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Lỗi khi lưu cấu hình: ${e.message ?: "không xác định"}"
                    )
                }
            }
        }
    }

    /**
     * Creates a copy of the SQLite database file in the specified [destinationDir] on [ioDispatcher].
     *
     * @return The copied [File] on success, or null if the source database does not exist or I/O fails.
     */
    suspend fun createDatabaseBackup(destinationDir: File): File? = withContext(ioDispatcher) {
        _uiState.update { it.copy(isBackingUp = true, errorMessage = null, backupSuccessFile = null) }
        try {
            if (!databaseFile.exists()) {
                val errMsg = "Tệp cơ sở dữ liệu không tồn tại: ${databaseFile.absolutePath}"
                _uiState.update { it.copy(isBackingUp = false, errorMessage = errMsg) }
                return@withContext null
            }
            destinationDir.mkdirs()
            val backupFile = File(destinationDir, "cookingnote-backup.db")
            databaseFile.copyTo(backupFile, overwrite = true)
            _uiState.update {
                it.copy(
                    isBackingUp = false,
                    backupSuccessFile = backupFile
                )
            }
            backupFile
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isBackingUp = false,
                    errorMessage = "Sao lưu thất bại: ${e.message ?: "lỗi I/O"}"
                )
            }
            null
        }
    }

    /**
     * Helper for Compose UI to initiate database backup and receive the file callback on the main thread.
     */
    fun backupDatabase(destinationDir: File, onBackupReady: (File) -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            val backup = createDatabaseBackup(destinationDir)
            if (backup != null) {
                withContext(Dispatchers.Main) {
                    onBackupReady(backup)
                }
            }
        }
    }

    /**
     * Clears any transient status or error messages.
     */
    fun clearMessages() {
        _uiState.update { it.copy(saveSuccessMessage = null, errorMessage = null, backupSuccessFile = null) }
    }
}
