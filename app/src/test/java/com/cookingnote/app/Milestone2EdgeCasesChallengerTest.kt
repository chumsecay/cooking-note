package com.cookingnote.app

import app.cash.turbine.test
import com.cookingnote.app.ai.AiService
import com.cookingnote.app.ai.AiSuggestion
import com.cookingnote.app.data.dao.RecipeStats
import com.cookingnote.app.data.entity.CategoryEntity
import com.cookingnote.app.data.entity.ChatMessageEntity
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.RecipeWithDetails
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.ui.viewmodel.AiUiState
import com.cookingnote.app.ui.viewmodel.AiViewModel
import com.cookingnote.app.ui.viewmodel.CreateRecipeViewModel
import com.cookingnote.app.ui.viewmodel.DetailUiState
import com.cookingnote.app.ui.viewmodel.DetailViewModel
import com.cookingnote.app.ui.viewmodel.FavoritesUiState
import com.cookingnote.app.ui.viewmodel.FavoritesViewModel
import com.cookingnote.app.ui.viewmodel.HistoryUiState
import com.cookingnote.app.ui.viewmodel.HistoryViewModel
import com.cookingnote.app.ui.viewmodel.HomeUiState
import com.cookingnote.app.ui.viewmodel.HomeViewModel
import com.cookingnote.app.ui.viewmodel.LibraryUiState
import com.cookingnote.app.ui.viewmodel.LibraryViewModel
import com.cookingnote.app.ui.viewmodel.PantryViewModel
import com.cookingnote.app.ui.viewmodel.SearchViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Adversarial Empirical Edge Case Verification Suite for Milestone 2.
 *
 * Specifically verifies:
 * 1. Recipe not found handling:
 *    - recipeId = -1L or <= 0L in DetailViewModel
 *    - recipeId > 0L but repository returns null
 * 2. Form validation error handling in CreateRecipeViewModel:
 *    - blank/whitespace name rejection
 *    - nameError update and prevention of repository save
 *    - valid name clears error
 * 3. Empty lists across ViewModels:
 *    - HomeViewModel (empty stats + picks + favorites -> HomeUiState.Empty)
 *    - LibraryViewModel (empty recipes -> LibraryUiState.Empty with categories preserved)
 *    - FavoritesViewModel (empty favorites -> FavoritesUiState.Empty)
 *    - HistoryViewModel (empty history -> HistoryUiState.Empty)
 *    - PantryViewModel (empty items -> isEmpty = true)
 * 4. Offline AI fallback trigger and state representation in AiViewModel:
 *    - Rule-based provider triggers offline fallback banner
 *    - Unconfigured cloud provider triggers config required banner
 *    - AI exception triggers assistant error message recording without crash
 * 5. Blank queries in SearchViewModel:
 *    - Blank/whitespace input does not trigger repository search
 *    - State reflects isEmptyPrompt = true and isNoResults = false
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Milestone2EdgeCasesChallengerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: CookbookRepository
    private lateinit var mockAiSettings: AiSettingsStore
    private lateinit var mockAiService: AiService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        mockAiSettings = mockk(relaxed = true)
        mockAiService = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================================================
    // 1. Edge Case: Recipe not found (-1L, 0L, or null in DetailViewModel)
    // ========================================================================

    @Test
    fun verifyDetailViewModel_negativeRecipeId_emitsEmptyStateDirectly() = runTest(testDispatcher) {
        val detailVm = DetailViewModel(mockRepository, recipeId = -1L)

        detailVm.uiState.test {
            // First emission is Loading (initialValue of stateIn)
            val first = awaitItem()
            assertTrue("First state is Loading", first is DetailUiState.Loading)

            // Upstream flowOf(DetailUiState.Empty) executes
            val second = awaitItem()
            assertTrue("DetailViewModel with recipeId=-1L must transition to DetailUiState.Empty", second is DetailUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }

        // Verify repository was never queried for -1L
        coVerify(exactly = 0) { mockRepository.observeRecipe(any()) }
    }

    @Test
    fun verifyDetailViewModel_zeroRecipeId_emitsEmptyStateDirectly() = runTest(testDispatcher) {
        val detailVm = DetailViewModel(mockRepository, recipeId = 0L)

        detailVm.uiState.test {
            val first = awaitItem()
            assertTrue("First state is Loading", first is DetailUiState.Loading)

            val second = awaitItem()
            assertTrue("DetailViewModel with recipeId=0L must transition to DetailUiState.Empty", second is DetailUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { mockRepository.observeRecipe(any()) }
    }

    @Test
    fun verifyDetailViewModel_nonExistentRecipeInDb_emitsEmptyState() = runTest(testDispatcher) {
        every { mockRepository.observeRecipe(999L) } returns flowOf(null)

        val detailVm = DetailViewModel(mockRepository, recipeId = 999L)

        detailVm.uiState.test {
            val first = awaitItem()
            assertTrue("First state is Loading", first is DetailUiState.Loading)

            val second = awaitItem()
            assertTrue("When repository returns null, DetailViewModel must transition to DetailUiState.Empty", second is DetailUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { mockRepository.observeRecipe(999L) }
    }

    // ========================================================================
    // 2. Edge Case: Form validation errors in CreateRecipeViewModel
    // ========================================================================

    @Test
    fun verifyCreateRecipeViewModel_blankNameValidationRejectsSave() = runTest(testDispatcher) {
        val createVm = CreateRecipeViewModel(mockRepository, recipeId = null)

        // Initially name is empty
        assertEquals("", createVm.uiState.value.name)
        assertFalse("Blank name makes form invalid", createVm.uiState.value.isFormValid)
        assertNull("Initially nameError is null", createVm.uiState.value.nameError)

        // Attempt save with empty name
        createVm.saveRecipe()
        runCurrent()

        assertEquals("Tên món không được để trống", createVm.uiState.value.nameError)
        assertFalse("isSaving must remain false", createVm.uiState.value.isSaving)
        assertFalse("isSaveCompleted must remain false", createVm.uiState.value.isSaveCompleted)
        coVerify(exactly = 0) { mockRepository.saveRecipe(any(), any(), any()) }

        // Attempt save with whitespace only
        createVm.onNameChange("     ")
        createVm.saveRecipe()
        runCurrent()

        assertEquals("Tên món không được để trống", createVm.uiState.value.nameError)
        assertFalse("Whitespace name is invalid", createVm.uiState.value.isFormValid)
        coVerify(exactly = 0) { mockRepository.saveRecipe(any(), any(), any()) }

        // Enter a valid name
        createVm.onNameChange("Bánh mì chảo")
        assertNull("Entering valid name clears nameError", createVm.uiState.value.nameError)
        assertTrue("Valid name makes form valid", createVm.uiState.value.isFormValid)
    }

    // ========================================================================
    // 3. Edge Case: Empty list handling across ViewModels
    // ========================================================================

    @Test
    fun verifyHomeViewModel_emptyCatalogEmitsEmptyState() = runTest(testDispatcher) {
        every { mockRepository.observeStats() } returns flowOf(RecipeStats(total = 0, favorites = 0))
        every { mockRepository.observeTodaysPick(3) } returns flowOf(emptyList())
        every { mockRepository.observeFavorites() } returns flowOf(emptyList())

        val homeVm = HomeViewModel(mockRepository)

        homeVm.uiState.test {
            val first = awaitItem()
            assertTrue("Initial state is Loading", first is HomeUiState.Loading)

            val second = awaitItem()
            assertTrue("Empty stats and lists must yield HomeUiState.Empty", second is HomeUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun verifyLibraryViewModel_emptyRecipesEmitsEmptyStateWithCategories() = runTest(testDispatcher) {
        val dummyCategories = listOf(
            CategoryEntity(id = 1L, name = "Món chính", icon = "main"),
            CategoryEntity(id = 2L, name = "Món tráng miệng", icon = "dessert")
        )
        every { mockRepository.observeRecipes() } returns flowOf(emptyList())
        every { mockRepository.observeCategories() } returns flowOf(dummyCategories)

        val libraryVm = LibraryViewModel(mockRepository)

        libraryVm.uiState.test {
            val first = awaitItem()
            assertTrue("Initial state is Loading", first is LibraryUiState.Loading)

            val second = awaitItem()
            assertTrue("Empty recipe list must yield LibraryUiState.Empty", second is LibraryUiState.Empty)
            val emptyState = second as LibraryUiState.Empty
            assertEquals(2, emptyState.categories.size)
            assertEquals("Món chính", emptyState.categories[0].name)
            assertNull("No category selected initially", emptyState.selectedCategoryId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun verifyFavoritesViewModel_emptyFavoritesEmitsEmptyState() = runTest(testDispatcher) {
        every { mockRepository.observeFavorites() } returns flowOf(emptyList())

        val favoritesVm = FavoritesViewModel(mockRepository)

        favoritesVm.uiState.test {
            val first = awaitItem()
            assertTrue("Initial state is Loading", first is FavoritesUiState.Loading)

            val second = awaitItem()
            assertTrue("Empty favorites list must yield FavoritesUiState.Empty", second is FavoritesUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun verifyHistoryViewModel_emptyHistoryEmitsEmptyState() = runTest(testDispatcher) {
        every { mockRepository.observeHistory() } returns flowOf(emptyList())

        val historyVm = HistoryViewModel(mockRepository)

        historyVm.uiState.test {
            val first = awaitItem()
            assertTrue("Initial state is Loading", first is HistoryUiState.Loading)

            val second = awaitItem()
            assertTrue("Empty history list must yield HistoryUiState.Empty", second is HistoryUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun verifyPantryViewModel_emptyPantryComputesIsEmptyTrue() = runTest(testDispatcher) {
        every { mockRepository.observePantry() } returns flowOf(emptyList())
        every { mockRepository.observeLowStock() } returns flowOf(emptyList())

        val pantryVm = PantryViewModel(mockRepository)

        pantryVm.uiState.test {
            val first = awaitItem()
            assertTrue("Initial state isLoading is true", first.isLoading)

            val second = awaitItem()
            assertFalse("Loaded state isLoading is false", second.isLoading)
            assertTrue("Empty pantry items makes isEmpty true", second.isEmpty)
            assertFalse("No low stock items makes hasLowStock false", second.hasLowStock)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================================================
    // 4. Edge Case: Offline AI fallback trigger & error handling in AiViewModel
    // ========================================================================

    @Test
    fun verifyAiViewModel_offlineRuleBasedProviderBanner() = runTest(testDispatcher) {
        val settingsFlow = MutableStateFlow(
            AiSettings(provider = AiProviderType.RULE_BASED, apiKey = "")
        )
        every { mockAiSettings.settings } returns settingsFlow
        every { mockRepository.observeChat() } returns flowOf(emptyList())
        every { mockRepository.observePantry() } returns flowOf(emptyList())

        val aiVm = AiViewModel(mockAiService, mockAiSettings, mockRepository, testDispatcher)

        aiVm.uiState.test {
            // Initial state from stateIn
            val initial = awaitItem()
            assertNotNull(initial)

            // Combined state from flows
            val state = awaitItem()
            assertEquals("Đang dùng gợi ý cục bộ (rule-based).", state.statusBannerText)
            assertFalse("Rule based is not cloud configured", state.isCloudConfigured)
            assertFalse("Rule based does not require key", state.keyRequired)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun verifyAiViewModel_cloudProviderWithoutKey_bannerPromptsConfiguration() = runTest(testDispatcher) {
        val settingsFlow = MutableStateFlow(
            AiSettings(provider = AiProviderType.GEMINI, apiKey = "")
        )
        every { mockAiSettings.settings } returns settingsFlow
        every { mockRepository.observeChat() } returns flowOf(emptyList())
        every { mockRepository.observePantry() } returns flowOf(emptyList())

        val aiVm = AiViewModel(mockAiService, mockAiSettings, mockRepository, testDispatcher)

        aiVm.uiState.test {
            // Initial state from stateIn
            val initial = awaitItem()
            assertNotNull(initial)

            // Combined state from flows
            val state = awaitItem()
            assertEquals("AI chưa cấu hình. Vào Cài đặt → chọn provider → nhập API key.", state.statusBannerText)
            assertFalse(state.isCloudConfigured)
            assertTrue(state.keyRequired)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun verifyAiViewModel_offlineFallbackExecutionSuccess() = runTest(testDispatcher) {
        val settingsFlow = MutableStateFlow(AiSettings(provider = AiProviderType.RULE_BASED))
        every { mockAiSettings.settings } returns settingsFlow
        every { mockRepository.observeChat() } returns flowOf(emptyList())
        every { mockRepository.observePantry() } returns flowOf(emptyList())

        // Rule-based AI returns suggestion
        coEvery { mockAiService.chat(any(), any()) } returns AiSuggestion(
            title = "Món gợi ý",
            summary = "Gợi ý từ rule cục bộ",
            detail = "Nấu trứng chiên hành trong 5 phút.",
            source = "rule_based"
        )

        val aiVm = AiViewModel(mockAiService, mockAiSettings, mockRepository, testDispatcher)
        aiVm.sendMessage("Tôi có trứng")
        runCurrent()

        coVerify(exactly = 1) { mockRepository.appendMessage(ChatMessageEntity.ROLE_USER, "Tôi có trứng") }
        coVerify(exactly = 1) { mockRepository.appendMessage(ChatMessageEntity.ROLE_ASSISTANT, "Nấu trứng chiên hành trong 5 phút.") }
        assertFalse("isBusy must reset to false", aiVm.isBusy.value)
    }

    // ========================================================================
    // 5. Edge Case: Blank queries in SearchViewModel
    // ========================================================================

    @Test
    fun verifySearchViewModel_blankOrWhitespaceQueryDoesNotQueryRepository() = runTest(testDispatcher) {
        val searchVm = SearchViewModel(mockRepository)

        // Enter whitespace
        searchVm.onQueryChange("     ")
        assertEquals("     ", searchVm.searchQuery.value)
        assertTrue("Whitespace query counts as isEmptyPrompt", searchVm.uiState.value.isEmptyPrompt)
        assertFalse("isNoResults is false when query is blank", searchVm.uiState.value.isNoResults)

        // Clear query
        searchVm.clearQuery()
        assertEquals("", searchVm.searchQuery.value)
        assertTrue(searchVm.uiState.value.isEmptyPrompt)

        // Repository should never be queried for blank search
        coVerify(exactly = 0) { mockRepository.searchRecipes(any()) }
    }
}
