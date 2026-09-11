package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.dao.RecipeStats
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State representation for the Home screen.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Content(
        val stats: RecipeStats,
        val todaysPicks: List<RecipeEntity>,
        val favorites: List<RecipeEntity>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * ViewModel managing data feeds for the Home catalog dashboard.
 */
class HomeViewModel(
    private val repository: CookbookRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeStats(),
        repository.observeTodaysPick(3),
        repository.observeFavorites()
    ) { stats, todaysPicks, favorites ->
        if (stats.total == 0 && todaysPicks.isEmpty() && favorites.isEmpty()) {
            HomeUiState.Empty
        } else {
            HomeUiState.Content(
                stats = stats,
                todaysPicks = todaysPicks,
                favorites = favorites
            )
        }
    }
    .catch { throwable ->
        emit(HomeUiState.Error(throwable.localizedMessage ?: "Đã xảy ra lỗi khi tải dữ liệu trang chủ"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading
    )

    fun toggleFavorite(recipeId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.setFavorite(recipeId, isFavorite)
            } catch (e: Exception) {
                // Background exception handled or logged; flow automatically updates
            }
        }
    }

    fun toggleFavorite(recipe: RecipeEntity) {
        toggleFavorite(recipe.id, !recipe.isFavorite)
    }
}
