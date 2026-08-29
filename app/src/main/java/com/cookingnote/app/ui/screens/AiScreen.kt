package com.cookingnote.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cookingnote.app.ai.AiSuggestion
import com.cookingnote.app.ui.local.LocalAppContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(onOpenRecipe: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val settings by container.aiSettings.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("Gợi ý món nhanh 15 phút") }
    var loading by remember { mutableStateOf(false) }
    val results = remember { mutableStateListOf<AiSuggestion>() }

    Scaffold(topBar = { TopAppBar(title = { Text("AI Gợi ý") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!container.aiService.isCloudConfigured) {
                Text(
                    "AI chưa cấu hình. Đang dùng gợi ý cục bộ từ thư viện.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            } else {
                Text(
                    "Provider: ${settings.provider.label} · model ${settings.model}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bạn muốn ăn gì?") },
                minLines = 2
            )
            Button(
                enabled = !loading && prompt.isNotBlank(),
                onClick = {
                    scope.launch {
                        loading = true
                        try {
                            val one = container.aiService.chat(prompt)
                            results.clear()
                            results.add(one)
                            container.repository.suggestFromPantry().take(3).forEach { recipe ->
                                results.add(
                                    AiSuggestion(
                                        title = recipe.name,
                                        summary = recipe.description,
                                        matchedRecipe = recipe,
                                        source = "pantry"
                                    )
                                )
                            }
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Đang nghĩ…" else "Gợi ý món")
            }
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (item.matchedRecipe != null) {
                                    Modifier.clickable { onOpenRecipe(item.matchedRecipe.id) }
                                } else Modifier
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.summary, style = MaterialTheme.typography.bodyMedium)
                            item.detail?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "Nguồn: ${item.source}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
