package com.cookingnote.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cookingnote.app.ui.local.LocalAppContainer
import com.cookingnote.app.ui.viewmodel.AppViewModelFactory
import com.cookingnote.app.ui.viewmodel.CreateRecipeUiState
import com.cookingnote.app.ui.viewmodel.CreateRecipeViewModel

/**
 * Stateful destination wrapper for creating or editing a recipe.
 */
@Composable
fun CreateRecipeScreen(
    recipeId: Long? = null,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateRecipeViewModel = viewModel(
        key = recipeId?.let { "create_recipe_$it" },
        factory = AppViewModelFactory(LocalAppContainer.current, recipeId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaveCompleted) {
        if (uiState.isSaveCompleted) {
            viewModel.resetSaveCompleted()
            onDone()
        }
    }

    CreateRecipeContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onPrepTimeChange = viewModel::onPrepTimeChange,
        onCookTimeChange = viewModel::onCookTimeChange,
        onServingsChange = viewModel::onServingsChange,
        onDifficultyChange = viewModel::onDifficultyChange,
        onNotesChange = viewModel::onNotesChange,
        onAddIngredient = viewModel::addIngredient,
        onUpdateIngredient = viewModel::updateIngredient,
        onRemoveIngredient = viewModel::removeIngredient,
        onAddStep = viewModel::addStep,
        onUpdateStep = viewModel::updateStep,
        onRemoveStep = viewModel::removeStep,
        onSaveRecipe = viewModel::saveRecipe,
        onDismissError = viewModel::clearError,
        onBack = onDone,
        modifier = modifier
    )
}

/**
 * Stateless UI rendering for creating or editing a recipe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeContent(
    uiState: CreateRecipeUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPrepTimeChange: (String) -> Unit,
    onCookTimeChange: (String) -> Unit,
    onServingsChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onAddIngredient: () -> Unit,
    onUpdateIngredient: (index: Int, name: String, amount: String, unit: String) -> Unit,
    onRemoveIngredient: (index: Int) -> Unit,
    onAddStep: () -> Unit,
    onUpdateStep: (index: Int, description: String) -> Unit,
    onRemoveStep: (index: Int) -> Unit,
    onSaveRecipe: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Sửa công thức" else "Thêm công thức") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.saveError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.saveError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onDismissError) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Đóng",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text("Tên món *") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.prepTime,
                        onValueChange = onPrepTimeChange,
                        label = { Text("Chuẩn bị (phút)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.cookTime,
                        onValueChange = onCookTimeChange,
                        label = { Text("Nấu (phút)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.servings,
                        onValueChange = onServingsChange,
                        label = { Text("Khẩu phần") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.difficulty,
                        onValueChange = onDifficultyChange,
                        label = { Text("Độ khó 1-3") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Nguyên liệu",
                    style = MaterialTheme.typography.titleMedium
                )

                uiState.ingredients.forEachIndexed { index, item ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { onUpdateIngredient(index, it, item.amount, item.unit) },
                            label = { Text("Tên") },
                            modifier = Modifier.weight(1.4f)
                        )
                        OutlinedTextField(
                            value = item.amount,
                            onValueChange = { onUpdateIngredient(index, item.name, it, item.unit) },
                            label = { Text("SL") },
                            modifier = Modifier.weight(0.7f)
                        )
                        OutlinedTextField(
                            value = item.unit,
                            onValueChange = { onUpdateIngredient(index, item.name, item.amount, it) },
                            label = { Text("ĐV") },
                            modifier = Modifier.weight(0.7f)
                        )
                        IconButton(onClick = { onRemoveIngredient(index) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Xóa nguyên liệu")
                        }
                    }
                }

                Button(onClick = onAddIngredient) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm nguyên liệu")
                }

                Text(
                    text = "Các bước",
                    style = MaterialTheme.typography.titleMedium
                )

                uiState.steps.forEachIndexed { index, stepDesc ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = stepDesc,
                            onValueChange = { onUpdateStep(index, it) },
                            label = { Text("Bước ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            minLines = 2
                        )
                        IconButton(onClick = { onRemoveStep(index) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Xóa bước")
                        }
                    }
                }

                Button(onClick = onAddStep) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm bước")
                }

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Ghi chú") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onSaveRecipe,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang lưu...")
                    } else {
                        Text(if (uiState.isEditMode) "Cập nhật công thức" else "Lưu công thức")
                    }
                }
            }
        }
    }
}
