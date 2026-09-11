package com.cookingnote.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.ui.local.LocalAppContainer
import com.cookingnote.app.ui.viewmodel.AppViewModelFactory
import com.cookingnote.app.ui.viewmodel.SettingsUiState
import com.cookingnote.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = AppViewModelFactory(LocalAppContainer.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccessMessage, uiState.errorMessage) {
        val message = uiState.saveSuccessMessage ?: uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    SettingsContent(
        uiState = uiState,
        onProviderSelected = viewModel::onProviderSelected,
        onBaseUrlChanged = viewModel::onBaseUrlChanged,
        onApiKeyChanged = viewModel::onApiKeyChanged,
        onModelChanged = viewModel::onModelChanged,
        onMaxTokensChanged = viewModel::onMaxTokensChanged,
        onDropdownExpandedChanged = viewModel::setProviderDropdownExpanded,
        onSaveAiSettings = { viewModel.saveAiSettings() },
        onBackupDatabase = {
            viewModel.backupDatabase(context.cacheDir) { backupFile ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    backupFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Backup database"))
            }
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onProviderSelected: (AiProviderType) -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onMaxTokensChanged: (String) -> Unit,
    onDropdownExpandedChanged: (Boolean) -> Unit,
    onSaveAiSettings: () -> Unit,
    onBackupDatabase: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Cài đặt") }) },
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("AI Provider", style = MaterialTheme.typography.titleLarge)

            ExposedDropdownMenuBox(
                expanded = uiState.isProviderDropdownExpanded,
                onExpandedChange = onDropdownExpandedChanged
            ) {
                OutlinedTextField(
                    value = uiState.provider.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Provider / Endpoint chuẩn") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(uiState.isProviderDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = uiState.isProviderDropdownExpanded,
                    onDismissRequest = { onDropdownExpandedChanged(false) }
                ) {
                    AiProviderType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = { onProviderSelected(type) }
                        )
                    }
                }
            }

            Text(
                "Endpoint sẽ gọi: ${uiState.endpointSummary}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.baseUrl,
                onValueChange = onBaseUrlChanged,
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isKeyRequired && !uiState.isSaving
            )
            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = onApiKeyChanged,
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isKeyRequired && !uiState.isSaving
            )
            OutlinedTextField(
                value = uiState.model,
                onValueChange = onModelChanged,
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isKeyRequired && !uiState.isSaving
            )
            OutlinedTextField(
                value = uiState.maxTokens,
                onValueChange = onMaxTokensChanged,
                label = { Text("max_tokens (256 – 8192)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isKeyRequired && !uiState.isSaving,
                singleLine = true
            )

            Button(
                onClick = onSaveAiSettings,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSaving) "Đang lưu…" else "Lưu cấu hình AI")
            }

            Text("Dữ liệu", style = MaterialTheme.typography.titleLarge)
            Button(
                onClick = onBackupDatabase,
                enabled = !uiState.isBackingUp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isBackingUp) "Đang sao lưu…" else "Backup SQLite")
            }

            Text(
                "App: Sổ tay Nấu ăn · Kotlin + Compose + Room · " +
                    "AI: 3 endpoint chuẩn (Chat Completions, Responses, Anthropic) + Gemini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
