package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun uiState_whenFavoritesPresent_emitsContent() = runTest {
        val list = listOf(RecipeEntity(id = 1, name = "Phở", isFavorite = true))
        every { mockRepository.observeFavorites() } returns flowOf(list)

        val viewModel = FavoritesViewModel(mockRepository)

        viewModel.uiState.test {
            val item = awaitItem()
            val content = if (item is FavoritesUiState.Loading) awaitItem() else item
            assertTrue("Expected Content", content is FavoritesUiState.Content)
            assertEquals(1, (content as FavoritesUiState.Content).favorites.size)
        }
    }

    @Test
    fun removeFavorite_delegatesToRepository() = runTest {
        every { mockRepository.observeFavorites() } returns flowOf(emptyList())
        val viewModel = FavoritesViewModel(mockRepository)

        viewModel.removeFavorite(42L)
        coVerify(exactly = 1) { mockRepository.setFavorite(42L, false) }
    }
}
