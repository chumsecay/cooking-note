package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * UI State for the Recipe Search screen.
 */
data class SearchUiState(
    val query: String = "",
    val results: List<RecipeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isInitial: Boolean = true
) {
    /**
     * Displayed when the user has not entered a query yet.
     */
    val isEmptyPrompt: Boolean
        get() = isInitial || query.isBlank()

    /**
     * Displayed when a search was performed but returned 0 results.
     */
    val isNoResults: Boolean
        get() = !isInitial && query.isNotBlank() && !isLoading && results.isEmpty() && errorMessage == null

    /**
     * True when valid recipe search results are available.
     */
    val hasResults: Boolean
        get() = !isLoading && results.isNotEmpty()
}

/**
 * Internal helper representing the asynchronous status of a search query execution.
 */
private sealed interface SearchResultStatus {
    data object Initial : SearchResultStatus
    data object Loading : SearchResultStatus
    data class Success(val recipes: List<RecipeEntity>) : SearchResultStatus
    data class Error(val message: String) : SearchResultStatus
}

/**
 * ViewModel managing recipe searches with 300ms query debouncing, instant UI query updates,
 * and distinct empty vs no-results states.
 */
class SearchViewModel(
    private val repository: CookbookRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val searchExecutionFlow: Flow<SearchResultStatus> = _query
        .debounce { q -> if (q.isBlank()) 0L else 300L }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(SearchResultStatus.Initial)
            } else {
                repository.searchRecipes(query)
                    .map<List<RecipeEntity>, SearchResultStatus> { recipes ->
                        SearchResultStatus.Success(recipes)
                    }
                    .onStart {
                        emit(SearchResultStatus.Loading)
                    }
                    .catch { throwable ->
                        emit(SearchResultStatus.Error(throwable.message ?: "Lỗi khi tìm kiếm công thức"))
                    }
            }
        }

    val uiState: StateFlow<SearchUiState> = combine(_query, searchExecutionFlow) { query, status ->
        when (status) {
            is SearchResultStatus.Initial -> SearchUiState(
                query = query,
                results = emptyList(),
                isLoading = false,
                errorMessage = null,
                isInitial = true
            )
            is SearchResultStatus.Loading -> SearchUiState(
                query = query,
                results = emptyList(),
                isLoading = true,
                errorMessage = null,
                isInitial = false
            )
            is SearchResultStatus.Success -> SearchUiState(
                query = query,
                results = status.recipes,
                isLoading = false,
                errorMessage = null,
                isInitial = false
            )
            is SearchResultStatus.Error -> SearchUiState(
                query = query,
                results = emptyList(),
                isLoading = false,
                errorMessage = status.message,
                isInitial = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(query = "", isInitial = true)
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }
}
