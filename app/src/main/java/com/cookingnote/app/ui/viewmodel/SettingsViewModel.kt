package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.prefs.AiCloudDefaults
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
    val isCustomEndpoint: Boolean = false,
    val isProviderDropdownExpanded: Boolean = false,
    val isAdvancedExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val isTestingConnection: Boolean = false,
    val testConnectionResult: String? = null,
    val isTestSuccess: Boolean? = null,
    val saveSuccessMessage: String? = null,
    val isBackingUp: Boolean = false,
    val backupSuccessFile: File? = null,
    val errorMessage: String? = null
) {
    val isKeyRequired: Boolean
        get() = provider != AiProviderType.RULE_BASED && provider.requiresApiKey

    // Chế độ đơn giản cho production: chỉ phân biệt offline vs cloud.
    val isCloudEnabled: Boolean
        get() = provider != AiProviderType.RULE_BASED

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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val aiService: com.cookingnote.app.ai.AiService? = null
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
                            maxTokens = settings.maxTokens.toString(),
                            isCustomEndpoint = settings.isCustom
                        )
                    }
                }
            }
        }
    }

    fun onCustomEndpointToggled(isCustom: Boolean) {
        if (!isCustom) {
            onUsePresetCloudSelected()
        } else {
            isFormDirty = true
            _uiState.update { it.copy(isCustomEndpoint = true) }
        }
    }

    fun onUsePresetCloudSelected() {
        isFormDirty = true
        _uiState.update {
            it.copy(
                isCustomEndpoint = false,
                provider = AiCloudDefaults.CLOUD_PROVIDER,
                baseUrl = AiCloudDefaults.CLOUD_BASE_URL,
                model = AiCloudDefaults.CLOUD_MODEL,
                apiKey = AiCloudDefaults.CLOUD_API_KEY,
                maxTokens = AiCloudDefaults.CLOUD_MAX_TOKENS.toString()
            )
        }
        viewModelScope.launch(ioDispatcher) {
            aiSettingsStore.update {
                it.copy(
                    provider = AiCloudDefaults.CLOUD_PROVIDER,
                    baseUrl = AiCloudDefaults.CLOUD_BASE_URL,
                    model = AiCloudDefaults.CLOUD_MODEL,
                    apiKey = AiCloudDefaults.CLOUD_API_KEY,
                    maxTokens = AiCloudDefaults.CLOUD_MAX_TOKENS
                )
            }
            isFormDirty = false
        }
    }

    fun onProviderSelected(provider: AiProviderType) {
        isFormDirty = true
        _uiState.update {
            it.copy(
                provider = provider,
                baseUrl = AiCloudDefaults.baseUrlFor(provider),
                model = AiCloudDefaults.modelFor(provider),
                isProviderDropdownExpanded = false,
                isCustomEndpoint = true
            )
        }
        viewModelScope.launch(ioDispatcher) {
            aiSettingsStore.update { current ->
                current.copy(
                    provider = provider,
                    baseUrl = AiCloudDefaults.baseUrlFor(provider),
                    model = AiCloudDefaults.modelFor(provider)
                )
            }
        }
    }

    fun onBaseUrlChanged(baseUrl: String) {
        isFormDirty = true
        _uiState.update { it.copy(baseUrl = baseUrl, isCustomEndpoint = true) }
    }

    fun onApiKeyChanged(apiKey: String) {
        isFormDirty = true
        _uiState.update { it.copy(apiKey = apiKey) }
    }

    fun onModelChanged(model: String) {
        isFormDirty = true
        _uiState.update { it.copy(model = model, isCustomEndpoint = true) }
    }

    fun onMaxTokensChanged(maxTokens: String) {
        val filtered = maxTokens.filter(Char::isDigit).take(5)
        isFormDirty = true
        _uiState.update { it.copy(maxTokens = filtered) }
    }

    fun setProviderDropdownExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isProviderDropdownExpanded = expanded) }
    }

    fun setAdvancedExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isAdvancedExpanded = expanded) }
    }

    fun onCloudEnabledChanged(enabled: Boolean) {
        val current = _uiState.value
        if (enabled == current.isCloudEnabled) return
        if (enabled) {
            onProviderSelected(AiCloudDefaults.CLOUD_PROVIDER)
        } else {
            onProviderSelected(AiProviderType.RULE_BASED)
        }
    }

    /**
     * Persists the AI configuration into [AiSettingsStore] on [ioDispatcher].
     */
    fun saveAiSettings(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccessMessage = null) }
            try {
                val current = _uiState.value
                val tokens = current.maxTokens.toIntOrNull()?.coerceIn(0, 32768) ?: 0
                aiSettingsStore.update {
                    it.copy(
                        provider = current.provider,
                        baseUrl = current.baseUrl.trim(),
                        apiKey = current.apiKey.trim(),
                        model = current.model.trim(),
                        maxTokens = tokens,
                        isCustom = current.isCustomEndpoint
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
     * Gửi yêu cầu kiểm tra kết nối AI đến service hiện tại để kiểm tra tính khả dụng.
     */
    fun testAiConnection() {
        if (_uiState.value.isTestingConnection) return
        viewModelScope.launch(ioDispatcher) {
            _uiState.update {
                it.copy(
                    isTestingConnection = true,
                    errorMessage = null,
                    saveSuccessMessage = null,
                    testConnectionResult = null,
                    isTestSuccess = null
                )
            }
            try {
                if (aiService == null) {
                    _uiState.update {
                        it.copy(
                            isTestingConnection = false,
                            isTestSuccess = false,
                            testConnectionResult = "Dịch vụ AI chưa sẵn sàng.",
                            errorMessage = "Dịch vụ AI chưa sẵn sàng."
                        )
                    }
                    return@launch
                }
                val reply = aiService.testConnection()
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        isTestSuccess = true,
                        testConnectionResult = reply,
                        saveSuccessMessage = "Kết nối thành công!"
                    )
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Lỗi không xác định"
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        isTestSuccess = false,
                        testConnectionResult = "Thất bại: $errorMsg",
                        errorMessage = "Kiểm tra kết nối thất bại: $errorMsg"
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
