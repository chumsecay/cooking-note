package com.cookingnote.app

import androidx.lifecycle.ViewModel
import com.cookingnote.app.data.AppContainer
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.ui.Route
import com.cookingnote.app.ui.viewmodel.AiViewModel
import com.cookingnote.app.ui.viewmodel.AppViewModelFactory
import com.cookingnote.app.ui.viewmodel.CreateRecipeViewModel
import com.cookingnote.app.ui.viewmodel.DetailViewModel
import com.cookingnote.app.ui.viewmodel.FavoritesViewModel
import com.cookingnote.app.ui.viewmodel.HistoryViewModel
import com.cookingnote.app.ui.viewmodel.HomeViewModel
import com.cookingnote.app.ui.viewmodel.LibraryViewModel
import com.cookingnote.app.ui.viewmodel.PantryViewModel
import com.cookingnote.app.ui.viewmodel.SearchViewModel
import com.cookingnote.app.ui.viewmodel.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Adversarial Challenger verification test suite for Milestone 3.
 *
 * Verifies:
 * 1. CookingNoteRoot.kt navigation wiring:
 *    - All 11 destination routes are wired.
 *    - Every destination resolves its ViewModel via AppViewModelFactory.
 *    - Factory correctly handles all 10 ViewModel classes.
 * 2. Parameter passing:
 *    - Detail route "detail/{id}" extracts id and passes recipeId to Factory and DetailScreen.
 *    - Edit route "edit/{id}" extracts id and passes recipeId to Factory and CreateRecipeScreen.
 *    - Create route passes null recipeId to Factory and CreateRecipeScreen.
 *    - DetailViewModel and CreateRecipeViewModel reflect passed recipeId vs null.
 * 3. UI state edge case handling across all screens:
 *    - Loading, Empty, and Error states are modeled and represented in Composables.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Milestone3ChallengerAdversarialTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockContainer: AppContainer
    private lateinit var mockRepository: CookbookRepository
    private lateinit var mockAiSettings: AiSettingsStore
    private val fakeDb = File.createTempFile("fake_db", ".sqlite")

    private val screensDir = File("src/main/java/com/cookingnote/app/ui/screens")
        .takeIf { it.exists() }
        ?: File("app/src/main/java/com/cookingnote/app/ui/screens")

    private val rootNavFile = File("src/main/java/com/cookingnote/app/ui/CookingNoteRoot.kt")
        .takeIf { it.exists() }
        ?: File("app/src/main/java/com/cookingnote/app/ui/CookingNoteRoot.kt")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        mockAiSettings = mockk(relaxed = true)
        mockContainer = mockk(relaxed = true)

        every { mockAiSettings.settings } returns MutableStateFlow(AiSettings())
        every { mockContainer.repository } returns mockRepository
        every { mockContainer.aiSettings } returns mockAiSettings
        every { mockContainer.databaseFile() } returns fakeDb
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================================================
    // 1. Navigation Wiring Verification
    // ========================================================================

    @Test
    fun verifyCookingNoteRoot_wiresAllElevenRoutes() {
        assertTrue("CookingNoteRoot.kt must exist", rootNavFile.exists())
        val code = rootNavFile.readText()

        val expectedRoutes = listOf(
            "Route.Home.path",
            "Route.Library.path",
            "Route.Pantry.path",
            "Route.Ai.path",
            "Route.Settings.path",
            "Route.Favorites.path",
            "Route.History.path",
            "Route.Search.path",
            "Route.Create.path",
            "Route.Detail.path",
            "Route.Edit.path"
        )

        for (route in expectedRoutes) {
            assertTrue("CookingNoteRoot must contain composable mapping for $route", code.contains(route))
        }
    }

    @Test
    fun verifyCookingNoteRoot_instantiatesAllViewModelsViaAppViewModelFactory() {
        val code = rootNavFile.readText()

        val expectedVms = listOf(
            "HomeViewModel",
            "LibraryViewModel",
            "PantryViewModel",
            "AiViewModel",
            "SettingsViewModel",
            "FavoritesViewModel",
            "HistoryViewModel",
            "SearchViewModel",
            "CreateRecipeViewModel",
            "DetailViewModel"
        )

        for (vm in expectedVms) {
            assertTrue(
                "CookingNoteRoot must resolve $vm via viewModel(factory = AppViewModelFactory(...))",
                code.contains("val viewModel: $vm = viewModel(")
            )
        }
    }

    @Test
    fun verifyAppViewModelFactory_resolvesAllTenViewModelsCorrectly() {
        val factoryWithId = AppViewModelFactory(mockContainer, recipeId = 77L)

        val vmMap: Map<Class<out ViewModel>, ViewModel> = mapOf(
            HomeViewModel::class.java to factoryWithId.create(HomeViewModel::class.java),
            LibraryViewModel::class.java to factoryWithId.create(LibraryViewModel::class.java),
            DetailViewModel::class.java to factoryWithId.create(DetailViewModel::class.java),
            PantryViewModel::class.java to factoryWithId.create(PantryViewModel::class.java),
            FavoritesViewModel::class.java to factoryWithId.create(FavoritesViewModel::class.java),
            HistoryViewModel::class.java to factoryWithId.create(HistoryViewModel::class.java),
            SearchViewModel::class.java to factoryWithId.create(SearchViewModel::class.java),
            SettingsViewModel::class.java to factoryWithId.create(SettingsViewModel::class.java),
            AiViewModel::class.java to factoryWithId.create(AiViewModel::class.java),
            CreateRecipeViewModel::class.java to factoryWithId.create(CreateRecipeViewModel::class.java)
        )

        assertEquals("Must resolve all 10 registered ViewModel classes", 10, vmMap.size)
        vmMap.forEach { (clazz, instance) ->
            assertTrue("Instance must match requested type ${clazz.simpleName}", clazz.isInstance(instance))
        }
    }

    // ========================================================================
    // 2. Parameter Passing Verification
    // ========================================================================

    @Test
    fun verifyRouteParameterHelpers_andArgumentNames() {
        assertEquals("detail/100", Route.Detail.of(100L))
        assertEquals("edit/100", Route.Edit.of(100L))
        assertEquals("detail/{id}", Route.Detail.path)
        assertEquals("edit/{id}", Route.Edit.path)
        assertEquals("id", Route.Detail.ARG_ID)
        assertEquals("id", Route.Edit.ARG_ID)
    }

    @Test
    fun verifyCookingNoteRoot_passesRecipeIdToDetailAndEditScreens() {
        val code = rootNavFile.readText()

        // Detail composable block
        assertTrue(
            "Detail route must declare ARG_ID NavType.LongType argument",
            code.contains("Route.Detail.ARG_ID") && code.contains("NavType.LongType")
        )
        assertTrue(
            "Detail route must pass recipeId into AppViewModelFactory",
            code.contains("factory = AppViewModelFactory(container, recipeId = id)")
        )
        assertTrue(
            "Detail route must pass recipeId into DetailScreen",
            code.contains("DetailScreen(\n                recipeId = id,") ||
                code.contains("DetailScreen(recipeId = id,") ||
                code.contains("DetailScreen(\r\n                recipeId = id,")
        )

        // Edit composable block
        assertTrue(
            "Edit route must declare ARG_ID NavType.LongType argument",
            code.contains("Route.Edit.ARG_ID") && code.contains("NavType.LongType")
        )
        assertTrue(
            "Edit route must pass recipeId into CreateRecipeViewModel factory",
            code.contains("create_recipe_\$id") && code.contains("factory = AppViewModelFactory(container, recipeId = id)")
        )

        // Create composable block
        assertTrue(
            "Create route must pass recipeId = null into factory",
            code.contains("factory = AppViewModelFactory(container, recipeId = null)")
        )
    }

    @Test
    fun verifyRecipeIdPropagation_toViewModels() {
        val factoryWithId = AppViewModelFactory(mockContainer, recipeId = 123L)
        val detailVmWithId = factoryWithId.create(DetailViewModel::class.java)
        assertEquals(123L, detailVmWithId.recipeId)

        val factoryWithoutId = AppViewModelFactory(mockContainer, recipeId = null)
        val detailVmWithoutId = factoryWithoutId.create(DetailViewModel::class.java)
        assertEquals(-1L, detailVmWithoutId.recipeId)

        val createVmWithId = factoryWithId.create(CreateRecipeViewModel::class.java)
        assertTrue(createVmWithId.uiState.value.isEditMode)

        val createVmWithoutId = factoryWithoutId.create(CreateRecipeViewModel::class.java)
        assertFalse(createVmWithoutId.uiState.value.isEditMode)
    }

    // ========================================================================
    // 3. Edge Case UI Behavior: Empty, Loading, and Error States in Composables
    // ========================================================================

    @Test
    fun verifyHomeScreen_representsLoadingEmptyErrorContentStates() {
        val code = File(screensDir, "HomeScreen.kt").readText()
        assertTrue("HomeScreen must handle HomeUiState.Loading", code.contains("is HomeUiState.Loading"))
        assertTrue("HomeScreen must show CircularProgressIndicator on Loading", code.contains("CircularProgressIndicator()"))
        assertTrue("HomeScreen must handle HomeUiState.Error", code.contains("is HomeUiState.Error"))
        assertTrue("HomeScreen must render error message", code.contains("uiState.message"))
        assertTrue("HomeScreen must handle HomeUiState.Empty", code.contains("is HomeUiState.Empty"))
        assertTrue("HomeScreen must handle HomeUiState.Content", code.contains("is HomeUiState.Content"))
    }

    @Test
    fun verifyLibraryScreen_representsLoadingEmptyErrorContentStates() {
        val code = File(screensDir, "LibraryScreen.kt").readText()
        assertTrue("LibraryScreen must handle LibraryUiState.Loading", code.contains("is LibraryUiState.Loading"))
        assertTrue("LibraryScreen must show CircularProgressIndicator", code.contains("CircularProgressIndicator()"))
        assertTrue("LibraryScreen must handle LibraryUiState.Error", code.contains("is LibraryUiState.Error"))
        assertTrue("LibraryScreen must render error message", code.contains("uiState.message"))
        assertTrue("LibraryScreen must handle LibraryUiState.Empty", code.contains("is LibraryUiState.Empty"))
        assertTrue("LibraryScreen must handle LibraryUiState.Content", code.contains("is LibraryUiState.Content"))
    }

    @Test
    fun verifyDetailScreen_representsLoadingEmptyErrorContentStates() {
        val code = File(screensDir, "DetailScreen.kt").readText()
        assertTrue("DetailScreen must handle DetailUiState.Loading", code.contains("is DetailUiState.Loading"))
        assertTrue("DetailScreen must show CircularProgressIndicator", code.contains("CircularProgressIndicator()"))
        assertTrue("DetailScreen must handle DetailUiState.Error", code.contains("is DetailUiState.Error"))
        assertTrue("DetailScreen must handle DetailUiState.Empty", code.contains("is DetailUiState.Empty"))
        assertTrue("DetailScreen must handle DetailUiState.Content", code.contains("is DetailUiState.Content"))
        assertTrue("DetailScreen must handle recipe deletion dialog", code.contains("AlertDialog("))
    }

    @Test
    fun verifyFavoritesScreen_representsLoadingEmptyErrorContentStates() {
        val code = File(screensDir, "FavoritesScreen.kt").readText()
        assertTrue("FavoritesScreen must handle FavoritesUiState.Loading", code.contains("is FavoritesUiState.Loading"))
        assertTrue("FavoritesScreen must handle FavoritesUiState.Error", code.contains("is FavoritesUiState.Error"))
        assertTrue("FavoritesScreen must handle FavoritesUiState.Empty", code.contains("is FavoritesUiState.Empty"))
        assertTrue("FavoritesScreen must handle FavoritesUiState.Content", code.contains("is FavoritesUiState.Content"))
    }

    @Test
    fun verifyHistoryScreen_representsLoadingEmptyErrorContentStates() {
        val code = File(screensDir, "HistoryScreen.kt").readText()
        assertTrue("HistoryScreen must handle HistoryUiState.Loading", code.contains("is HistoryUiState.Loading"))
        assertTrue("HistoryScreen must handle HistoryUiState.Error", code.contains("is HistoryUiState.Error"))
        assertTrue("HistoryScreen must handle HistoryUiState.Empty", code.contains("is HistoryUiState.Empty"))
        assertTrue("HistoryScreen must handle HistoryUiState.Content", code.contains("is HistoryUiState.Content"))
    }

    @Test
    fun verifyPantryScreen_representsLoadingEmptyErrorBanners() {
        val code = File(screensDir, "PantryScreen.kt").readText()
        assertTrue("PantryScreen must handle uiState.isLoading", code.contains("uiState.isLoading"))
        assertTrue("PantryScreen must handle uiState.isEmpty", code.contains("uiState.isEmpty"))
        assertTrue("PantryScreen must handle uiState.errorMessage", code.contains("uiState.errorMessage != null"))
        assertTrue("PantryScreen must handle low stock banner", code.contains("uiState.hasLowStock"))
        assertTrue("PantryScreen must provide dismiss error callback", code.contains("onDismissError"))
    }

    @Test
    fun verifySearchScreen_representsLoadingPromptNoResultsStates() {
        val code = File(screensDir, "SearchScreen.kt").readText()
        assertTrue("SearchScreen must handle uiState.isLoading", code.contains("uiState.isLoading"))
        assertTrue("SearchScreen must handle uiState.isEmptyPrompt", code.contains("uiState.isEmptyPrompt"))
        assertTrue("SearchScreen must handle uiState.isNoResults", code.contains("uiState.isNoResults"))
        assertTrue("SearchScreen must handle uiState.errorMessage", code.contains("uiState.errorMessage != null"))
    }

    @Test
    fun verifyCreateRecipeScreen_representsLoadingErrorValidationStates() {
        val code = File(screensDir, "CreateRecipeScreen.kt").readText()
        assertTrue("CreateRecipeScreen must handle uiState.isLoading", code.contains("uiState.isLoading"))
        assertTrue("CreateRecipeScreen must handle uiState.saveError", code.contains("uiState.saveError != null"))
        assertTrue("CreateRecipeScreen must handle uiState.nameError", code.contains("uiState.nameError != null"))
        assertTrue("CreateRecipeScreen must handle uiState.isSaving", code.contains("uiState.isSaving"))
    }

    @Test
    fun verifyAiScreen_representsEmptyBusyErrorStates() {
        val code = File(screensDir, "AiScreen.kt").readText()
        assertTrue("AiScreen must handle empty messages state", code.contains("uiState.messages.isEmpty()"))
        assertTrue("AiScreen must handle isBusy state with typing bubble", code.contains("uiState.isBusy") && code.contains("TypingBubble()"))
        assertTrue("AiScreen must handle errorMessage", code.contains("uiState.errorMessage != null"))
        assertTrue("AiScreen must display statusBannerText", code.contains("StatusBanner(uiState.statusBannerText)"))
    }

    @Test
    fun verifySettingsScreen_representsSavingBackupFeedbackStates() {
        val code = File(screensDir, "SettingsScreen.kt").readText()
        assertTrue("SettingsScreen must handle isSaving", code.contains("uiState.isSaving"))
        assertTrue("SettingsScreen must handle isBackingUp", code.contains("uiState.isBackingUp"))
        assertTrue("SettingsScreen must display snackbar feedback", code.contains("snackbarHostState.showSnackbar"))
    }
}
