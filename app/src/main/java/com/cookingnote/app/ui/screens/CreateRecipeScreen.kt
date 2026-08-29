package com.cookingnote.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.StepEntity
import com.cookingnote.app.ui.local.LocalAppContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    recipeId: Long? = null,
    onDone: () -> Unit
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("10") }
    var cookTime by remember { mutableStateOf("20") }
    var servings by remember { mutableStateOf("2") }
    var difficulty by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf("" to ("1" to "")) }
    val steps = remember { mutableStateListOf("") }

    LaunchedEffect(recipeId) {
        if (recipeId != null && recipeId > 0) {
            val details = container.repository.getRecipe(recipeId) ?: return@LaunchedEffect
            name = details.recipe.name
            description = details.recipe.description
            prepTime = details.recipe.prepTime.toString()
            cookTime = details.recipe.cookTime.toString()
            servings = details.recipe.servings.toString()
            difficulty = details.recipe.difficulty.toString()
            notes = details.recipe.notes
            ingredients.clear()
            if (details.ingredients.isEmpty()) ingredients.add("" to ("1" to ""))
            else details.ingredients.forEach {
                ingredients.add(it.name to (it.amount.toString() to it.unit))
            }
            steps.clear()
            if (details.steps.isEmpty()) steps.add("")
            else details.steps.sortedBy { it.stepNumber }.forEach { steps.add(it.description) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recipeId != null && recipeId > 0) "Sửa công thức" else "Thêm công thức") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên món") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = prepTime, onValueChange = { prepTime = it }, label = { Text("Chuẩn bị (phút)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = cookTime, onValueChange = { cookTime = it }, label = { Text("Nấu (phút)") }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = servings, onValueChange = { servings = it }, label = { Text("Khẩu phần") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = difficulty, onValueChange = { difficulty = it }, label = { Text("Độ khó 1-3") }, modifier = Modifier.weight(1f))
            }
            Text("Nguyên liệu")
            ingredients.forEachIndexed { index, triple ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = triple.first,
                        onValueChange = { ingredients[index] = it to triple.second },
                        label = { Text("Tên") },
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = triple.second.first,
                        onValueChange = { ingredients[index] = triple.first to (it to triple.second.second) },
                        label = { Text("SL") },
                        modifier = Modifier.weight(0.7f)
                    )
                    OutlinedTextField(
                        value = triple.second.second,
                        onValueChange = { ingredients[index] = triple.first to (triple.second.first to it) },
                        label = { Text("ĐV") },
                        modifier = Modifier.weight(0.7f)
                    )
                    IconButton(onClick = { if (ingredients.size > 1) ingredients.removeAt(index) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Xóa")
                    }
                }
            }
            Button(onClick = { ingredients.add("" to ("1" to "")) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(" Thêm nguyên liệu")
            }
            Text("Các bước")
            steps.forEachIndexed { index, value ->
                Row {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { steps[index] = it },
                        label = { Text("Bước ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        minLines = 2
                    )
                    IconButton(onClick = { if (steps.size > 1) steps.removeAt(index) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Xóa")
                    }
                }
            }
            Button(onClick = { steps.add("") }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(" Thêm bước")
            }
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    scope.launch {
                        val existing = if (recipeId != null && recipeId > 0) {
                            container.repository.getRecipe(recipeId)?.recipe
                        } else null
                        val recipe = RecipeEntity(
                            id = existing?.id ?: 0,
                            name = name.trim(),
                            description = description.trim(),
                            categoryId = existing?.categoryId,
                            prepTime = prepTime.toIntOrNull() ?: 0,
                            cookTime = cookTime.toIntOrNull() ?: 0,
                            servings = servings.toIntOrNull() ?: 2,
                            difficulty = (difficulty.toIntOrNull() ?: 1).coerceIn(1, 3),
                            imageUri = existing?.imageUri,
                            isFavorite = existing?.isFavorite ?: false,
                            notes = notes.trim(),
                            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            lastCookedAt = existing?.lastCookedAt
                        )
                        val ings = ingredients
                            .filter { it.first.isNotBlank() }
                            .mapIndexed { index, (n, amountUnit) ->
                                IngredientEntity(
                                    recipeId = recipe.id,
                                    name = n.trim(),
                                    amount = amountUnit.first.toDoubleOrNull() ?: 0.0,
                                    unit = amountUnit.second.trim(),
                                    sortOrder = index
                                )
                            }
                        val stepEntities = steps
                            .filter { it.isNotBlank() }
                            .mapIndexed { index, text ->
                                StepEntity(
                                    recipeId = recipe.id,
                                    stepNumber = index + 1,
                                    description = text.trim()
                                )
                            }
                        container.repository.saveRecipe(recipe, ings, stepEntities)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lưu công thức")
            }
        }
    }
}
