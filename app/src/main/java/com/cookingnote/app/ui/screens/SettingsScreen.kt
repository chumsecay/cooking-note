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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.ui.local.LocalAppContainer
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val container = LocalAppContainer.current
    val settings by container.aiSettings.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var providerExpanded by remember { mutableStateOf(false) }
    var baseUrl by remember(settings.provider) { mutableStateOf(settings.baseUrl) }
    var apiKey by remember(settings.provider) { mutableStateOf(settings.apiKey) }
    var model by remember(settings.provider) { mutableStateOf(settings.model) }
    var maxTokens by remember(settings.provider) { mutableStateOf(settings.maxTokens.toString()) }

    val keyRequired = settings.provider != AiProviderType.RULE_BASED &&
        settings.provider.requiresApiKey

    Scaffold(topBar = { TopAppBar(title = { Text("Cài đặt") }) }) { padding ->
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
                expanded = providerExpanded,
                onExpandedChange = { providerExpanded = it }
            ) {
                OutlinedTextField(
                    value = settings.provider.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Provider / Endpoint chuẩn") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(providerExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = providerExpanded,
                    onDismissRequest = { providerExpanded = false }
                ) {
                    AiProviderType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = {
                                providerExpanded = false
                                scope.launch {
                                    container.aiSettings.update { current ->
                                        current.copy(
                                            provider = type,
                                            baseUrl = type.defaultBaseUrl,
                                            model = type.defaultModel
                                        )
                                    }
                                    baseUrl = type.defaultBaseUrl
                                    model = type.defaultModel
                                }
                            }
                        )
                    }
                }
            }

            Text(
                "Endpoint sẽ gọi: ${endpointSummary(settings.provider)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth(),
                enabled = keyRequired
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                enabled = keyRequired
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                enabled = keyRequired
            )
            OutlinedTextField(
                value = maxTokens,
                onValueChange = { v -> maxTokens = v.filter(Char::isDigit).take(5) },
                label = { Text("max_tokens (256 – 8192)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = keyRequired,
                singleLine = true
            )

            Button(
                onClick = {
                    scope.launch {
                        container.aiSettings.update {
                            it.copy(
                                baseUrl = baseUrl.trim(),
                                apiKey = apiKey.trim(),
                                model = model.trim(),
                                maxTokens = maxTokens.toIntOrNull()
                                    ?.coerceIn(256, 8192) ?: 1500
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Lưu cấu hình AI") }

            Text("Dữ liệu", style = MaterialTheme.typography.titleLarge)
            Button(
                onClick = {
                    val db = container.databaseFile()
                    if (!db.exists()) return@Button
                    val cacheCopy = File(context.cacheDir, "cookingnote-backup.db")
                    db.copyTo(cacheCopy, overwrite = true)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        cacheCopy
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/octet-stream"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Backup database"))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Backup SQLite") }

            Text(
                "App: Sổ tay Nấu ăn · Kotlin + Compose + Room · " +
                    "AI: 3 endpoint chuẩn (Chat Completions, Responses, Anthropic) + Gemini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun endpointSummary(p: AiProviderType): String = when (p) {
    AiProviderType.RULE_BASED -> "Không gọi mạng (dùng thư viện cục bộ)."
    AiProviderType.OPENAI_CHAT -> "POST {baseUrl}/chat/completions (OpenAI-compatible)"
    AiProviderType.OPENAI_RESPONSES -> "POST {baseUrl}/responses (OpenAI Responses)"
    AiProviderType.ANTHROPIC -> "POST {baseUrl}/v1/messages (Anthropic)"
    AiProviderType.GEMINI -> "POST {baseUrl}/v1beta/models/{model}:generateContent?key=..."
}
