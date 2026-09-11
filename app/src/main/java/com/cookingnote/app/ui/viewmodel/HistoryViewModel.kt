package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.entity.CookHistoryEntity
import com.cookingnote.app.data.entity.CookHistoryWithRecipe
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pre-formatted UI item representation of a cooking log entry.
 */
data class HistoryItemUiState(
    val id: Long,
    val recipeId: Long,
    val recipeName: String,
    val cookedAt: Long,
    val formattedDate: String,
    val note: String,
    val raw: CookHistoryWithRecipe
)

/**
 * UI State representation for the Cooking History screen.
 */
sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Content(
        val history: List<HistoryItemUiState>
    ) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

/**
 * ViewModel managing cooking history logs and date formatting.
 */
class HistoryViewModel(
    private val repository: CookbookRepository,
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.observeHistory()
        .map { list ->
            if (list.isEmpty()) {
                HistoryUiState.Empty
            } else {
                val items = list.map { entry ->
                    HistoryItemUiState(
                        id = entry.history.id,
                        recipeId = entry.recipe.id,
                        recipeName = entry.recipe.name,
                        cookedAt = entry.history.cookedAt,
                        formattedDate = dateFormatter.format(Date(entry.history.cookedAt)),
                        note = entry.history.note,
                        raw = entry
                    )
                }
                HistoryUiState.Content(history = items)
            }
        }
        .catch { throwable ->
            emit(HistoryUiState.Error(throwable.localizedMessage ?: "Đã xảy ra lỗi khi tải lịch sử nấu ăn"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState.Loading
        )
}
