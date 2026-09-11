package com.cookingnote.app.ui.viewmodel

import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateRecipeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
    }

    @Test
    fun initialMode_withoutRecipeId_isCreateMode() = runTest(testDispatcher) {
        val viewModel = CreateRecipeViewModel(mockRepository, recipeId = null)
        runCurrent()

        assertFalse(viewModel.uiState.value.isEditMode)
        assertFalse(viewModel.uiState.value.isFormValid)
    }

    @Test
    fun formEditing_updatesValidationState() = runTest(testDispatcher) {
        val viewModel = CreateRecipeViewModel(mockRepository, recipeId = null)
        runCurrent()

        viewModel.onNameChange("Canh chua cá lóc")
        viewModel.onDescriptionChange("Món canh miền Tây")

        val state = viewModel.uiState.value
        assertEquals("Canh chua cá lóc", state.name)
        assertEquals("Món canh miền Tây", state.description)
        assertTrue(state.isFormValid)
    }

    @Test
    fun ingredientsAndSteps_dynamicAddAndRemove() = runTest(testDispatcher) {
        val viewModel = CreateRecipeViewModel(mockRepository, recipeId = null)
        runCurrent()

        assertEquals(1, viewModel.uiState.value.ingredients.size)
        assertEquals(1, viewModel.uiState.value.steps.size)

        viewModel.addIngredient()
        assertEquals(2, viewModel.uiState.value.ingredients.size)

        viewModel.addStep()
        assertEquals(2, viewModel.uiState.value.steps.size)

        viewModel.removeIngredient(0)
        assertEquals(1, viewModel.uiState.value.ingredients.size)

        viewModel.removeStep(0)
        assertEquals(1, viewModel.uiState.value.steps.size)
    }

    @Test
    fun saveRecipe_validForm_delegatesToRepository() = runTest(testDispatcher) {
        coEvery { mockRepository.saveRecipe(any(), any(), any()) } returns 88L

        val viewModel = CreateRecipeViewModel(mockRepository, recipeId = null)
        runCurrent()

        viewModel.onNameChange("Bánh mì chảo")
        viewModel.saveRecipe()
        runCurrent()

        coVerify(exactly = 1) { mockRepository.saveRecipe(any(), any(), any()) }
        assertTrue(viewModel.uiState.value.isSaveCompleted)
        assertEquals(88L, viewModel.uiState.value.savedRecipeId)
    }
}
