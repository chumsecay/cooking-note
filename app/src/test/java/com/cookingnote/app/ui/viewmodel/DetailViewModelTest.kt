package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.RecipeWithDetails
import com.cookingnote.app.data.entity.StepEntity
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
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun uiState_whenRecipeIdInvalid_emitsEmpty() = runTest {
        val viewModel = DetailViewModel(mockRepository, recipeId = -1L)

        viewModel.uiState.test {
            val item = awaitItem()
            assertTrue("Negative id should emit Empty", item is DetailUiState.Empty)
        }
    }

    @Test
    fun uiState_whenRecipeFound_emitsContent() = runTest {
        val recipe = RecipeEntity(id = 5L, name = "Cơm chiên", isFavorite = true)
        val details = RecipeWithDetails(
            recipe = recipe,
            category = null,
            ingredients = listOf(IngredientEntity(recipeId = 5L, name = "Cơm", amount = 1.0, unit = "bát")),
            steps = listOf(StepEntity(recipeId = 5L, stepNumber = 1, description = "Chiên cơm vàng đều")),
            tags = emptyList()
        )

        every { mockRepository.observeRecipe(5L) } returns flowOf(details)

        val viewModel = DetailViewModel(mockRepository, recipeId = 5L)

        viewModel.uiState.test {
            val item = awaitItem()
            val content = if (item is DetailUiState.Loading) awaitItem() else item
            assertTrue("Expected Content state", content is DetailUiState.Content)
            assertEquals("Cơm chiên", (content as DetailUiState.Content).recipeWithDetails.recipe.name)
        }
    }

    @Test
    fun actions_delegateToRepository() = runTest {
        every { mockRepository.observeRecipe(5L) } returns flowOf(null)
        val viewModel = DetailViewModel(mockRepository, recipeId = 5L)

        viewModel.toggleFavorite(true)
        coVerify(exactly = 1) { mockRepository.setFavorite(5L, true) }

        viewModel.markCooked("Ngon")
        coVerify(exactly = 1) { mockRepository.markCooked(5L, "Ngon") }

        var deletedCalled = false
        viewModel.deleteRecipe(onDeleted = { deletedCalled = true })
        coVerify(exactly = 1) { mockRepository.deleteRecipe(5L) }
        assertTrue("onDeleted callback should be invoked", deletedCalled)
    }
}
