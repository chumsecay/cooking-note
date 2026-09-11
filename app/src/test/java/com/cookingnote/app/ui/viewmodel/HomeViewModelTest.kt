package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.data.dao.RecipeStats
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun uiState_whenRepositoryDataPresent_emitsContentState() = runTest {
        val stats = RecipeStats(total = 5, favorites = 2)
        val today = listOf(RecipeEntity(id = 1, name = "Phở Bò"))
        val favorites = listOf(RecipeEntity(id = 1, name = "Phở Bò", isFavorite = true))

        every { mockRepository.observeStats() } returns flowOf(stats)
        every { mockRepository.observeTodaysPick(3) } returns flowOf(today)
        every { mockRepository.observeFavorites() } returns flowOf(favorites)

        val viewModel = HomeViewModel(mockRepository)

        viewModel.uiState.test {
            val item = awaitItem()
            // May start with initial Loading or directly Content with UnconfinedTestDispatcher
            if (item is HomeUiState.Loading) {
                val next = awaitItem()
                assertTrue("Expected Content state", next is HomeUiState.Content)
                val content = next as HomeUiState.Content
                assertEquals(5, content.stats.total)
                assertEquals(1, content.todaysPicks.size)
            } else {
                assertTrue("Expected Content state", item is HomeUiState.Content)
                val content = item as HomeUiState.Content
                assertEquals(5, content.stats.total)
                assertEquals(1, content.todaysPicks.size)
            }
        }
    }

    @Test
    fun uiState_whenEmptyData_emitsEmptyState() = runTest {
        every { mockRepository.observeStats() } returns flowOf(RecipeStats(total = 0, favorites = 0))
        every { mockRepository.observeTodaysPick(3) } returns flowOf(emptyList())
        every { mockRepository.observeFavorites() } returns flowOf(emptyList())

        val viewModel = HomeViewModel(mockRepository)

        viewModel.uiState.test {
            val item = awaitItem()
            if (item is HomeUiState.Loading) {
                val next = awaitItem()
                assertTrue("Expected Empty state", next is HomeUiState.Empty)
            } else {
                assertTrue("Expected Empty state", item is HomeUiState.Empty)
            }
        }
    }

    @Test
    fun uiState_whenFlowThrows_emitsErrorState() = runTest {
        every { mockRepository.observeStats() } returns flow { error("Database crash") }
        every { mockRepository.observeTodaysPick(3) } returns flowOf(emptyList())
        every { mockRepository.observeFavorites() } returns flowOf(emptyList())

        val viewModel = HomeViewModel(mockRepository)

        viewModel.uiState.test {
            val item = awaitItem()
            if (item is HomeUiState.Loading) {
                val next = awaitItem()
                assertTrue("Expected Error state", next is HomeUiState.Error)
            } else {
                assertTrue("Expected Error state", item is HomeUiState.Error)
            }
        }
    }

    @Test
    fun toggleFavorite_delegatesToRepository() = runTest {
        every { mockRepository.observeStats() } returns flowOf(RecipeStats(0, 0))
        every { mockRepository.observeTodaysPick(3) } returns flowOf(emptyList())
        every { mockRepository.observeFavorites() } returns flowOf(emptyList())

        val viewModel = HomeViewModel(mockRepository)
        viewModel.toggleFavorite(10L, true)

        coVerify(exactly = 1) { mockRepository.setFavorite(10L, true) }
    }
}
