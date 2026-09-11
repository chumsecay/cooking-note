package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.data.entity.CookHistoryEntity
import com.cookingnote.app.data.entity.CookHistoryWithRecipe
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.testutil.MainDispatcherRule
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
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun uiState_whenHistoryPresent_emitsContentWithFormattedDate() = runTest {
        val entry = CookHistoryWithRecipe(
            history = CookHistoryEntity(id = 1, recipeId = 10, cookedAt = 1700000000000L, note = "Ăn ngon"),
            recipe = RecipeEntity(id = 10, name = "Bún chả")
        )
        every { mockRepository.observeHistory() } returns flowOf(listOf(entry))

        val viewModel = HistoryViewModel(mockRepository)

        viewModel.uiState.test {
            val item = awaitItem()
            val content = if (item is HistoryUiState.Loading) awaitItem() else item
            assertTrue("Expected Content state", content is HistoryUiState.Content)
            val historyItems = (content as HistoryUiState.Content).history
            assertEquals(1, historyItems.size)
            assertEquals("Bún chả", historyItems[0].recipeName)
            assertEquals("Ăn ngon", historyItems[0].note)
            assertTrue(historyItems[0].formattedDate.isNotBlank())
        }
    }
}
