package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.entity.RecipeWithDetails
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State representation for the Recipe Detail screen.
 */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object Empty : DetailUiState
    data class Content(
        val recipeWithDetails: RecipeWithDetails
    ) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

/**
 * ViewModel managing details, favorites, deletion, and cooking marks for a specific recipe.
 */
class DetailViewModel(
    private val repository: CookbookRepository,
    val recipeId: Long
) : ViewModel() {

    val uiState: StateFlow<DetailUiState> = if (recipeId <= 0L) {
        flowOf<DetailUiState>(DetailUiState.Empty)
    } else {
        repository.observeRecipe(recipeId)
            .map { details ->
                if (details == null) {
                    DetailUiState.Empty
                } else {
                    DetailUiState.Content(recipeWithDetails = details)
                }
            }
            .catch { throwable ->
                emit(DetailUiState.Error(throwable.localizedMessage ?: "Đã xảy ra lỗi khi tải chi tiết công thức"))
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailUiState.Loading
    )

    fun toggleFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.setFavorite(recipeId, isFavorite)
            } catch (e: Exception) {
                // Handled
            }
        }
    }

    fun toggleFavorite() {
        val current = (uiState.value as? DetailUiState.Content)?.recipeWithDetails?.recipe?.isFavorite ?: return
        toggleFavorite(!current)
    }

    fun markCooked(note: String = "") {
        viewModelScope.launch {
            try {
                repository.markCooked(recipeId, note)
            } catch (e: Exception) {
                // Handled
            }
        }
    }

    fun deleteRecipe(onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteRecipe(recipeId)
                onDeleted()
            } catch (e: Exception) {
                // Handled
            }
        }
    }

    fun getShareableText(): String {
        val details = (uiState.value as? DetailUiState.Content)?.recipeWithDetails ?: return ""
        return buildShareText(details)
    }

    companion object {
        fun buildShareText(details: RecipeWithDetails): String = buildString {
            appendLine(details.recipe.name)
            if (details.recipe.description.isNotBlank()) {
                appendLine(details.recipe.description)
            }
            if (details.ingredients.isNotEmpty()) {
                appendLine("Nguyên liệu:")
                details.ingredients.forEach {
                    val amountStr = if (it.amount % 1.0 == 0.0) it.amount.toInt().toString() else it.amount.toString()
                    appendLine("- $amountStr ${it.unit} ${it.name}".trim())
                }
            }
            if (details.steps.isNotEmpty()) {
                appendLine("Các bước:")
                details.steps.sortedBy { it.stepNumber }.forEach {
                    appendLine("${it.stepNumber}. ${it.description}")
                }
            }
        }
    }
}
