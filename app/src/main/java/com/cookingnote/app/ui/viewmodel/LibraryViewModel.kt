package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.entity.CategoryEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State representation for the Library screen.
 */
sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Empty(
        val categories: List<CategoryEntity> = emptyList(),
        val selectedCategoryId: Long? = null
    ) : LibraryUiState
    data class Content(
        val categories: List<CategoryEntity>,
        val selectedCategoryId: Long?,
        val recipes: List<RecipeEntity>
    ) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

/**
 * ViewModel managing category filtering and catalog listing in the recipe library.
 */
class LibraryViewModel(
    private val repository: CookbookRepository
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeRecipes(),
        repository.observeCategories(),
        _selectedCategoryId
    ) { recipes, categories, selectedCategory ->
        val filtered = if (selectedCategory == null) {
            recipes
        } else {
            recipes.filter { it.categoryId == selectedCategory }
        }

        if (filtered.isEmpty()) {
            LibraryUiState.Empty(
                categories = categories,
                selectedCategoryId = selectedCategory
            )
        } else {
            LibraryUiState.Content(
                categories = categories,
                selectedCategoryId = selectedCategory,
                recipes = filtered
            )
        }
    }
    .catch { throwable ->
        emit(LibraryUiState.Error(throwable.localizedMessage ?: "Đã xảy ra lỗi khi tải danh mục thư viện"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState.Loading
    )

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleFavorite(recipeId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.setFavorite(recipeId, isFavorite)
            } catch (e: Exception)
            {
                // Background exception handled or logged; flow automatically updates
            }
        }
    }

    fun toggleFavorite(recipe: RecipeEntity) {
        toggleFavorite(recipe.id, !recipe.isFavorite)
    }
}
