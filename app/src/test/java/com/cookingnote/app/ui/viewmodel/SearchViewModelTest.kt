package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun initialState_showsEmptyPrompt() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(mockRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isEmptyPrompt)
            assertEquals("", state.query)
            assertTrue(state.isInitial)
        }
    }

    @Test
    fun onQueryChange_updatesQueryAndExecutesSearchAfterDebounce() = runTest(testDispatcher) {
        val searchResults = listOf(RecipeEntity(id = 1, name = "Phở Bò"))
        every { mockRepository.searchRecipes("Phở") } returns flowOf(searchResults)

        val viewModel = SearchViewModel(mockRepository)

        viewModel.uiState.test {
            awaitItem()

            viewModel.onQueryChange("Phở")
            assertEquals("Phở", viewModel.searchQuery.value)

            advanceTimeBy(350)
            runCurrent()

            val state = expectMostRecentItem()
            assertEquals("Phở", state.query)
            assertEquals(1, state.results.size)
            assertEquals("Phở Bò", state.results[0].name)
            assertFalse(state.isInitial)
            assertTrue(state.hasResults)
        }
    }

    @Test
    fun clearQuery_resetsToEmptyPrompt() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(mockRepository)

        viewModel.uiState.test {
            awaitItem()

            viewModel.onQueryChange("Bún")
            advanceTimeBy(350)
            runCurrent()

            viewModel.clearQuery()
            advanceTimeBy(50)
            runCurrent()

            val state = expectMostRecentItem()
            assertEquals("", viewModel.searchQuery.value)
            assertTrue(state.isEmptyPrompt)
        }
    }
}
