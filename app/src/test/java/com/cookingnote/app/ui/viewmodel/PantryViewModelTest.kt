package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PantryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun uiState_reflectsPantryAndLowStockItems() = runTest {
        val items = listOf(PantryItemEntity(id = 1, name = "Trứng", amount = 10.0, unit = "quả"))
        val lowStock = listOf(PantryItemEntity(id = 2, name = "Đường", amount = 50.0, unit = "g"))

        every { mockRepository.observePantry() } returns flowOf(items)
        every { mockRepository.observeLowStock() } returns flowOf(lowStock)

        val viewModel = PantryViewModel(mockRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            val actual = if (state.isLoading) awaitItem() else state
            assertEquals(1, actual.items.size)
            assertEquals("Trứng", actual.items[0].name)
            assertEquals(1, actual.lowStockItems.size)
            assertTrue(actual.hasLowStock)
            assertFalse(actual.isEmpty)
        }
    }

    @Test
    fun dialogControls_toggleAddDialogVisibility() = runTest {
        every { mockRepository.observePantry() } returns flowOf(emptyList())
        every { mockRepository.observeLowStock() } returns flowOf(emptyList())

        val viewModel = PantryViewModel(mockRepository)

        viewModel.uiState.test {
            val initial = awaitItem()
            val state = if (initial.isLoading) awaitItem() else initial
            assertFalse(state.isAddDialogVisible)

            viewModel.showAddDialog()
            val showing = awaitItem()
            assertTrue(showing.isAddDialogVisible)

            viewModel.dismissAddDialog()
            val dismissed = awaitItem()
            assertFalse(dismissed.isAddDialogVisible)
        }
    }

    @Test
    fun addItem_callsRepositoryAndClosesDialog() = runTest {
        every { mockRepository.observePantry() } returns flowOf(emptyList())
        every { mockRepository.observeLowStock() } returns flowOf(emptyList())
        coEvery { mockRepository.upsertPantryItem(any()) } returns 1L

        val viewModel = PantryViewModel(mockRepository)
        viewModel.showAddDialog()
        viewModel.addItem("  Hành tây  ", 2.0, "củ")

        coVerify(exactly = 1) {
            mockRepository.upsertPantryItem(match { item ->
                item.name == "Hành tây" && item.amount == 2.0 && item.unit == "củ"
            })
        }
        assertFalse(viewModel.uiState.value.isAddDialogVisible)
    }

    @Test
    fun deleteItem_callsRepositoryDelete() = runTest {
        every { mockRepository.observePantry() } returns flowOf(emptyList())
        every { mockRepository.observeLowStock() } returns flowOf(emptyList())

        val viewModel = PantryViewModel(mockRepository)
        viewModel.deleteItem(12L)

        coVerify(exactly = 1) { mockRepository.deletePantryItem(12L) }
    }
}
