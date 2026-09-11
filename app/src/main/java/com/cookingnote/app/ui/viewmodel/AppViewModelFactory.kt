package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.cookingnote.app.data.AppContainer

/**
 * Central [ViewModelProvider.Factory] for the Cooking Note application.
 *
 * Provides manual dependency injection by wiring dependencies from [AppContainer]
 * to each dedicated [ViewModel] across all screen destinations.
 *
 * @param container The application-level manual DI container supplying repository,
 *                  AI service, settings store, user preferences, and database file access.
 * @param recipeId Optional recipe ID parameter required by screens/ViewModels operating
 *                 on a specific recipe (e.g., [DetailViewModel] and edit mode of [CreateRecipeViewModel]).
 */
class AppViewModelFactory(
    private val container: AppContainer,
    private val recipeId: Long? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(container.repository) as T

            modelClass.isAssignableFrom(LibraryViewModel::class.java) ->
                LibraryViewModel(container.repository) as T

            modelClass.isAssignableFrom(DetailViewModel::class.java) ->
                DetailViewModel(container.repository, recipeId ?: -1L) as T

            modelClass.isAssignableFrom(PantryViewModel::class.java) ->
                PantryViewModel(container.repository) as T

            modelClass.isAssignableFrom(FavoritesViewModel::class.java) ->
                FavoritesViewModel(container.repository) as T

            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(container.repository) as T

            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(container.repository) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.aiSettings, container.databaseFile()) as T

            modelClass.isAssignableFrom(AiViewModel::class.java) ->
                AiViewModel(container.aiService, container.aiSettings, container.repository) as T

            modelClass.isAssignableFrom(CreateRecipeViewModel::class.java) ->
                CreateRecipeViewModel(container.repository, recipeId) as T

            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}. " +
                    "Ensure the ViewModel is registered in AppViewModelFactory."
            )
        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        create(modelClass)
}
