package com.cookingnote.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cookingnote.app.data.entity.ChatMessageEntity
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.ui.local.LocalAppContainer
import kotlinx.coroutines.launch

private data class QuickChip(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val prompt: String
)

private val quickChips = listOf(
    QuickChip("Từ tủ lạnh", Icons.Filled.Kitchen,
        "Dựa trên tủ lạnh hiện tại, gợi ý 3 món tôi có thể nấu ngay. Mỗi món 2-3 dòng."),
    QuickChip("Món nhanh 15 phút", Icons.Filled.Timer,
        "Gợi ý 5 món Việt nấu trong 15 phút, đơn giản, nguyên liệu dễ mua."),
    QuickChip("Món chay", Icons.Filled.Eco,
        "Gợi ý 5 món chay ngon, dễ nấu, phù hợp bữa cơm gia đình."),
    QuickChip("Món cay", Icons.Filled.LocalFireDepartment,
        "Gợi ý 5 món Việt cay, có thể làm tại nhà, kèm nguyên liệu chính.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(onOpenRecipe: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val settings by container.aiSettings.settings.collectAsState()
    val messages by container.repository.observeChat()
        .collectAsState(emptyList())
    val pantry by container.repository.observePantry()
        .collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    val isCloud = container.aiService.isCloudConfigured
    val keyRequired = settings.provider != com.cookingnote.app.data.prefs.AiProviderType.RULE_BASED &&
        settings.provider.requiresApiKey

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trợ lý nấu ăn") },
                actions = {
                    IconButton(
                        onClick = { scope.launch { container.repository.clearChat() } },
                        enabled = messages.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Xóa lịch sử")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isCloud && keyRequired) {
                StatusBanner("AI chưa cấu hình. Vào Cài đặt → chọn provider → nhập API key.")
            } else if (!isCloud) {
                StatusBanner("Đang dùng gợi ý cục bộ (rule-based).")
            } else {
                StatusBanner("Đã nhớ: ${messages.size} tin nhắn · AI thấy tủ lạnh: ${pantry.size} món.")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickChips.forEach { chip ->
                    AssistChip(
                        onClick = {
                            input = chip.prompt
                            scope.launch { sendToAi(container, chip.prompt, messages, onBusy = { busy = it }) }
                        },
                        label = { Text(chip.label, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                chip.icon,
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item { EmptyState() }
                }
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        onOpenRecipe = onOpenRecipe
                    )
                }
                if (busy) {
                    item { TypingBubble() }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Hỏi trợ lý…") },
                    enabled = !busy,
                    maxLines = 4
                )
                IconButton(
                    enabled = !busy && input.isNotBlank(),
                    onClick = {
                        val text = input.trim()
                        input = ""
                        scope.launch {
                            sendToAi(
                                container,
                                text,
                                messages,
                                onBusy = { busy = it },
                                onUserSend = { container.repository.appendMessage("user", it) }
                            )
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi")
                }
            }
        }
    }
}

private suspend fun sendToAi(
    container: com.cookingnote.app.data.AppContainer,
    prompt: String,
    current: List<ChatMessageEntity>,
    onBusy: (Boolean) -> Unit,
    onUserSend: (suspend (String) -> Unit)? = null
) {
    onBusy(true)
    try {
        if (onUserSend != null) onUserSend(prompt)
        val history = current.takeLast(20).map { it.role to it.content }
        val suggestion = container.aiService.chat(prompt, history)
        container.repository.appendMessage(
            "assistant",
            suggestion.detail ?: suggestion.summary
        )
    } catch (e: Exception) {
        container.repository.appendMessage(
            "assistant",
            "Lỗi: ${e.message ?: "không xác định"}"
        )
    } finally {
        onBusy(false)
    }
}

@Composable
private fun StatusBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Restaurant,
            contentDescription = null,
            modifier = Modifier.padding(8.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text("Trợ lý nấu ăn sẵn sàng.", style = MaterialTheme.typography.titleMedium)
        Text(
            "Chọn chip nhanh phía trên, hoặc gõ câu hỏi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessageEntity, onOpenRecipe: (Long) -> Unit) {
    val isUser = message.role == ChatMessageEntity.ROLE_USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bg = if (isUser) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isUser) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(
                    color = bg,
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )
        }
        Text(
            text = if (isUser) "Bạn" else "Trợ lý",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
    // Placeholder hook for tapping recipes (kept for future expansion)
    @Suppress("UNUSED_PARAMETER") val hint = onOpenRecipe
}

@Composable
private fun TypingBubble() {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            "Trợ lý đang nghĩ…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Suppress("unused")
private fun unused(p: List<PantryItemEntity>) = p.size
