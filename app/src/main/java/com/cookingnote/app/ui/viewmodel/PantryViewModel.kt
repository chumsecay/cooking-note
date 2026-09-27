package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.remote.model.SyncStatus
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state representation for the Pantry (Tủ lạnh / Nguyên liệu) screen.
 */
data class PantryUiState(
    val items: List<PantryItemEntity> = emptyList(),
    val lowStockItems: List<PantryItemEntity> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val syncStatus: SyncStatus = SyncStatus.Synced,
    val recommendations: List<com.cookingnote.app.data.remote.dto.RecipeMatchDto> = emptyList(),
    val isLoadingRecommendations: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && items.isEmpty()

    val hasLowStock: Boolean
        get() = lowStockItems.isNotEmpty()
}

private data class PantryBaseData(
    val items: List<PantryItemEntity>,
    val lowStock: List<PantryItemEntity>,
    val isDialogVisible: Boolean,
    val error: String?
)

/**
 * ViewModel managing pantry inventory, low stock monitoring, and item mutation.
 */
class PantryViewModel(
    private val repository: CookbookRepository
) : ViewModel() {

    private val _isAddDialogVisible = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _recommendations = MutableStateFlow<List<com.cookingnote.app.data.remote.dto.RecipeMatchDto>>(emptyList())
    private val _isLoadingRecommendations = MutableStateFlow(false)

    val uiState: StateFlow<PantryUiState> = combine(
        combine(
            repository.observePantry(),
            repository.observeLowStock(),
            _isAddDialogVisible,
            _errorMessage
        ) { items, lowStock, isDialogVisible, error ->
            PantryBaseData(items, lowStock, isDialogVisible, error)
        },
        combine(
            repository.observeSyncStatus().onStart { emit(SyncStatus.Synced) },
            _recommendations,
            _isLoadingRecommendations
        ) { syncStatus, recs, loadingRecs ->
            Triple(syncStatus, recs, loadingRecs)
        }
    ) { base, (syncStatus, recs, loadingRecs) ->
        PantryUiState(
            items = base.items,
            lowStockItems = base.lowStock,
            isAddDialogVisible = base.isDialogVisible,
            isLoading = false,
            errorMessage = base.error,
            syncStatus = syncStatus,
            recommendations = recs,
            isLoadingRecommendations = loadingRecs
        )
    }.catch { throwable ->
        emit(
            PantryUiState(
                isLoading = false,
                errorMessage = throwable.message ?: "Lỗi khi tải dữ liệu tủ lạnh"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PantryUiState(isLoading = true)
    )

    fun showAddDialog() {
        _isAddDialogVisible.value = true
    }

    fun dismissAddDialog() {
        _isAddDialogVisible.value = false
    }

    fun addItem(name: String, amount: Double, unit: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            try {
                repository.upsertPantryItem(
                    PantryItemEntity(
                        name = trimmedName,
                        amount = amount,
                        unit = unit.trim()
                    )
                )
                _isAddDialogVisible.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Không thể thêm nguyên liệu"
            }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            try {
                repository.deletePantryItem(id)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Không thể xóa nguyên liệu"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadSmartRecommendations() {
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            repository.getSmartRecommendations(limit = 20)
                .onSuccess { recs ->
                    _recommendations.value = recs
                    _isLoadingRecommendations.value = false
                }
                .onFailure {
                    _isLoadingRecommendations.value = false
                }
        }
    }
}
