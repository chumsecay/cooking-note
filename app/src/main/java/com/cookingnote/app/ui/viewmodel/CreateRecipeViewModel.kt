package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.StepEntity
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Editable form item representation for an ingredient.
 */
data class IngredientFormItem(
    val name: String = "",
    val amount: String = "1",
    val unit: String = ""
)

/**
 * UI State for creating or editing a recipe.
 */
data class CreateRecipeUiState(
    val recipeId: Long? = null,
    val isEditMode: Boolean = false,
    val name: String = "",
    val description: String = "",
    val prepTime: String = "10",
    val cookTime: String = "20",
    val servings: String = "2",
    val difficulty: String = "1",
    val notes: String = "",
    val categoryId: Long? = null,
    val imageUri: String? = null,
    val ingredients: List<IngredientFormItem> = listOf(IngredientFormItem()),
    val steps: List<String> = listOf(""),
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveCompleted: Boolean = false,
    val savedRecipeId: Long? = null,
    val saveError: String? = null
) {
    val isFormValid: Boolean
        get() = name.isNotBlank()
}

/**
 * ViewModel managing the creation or editing of a recipe, including form validation,
 * entity mapping, and transactional saving.
 */
class CreateRecipeViewModel(
    private val repository: CookbookRepository,
    private val recipeId: Long? = null
) : ViewModel() {

    private var existingRecipe: RecipeEntity? = null

    private val _uiState = MutableStateFlow(
        CreateRecipeUiState(
            recipeId = recipeId,
            isEditMode = recipeId != null && recipeId > 0
        )
    )
    val uiState: StateFlow<CreateRecipeUiState> = _uiState.asStateFlow()

    init {
        if (recipeId != null && recipeId > 0) {
            loadRecipe(recipeId)
        }
    }

    private fun loadRecipe(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val details = repository.getRecipe(id)
                if (details != null) {
                    existingRecipe = details.recipe
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            name = details.recipe.name,
                            description = details.recipe.description,
                            prepTime = details.recipe.prepTime.toString(),
                            cookTime = details.recipe.cookTime.toString(),
                            servings = details.recipe.servings.toString(),
                            difficulty = details.recipe.difficulty.toString(),
                            notes = details.recipe.notes,
                            categoryId = details.recipe.categoryId,
                            imageUri = details.recipe.imageUri,
                            ingredients = if (details.ingredients.isEmpty()) {
                                listOf(IngredientFormItem())
                            } else {
                                details.ingredients.sortedBy { it.sortOrder }.map { ing ->
                                    val formattedAmount = if (ing.amount % 1.0 == 0.0) {
                                        ing.amount.toInt().toString()
                                    } else {
                                        ing.amount.toString()
                                    }
                                    IngredientFormItem(
                                        name = ing.name,
                                        amount = formattedAmount,
                                        unit = ing.unit
                                    )
                                }
                            },
                            steps = if (details.steps.isEmpty()) {
                                listOf("")
                            } else {
                                details.steps.sortedBy { it.stepNumber }.map { it.description }
                            }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, saveError = "Không tìm thấy công thức #$id")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, saveError = e.message ?: "Lỗi tải công thức")
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update {
            it.copy(
                name = newName,
                nameError = if (newName.isNotBlank()) null else it.nameError
            )
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onPrepTimeChange(newPrepTime: String) {
        _uiState.update { it.copy(prepTime = newPrepTime) }
    }

    fun onCookTimeChange(newCookTime: String) {
        _uiState.update { it.copy(cookTime = newCookTime) }
    }

    fun onServingsChange(newServings: String) {
        _uiState.update { it.copy(servings = newServings) }
    }

    fun onDifficultyChange(newDifficulty: String) {
        _uiState.update { it.copy(difficulty = newDifficulty) }
    }

    fun onNotesChange(newNotes: String) {
        _uiState.update { it.copy(notes = newNotes) }
    }

    fun onCategoryIdChange(newCategoryId: Long?) {
        _uiState.update { it.copy(categoryId = newCategoryId) }
    }

    fun onImageUriChange(newImageUri: String?) {
        _uiState.update { it.copy(imageUri = newImageUri) }
    }

    fun addIngredient() {
        _uiState.update { state ->
            state.copy(ingredients = state.ingredients + IngredientFormItem())
        }
    }

    fun updateIngredient(index: Int, name: String, amount: String, unit: String) {
        _uiState.update { state ->
            val updated = state.ingredients.toMutableList()
            if (index in updated.indices) {
                updated[index] = IngredientFormItem(name = name, amount = amount, unit = unit)
            }
            state.copy(ingredients = updated)
        }
    }

    fun removeIngredient(index: Int) {
        _uiState.update { state ->
            if (state.ingredients.size <= 1) {
                state.copy(ingredients = listOf(IngredientFormItem()))
            } else {
                val updated = state.ingredients.toMutableList()
                if (index in updated.indices) {
                    updated.removeAt(index)
                }
                state.copy(ingredients = updated)
            }
        }
    }

    fun addStep() {
        _uiState.update { state ->
            state.copy(steps = state.steps + "")
        }
    }

    fun updateStep(index: Int, description: String) {
        _uiState.update { state ->
            val updated = state.steps.toMutableList()
            if (index in updated.indices) {
                updated[index] = description
            }
            state.copy(steps = updated)
        }
    }

    fun removeStep(index: Int) {
        _uiState.update { state ->
            if (state.steps.size <= 1) {
                state.copy(steps = listOf(""))
            } else {
                val updated = state.steps.toMutableList()
                if (index in updated.indices) {
                    updated.removeAt(index)
                }
                state.copy(steps = updated)
            }
        }
    }

    fun saveRecipe() {
        val currentState = _uiState.value
        val trimmedName = currentState.name.trim()

        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(nameError = "Tên món không được để trống") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val isEdit = currentState.isEditMode && currentState.recipeId != null && currentState.recipeId > 0
                val targetId = if (isEdit) currentState.recipeId!! else 0L

                val recipe = RecipeEntity(
                    id = targetId,
                    name = trimmedName,
                    description = currentState.description.trim(),
                    categoryId = currentState.categoryId ?: existingRecipe?.categoryId,
                    prepTime = currentState.prepTime.toIntOrNull() ?: 0,
                    cookTime = currentState.cookTime.toIntOrNull() ?: 0,
                    servings = currentState.servings.toIntOrNull() ?: 2,
                    difficulty = (currentState.difficulty.toIntOrNull() ?: 1).coerceIn(1, 3),
                    imageUri = currentState.imageUri ?: existingRecipe?.imageUri,
                    isFavorite = existingRecipe?.isFavorite ?: false,
                    notes = currentState.notes.trim(),
                    createdAt = existingRecipe?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastCookedAt = existingRecipe?.lastCookedAt
                )

                val ingredientEntities = currentState.ingredients
                    .filter { it.name.isNotBlank() }
                    .mapIndexed { index, item ->
                        IngredientEntity(
                            recipeId = targetId,
                            name = item.name.trim(),
                            amount = item.amount.toDoubleOrNull() ?: 0.0,
                            unit = item.unit.trim(),
                            sortOrder = index
                        )
                    }

                val stepEntities = currentState.steps
                    .filter { it.isNotBlank() }
                    .mapIndexed { index, desc ->
                        StepEntity(
                            recipeId = targetId,
                            stepNumber = index + 1,
                            description = desc.trim()
                        )
                    }

                val savedId = repository.saveRecipe(recipe, ingredientEntities, stepEntities)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaveCompleted = true,
                        savedRecipeId = savedId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = e.message ?: "Không thể lưu công thức"
                    )
                }
            }
        }
    }

    fun resetSaveCompleted() {
        _uiState.update { it.copy(isSaveCompleted = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(saveError = null) }
    }
}
