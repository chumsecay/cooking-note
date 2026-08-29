package com.cookingnote.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cookingnote.app.ui.local.LocalAppContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    recipeId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val container = LocalAppContainer.current
    val details by container.repository.observeRecipe(recipeId).collectAsState(null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(details?.recipe?.name ?: "Chi tiết") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val recipe = details?.recipe
                    if (recipe != null) {
                        IconButton(onClick = {
                            scope.launch {
                                container.repository.setFavorite(recipe.id, !recipe.isFavorite)
                            }
                        }) {
                            Icon(
                                if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Yêu thích"
                            )
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Sửa")
                        }
                        IconButton(onClick = {
                            val shareText = buildString {
                                appendLine(recipe.name)
                                appendLine(recipe.description)
                                appendLine("Nguyên liệu:")
                                details?.ingredients?.forEach {
                                    appendLine("- ${it.amount} ${it.unit} ${it.name}".trim())
                                }
                                appendLine("Các bước:")
                                details?.steps?.forEach {
                                    appendLine("${it.stepNumber}. ${it.description}")
                                }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Chia sẻ công thức"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Chia sẻ")
                        }
                        IconButton(onClick = {
                            scope.launch {
                                container.repository.deleteRecipe(recipe.id)
                                onBack()
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Xóa")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val data = details
        if (data == null) {
            Text("Đang tải…", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(data.recipe.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                "⏱ Chuẩn bị ${data.recipe.prepTime}′ · Nấu ${data.recipe.cookTime}′ · ${data.recipe.servings} phần · Độ khó ${data.recipe.difficulty}/3",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (data.category != null) {
                Text("Danh mục: ${data.category.name}", style = MaterialTheme.typography.labelMedium)
            }
            Text("Nguyên liệu", style = MaterialTheme.typography.titleLarge)
            data.ingredients.forEach { ing ->
                Text("• ${formatAmount(ing.amount)} ${ing.unit} ${ing.name}".trim())
            }
            Text("Cách làm", style = MaterialTheme.typography.titleLarge)
            data.steps.sortedBy { it.stepNumber }.forEach { step ->
                Text("${step.stepNumber}. ${step.description}")
            }
            if (data.recipe.notes.isNotBlank()) {
                Text("Ghi chú", style = MaterialTheme.typography.titleMedium)
                Text(data.recipe.notes)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            container.repository.markCooked(data.recipe.id)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Đã nấu hôm nay") }
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Sửa")
                }
            }
        }
    }
}

private fun formatAmount(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
