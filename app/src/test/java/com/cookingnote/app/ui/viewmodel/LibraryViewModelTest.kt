package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.data.entity.CategoryEntity
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
class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun uiState_filtersByCategoryCorrectly() = runTest {
        val cat1 = CategoryEntity(id = 1, name = "Món canh")
        val cat2 = CategoryEntity(id = 2, name = "Món mặn")
        val recipe1 = RecipeEntity(id = 10, name = "Canh chua", categoryId = 1)
        val recipe2 = RecipeEntity(id = 20, name = "Thịt kho", categoryId = 2)

        every { mockRepository.observeCategories() } returns flowOf(listOf(cat1, cat2))
        every { mockRepository.observeRecipes() } returns flowOf(listOf(recipe1, recipe2))

        val viewModel = LibraryViewModel(mockRepository)

        viewModel.uiState.test {
            val item = awaitItem()
            val content = if (item is LibraryUiState.Loading) awaitItem() else item
            assertTrue("Expected Content", content is LibraryUiState.Content)
            assertEquals(2, (content as LibraryUiState.Content).recipes.size)

            viewModel.selectCategory(1L)
            val filteredContent = awaitItem() as LibraryUiState.Content
            assertEquals(1, filteredContent.recipes.size)
            assertEquals("Canh chua", filteredContent.recipes[0].name)
            assertEquals(1L, filteredContent.selectedCategoryId)
        }
    }
}
