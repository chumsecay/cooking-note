package com.cookingnote.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cookingnote.app.ui.local.LocalAppContainer
import com.cookingnote.app.ui.viewmodel.AppViewModelFactory
import com.cookingnote.app.ui.viewmodel.DetailUiState
import com.cookingnote.app.ui.viewmodel.DetailViewModel

/**
 * Stateful Recipe Detail screen composable.
 * Wires [DetailViewModel], manages sharing intent side-effect, and handles recipe actions.
 */
@Composable
fun DetailScreen(
    recipeId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = viewModel(
        key = "detail_$recipeId",
        factory = AppViewModelFactory(LocalAppContainer.current, recipeId = recipeId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DetailContent(
        uiState = uiState,
        onBack = onBack,
        onEdit = onEdit,
        onToggleFavorite = { viewModel.toggleFavorite() },
        onMarkCooked = { viewModel.markCooked() },
        onDeleteRecipe = {
            viewModel.deleteRecipe(onDeleted = onBack)
        },
        onShare = {
            val shareText = viewModel.getShareableText()
            if (shareText.isNotBlank()) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "Chia sẻ công thức"))
            }
        },
        modifier = modifier
    )
}

/**
 * Stateless Recipe Detail content composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    uiState: DetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMarkCooked: () -> Unit,
    onDeleteRecipe: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState) {
                            is DetailUiState.Content -> uiState.recipeWithDetails.recipe.name
                            else -> "Chi tiết"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is DetailUiState.Content) {
                        val recipe = uiState.recipeWithDetails.recipe
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Yêu thích",
                                tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Sửa")
                        }
                        IconButton(onClick = onShare) {
                            Icon(Icons.Filled.Share, contentDescription = "Chia sẻ")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Xóa")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onBack) {
                            Text("Quay lại")
                        }
                    }
                }
            }

            is DetailUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Không tìm thấy công thức hoặc đã bị xóa.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onBack) {
                            Text("Quay lại")
                        }
                    }
                }
            }

            is DetailUiState.Content -> {
                val data = uiState.recipeWithDetails
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (data.recipe.description.isNotBlank()) {
                        Text(data.recipe.description, style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(
                        text = "⏱ Chuẩn bị ${data.recipe.prepTime}′ · Nấu ${data.recipe.cookTime}′ · ${data.recipe.servings} phần · Độ khó ${data.recipe.difficulty}/3",
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
                            onClick = onMarkCooked,
                            modifier = Modifier.weight(1f)
                        ) { Text("Đã nấu hôm nay") }
                        OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                            Text("Sửa")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa công thức") },
            text = { Text("Bạn có chắc chắn muốn xóa công thức này? Thao tác này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteRecipe()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

private fun formatAmount(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
