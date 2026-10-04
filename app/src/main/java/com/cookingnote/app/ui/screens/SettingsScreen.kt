package com.cookingnote.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cookingnote.app.data.prefs.AiCloudDefaults
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
    ),
    onOpenAuth: () -> Unit = {}
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
        onCloudEnabledChanged = viewModel::onCloudEnabledChanged,
        onCustomEndpointToggled = viewModel::onCustomEndpointToggled,
        onUsePresetCloudSelected = viewModel::onUsePresetCloudSelected,
        onProviderSelected = viewModel::onProviderSelected,
        onBaseUrlChanged = viewModel::onBaseUrlChanged,
        onApiKeyChanged = viewModel::onApiKeyChanged,
        onModelChanged = viewModel::onModelChanged,
        onMaxTokensChanged = viewModel::onMaxTokensChanged,
        onDropdownExpandedChanged = viewModel::setProviderDropdownExpanded,
        onAdvancedExpandedChanged = viewModel::setAdvancedExpanded,
        onSaveAiSettings = { viewModel.saveAiSettings() },
        onTestAiConnection = { viewModel.testAiConnection() },
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
        onOpenAuth = onOpenAuth,
        darkMode = viewModel.darkMode,
        onSetDarkMode = viewModel::setDarkMode,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onCloudEnabledChanged: (Boolean) -> Unit,
    onCustomEndpointToggled: (Boolean) -> Unit,
    onUsePresetCloudSelected: () -> Unit,
    onProviderSelected: (AiProviderType) -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onMaxTokensChanged: (String) -> Unit,
    onDropdownExpandedChanged: (Boolean) -> Unit,
    onAdvancedExpandedChanged: (Boolean) -> Unit,
    onSaveAiSettings: () -> Unit,
    onTestAiConnection: () -> Unit = {},
    onBackupDatabase: () -> Unit,
    onOpenAuth: () -> Unit = {},
    darkMode: kotlinx.coroutines.flow.Flow<Boolean?>? = null,
    onSetDarkMode: (Boolean?) -> Unit = {},
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
            // --- MỤC 0: TÀI KHOẢN NGƯỜI DÙNG ---
            Text("Tài khoản người dùng", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Quản lý thông tin hồ sơ và trải nghiệm nấu ăn cá nhân hóa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onOpenAuth,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Quản lý tài khoản")
                    }
                }
            }

            // --- MỤC 1: TRẢI NGHIỆM & GIAO DIỆN ---
            Text("Giao diện & Hiển thị", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                val darkModeState = darkMode?.collectAsStateWithLifecycle(initialValue = null)
                val darkModeValue: Boolean? = darkModeState?.value
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Chế độ tối", style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (darkModeValue == null) "Tự động theo hệ thống thiết bị"
                                else if (darkModeValue == true) "Đang bật chế độ tối"
                                else "Đang bật chế độ sáng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = darkModeValue == true,
                            onCheckedChange = { checked ->
                                // null -> true: từ auto sang ép tối; toggle off quay về auto
                                onSetDarkMode(if (checked) true else if (darkModeValue == true) false else null)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ngôn ngữ", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Tiếng Việt (Mặc định)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Đơn vị đo lường", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Hệ mét tiêu chuẩn (gam, mililit, muỗng cà phê)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- MỤC 2: TRỢ LÝ NẤU ĂN AI (CHUYỂN ĐỔI CLOUD CÀI SẴN VÀ CUSTOM ENDPOINT) ---
            Text("Trợ lý trí tuệ nhân tạo (AI)", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Kết nối trợ lý thông minh",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                if (!uiState.isCustomEndpoint) {
                                    "Đang sử dụng hệ thống đám mây tiêu chuẩn. Sẵn sàng gợi ý món ăn, giải đáp công thức và gợi ý nguyên liệu."
                                } else {
                                    "Đang sử dụng máy chủ tùy chỉnh cá nhân (chế độ nâng cao)."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Tự cấu hình máy chủ riêng",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Chỉ bật nếu bạn muốn kết nối vào máy chủ AI tự dựng hoặc mô hình riêng tư.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isCustomEndpoint,
                            onCheckedChange = onCustomEndpointToggled,
                            enabled = !uiState.isSaving && !uiState.isTestingConnection
                        )
                    }

                    OutlinedButton(
                        onClick = onTestAiConnection,
                        enabled = !uiState.isSaving && !uiState.isTestingConnection,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isTestingConnection) "Đang kiểm tra kết nối AI..." else "Kiểm tra kết nối AI")
                    }

                    if (uiState.testConnectionResult != null) {
                        val isSuccess = uiState.isTestSuccess == true
                        val cardBg = if (isSuccess) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        }
                        val textColor = if (isSuccess) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBg, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.testConnectionResult,
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // Form cấu hình custom endpoint khi người dùng chọn bật
            if (uiState.isCustomEndpoint) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Thông số máy chủ tùy chỉnh:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = uiState.baseUrl,
                            onValueChange = onBaseUrlChanged,
                            label = { Text("Base URL máy chủ") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = uiState.model,
                            onValueChange = onModelChanged,
                            label = { Text("Tên mô hình (Model)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = uiState.apiKey,
                            onValueChange = onApiKeyChanged,
                            label = { Text("Khóa API (Để trống nếu proxy tự xác thực)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving,
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        OutlinedTextField(
                            value = uiState.maxTokens,
                            onValueChange = onMaxTokensChanged,
                            label = { Text("max_tokens (0 = Không giới hạn)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving,
                            singleLine = true
                        )

                        // Mục Provider tinh giản, kín đáo cho người dùng chuyên sâu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Giao thức: ${uiState.provider.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { onAdvancedExpandedChanged(!uiState.isAdvancedExpanded) }) {
                                Text(
                                    if (uiState.isAdvancedExpanded) "Đóng" else "Đổi loại",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        if (uiState.isAdvancedExpanded) {
                            ExposedDropdownMenuBox(
                                expanded = uiState.isProviderDropdownExpanded,
                                onExpandedChange = onDropdownExpandedChanged
                            ) {
                                OutlinedTextField(
                                    value = uiState.provider.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Loại Provider") },
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
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onUsePresetCloudSelected,
                                enabled = !uiState.isSaving,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Khôi phục mặc định")
                            }

                            Button(
                                onClick = onSaveAiSettings,
                                enabled = !uiState.isSaving,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (uiState.isSaving) "Đang lưu…" else "Lưu cấu hình")
                            }
                        }
                    }
                }
            }

            // --- MỤC 3: DỮ LIỆU & BỘ NHỚ ---
            Text("Dữ liệu & Bộ nhớ", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Sao lưu dữ liệu công thức và lịch sử nấu ăn an toàn ra tập tin để khôi phục hoặc chuyển máy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onBackupDatabase,
                        enabled = !uiState.isBackingUp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isBackingUp) "Đang sao lưu…" else "Sao lưu cơ sở dữ liệu")
                    }
                }
            }

            // --- MỤC 4: THÔNG TIN ỨNG DỤNG ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Sổ tay Nấu ăn · Cooking Note v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
