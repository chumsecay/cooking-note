package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State representation for the Favorites screen.
 */
sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Content(
        val favorites: List<RecipeEntity>
    ) : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}

/**
 * ViewModel managing favorite recipes collection and removals.
 */
class FavoritesViewModel(
    private val repository: CookbookRepository
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = repository.observeFavorites()
        .map { favorites ->
            if (favorites.isEmpty()) {
                FavoritesUiState.Empty
            } else {
                FavoritesUiState.Content(favorites = favorites)
            }
        }
        .catch { throwable ->
            emit(FavoritesUiState.Error(throwable.localizedMessage ?: "Đã xảy ra lỗi khi tải danh sách yêu thích"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading
        )

    fun removeFavorite(recipeId: Long) {
        viewModelScope.launch {
            try {
                repository.setFavorite(recipeId, false)
            } catch (e: Exception) {
                // Error handling; flow update triggers recomposition
            }
        }
    }

    fun removeFavorite(recipe: RecipeEntity) {
        removeFavorite(recipe.id)
    }

    fun toggleFavorite(recipeId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.setFavorite(recipeId, isFavorite)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }
}
