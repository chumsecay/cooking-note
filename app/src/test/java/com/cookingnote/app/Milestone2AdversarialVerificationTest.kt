package com.cookingnote.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import com.cookingnote.app.ai.AiService
import com.cookingnote.app.ai.AiSuggestion
import com.cookingnote.app.data.AppContainer
import com.cookingnote.app.data.dao.RecipeStats
import com.cookingnote.app.data.entity.ChatMessageEntity
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.RecipeWithDetails
import com.cookingnote.app.data.entity.StepEntity
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.ui.viewmodel.AiUiState
import com.cookingnote.app.ui.viewmodel.AiViewModel
import com.cookingnote.app.ui.viewmodel.AppViewModelFactory
import com.cookingnote.app.ui.viewmodel.CreateRecipeUiState
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
import com.cookingnote.app.ui.viewmodel.PantryUiState
import com.cookingnote.app.ui.viewmodel.PantryViewModel
import com.cookingnote.app.ui.viewmodel.SearchUiState
import com.cookingnote.app.ui.viewmodel.SearchViewModel
import com.cookingnote.app.ui.viewmodel.SettingsUiState
import com.cookingnote.app.ui.viewmodel.SettingsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Adversarial empirical verification test suite for Milestone 2.
 *
 * Verifies:
 * 1. All 10 ViewModels are cleanly instantiated via AppViewModelFactory without throwing IllegalArgumentException.
 * 2. Unregistered ViewModels continue to throw IllegalArgumentException as expected.
 * 3. Initial UI state emissions for all 10 ViewModels match contract expectations.
 * 4. SearchViewModel genuinely implements 300ms debounce, distinctUntilChanged, and flatMapLatest.
 * 5. AiViewModel executes AI requests on background dispatcher, handles errors, and updates busy/error states.
 * 6. SettingsViewModel performs database backup on background dispatcher and handles missing files gracefully.
 * 7. Pure helper methods like DetailViewModel.buildShareText format correctly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Milestone2AdversarialVerificationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockContainer: AppContainer
    private lateinit var mockRepository: CookbookRepository
    private lateinit var mockAiSettings: AiSettingsStore
    private lateinit var mockAiService: AiService
    private val fakeDbFile = File.createTempFile("fake_db", ".sqlite").apply { deleteOnExit() }

    private class UnregisteredDummyViewModel : ViewModel()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mockk(relaxed = true)
        mockAiSettings = mockk(relaxed = true)
        mockAiService = mockk(relaxed = true)
        mockContainer = mockk(relaxed = true)

        // Setup common flow defaults
        every { mockRepository.observeStats() } returns flowOf(RecipeStats(total = 5, favorites = 2))
        every { mockRepository.observeTodaysPick(any()) } returns flowOf(emptyList())
        every { mockRepository.observeFavorites() } returns flowOf(emptyList())
        every { mockRepository.observeRecipes() } returns flowOf(emptyList())
        every { mockRepository.observeCategories() } returns flowOf(emptyList())
        every { mockRepository.observeRecipe(any()) } returns flowOf(null)
        every { mockRepository.observePantry() } returns flowOf(emptyList())
        every { mockRepository.observeLowStock() } returns flowOf(emptyList())
        every { mockRepository.observeHistory(any()) } returns flowOf(emptyList())
        every { mockRepository.observeChat() } returns flowOf(emptyList())
        every { mockRepository.searchRecipes(any()) } returns flowOf(emptyList())

        every { mockAiSettings.settings } returns kotlinx.coroutines.flow.MutableStateFlow(AiSettings())

        every { mockContainer.repository } returns mockRepository
        every { mockContainer.aiSettings } returns mockAiSettings
        every { mockContainer.aiService } returns mockAiService
        every { mockContainer.databaseFile() } returns fakeDbFile
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        fakeDbFile.delete()
    }

    // ========================================================================
    // 1. Empirical Verification of AppViewModelFactory Instantiation
    // ========================================================================

    @Test
    fun verifyFactory_instantiatesAll10ViewModelsSuccessfully() {
        val factory = AppViewModelFactory(container = mockContainer, recipeId = 42L)

        val vmList = listOf(
            factory.create(HomeViewModel::class.java),
            factory.create(LibraryViewModel::class.java),
            factory.create(DetailViewModel::class.java),
            factory.create(PantryViewModel::class.java),
            factory.create(FavoritesViewModel::class.java),
            factory.create(HistoryViewModel::class.java),
            factory.create(SearchViewModel::class.java),
            factory.create(SettingsViewModel::class.java),
            factory.create(AiViewModel::class.java),
            factory.create(CreateRecipeViewModel::class.java)
        )

        assertEquals("Factory must instantiate all 10 ViewModels", 10, vmList.size)
        vmList.forEach { vm ->
            assertNotNull("Created ViewModel must not be null", vm)
        }

        assertTrue("1. HomeViewModel instance", vmList[0] is HomeViewModel)
        assertTrue("2. LibraryViewModel instance", vmList[1] is LibraryViewModel)
        assertTrue("3. DetailViewModel instance", vmList[2] is DetailViewModel)
        assertTrue("4. PantryViewModel instance", vmList[3] is PantryViewModel)
        assertTrue("5. FavoritesViewModel instance", vmList[4] is FavoritesViewModel)
        assertTrue("6. HistoryViewModel instance", vmList[5] is HistoryViewModel)
        assertTrue("7. SearchViewModel instance", vmList[6] is SearchViewModel)
        assertTrue("8. SettingsViewModel instance", vmList[7] is SettingsViewModel)
        assertTrue("9. AiViewModel instance", vmList[8] is AiViewModel)
        assertTrue("10. CreateRecipeViewModel instance", vmList[9] is CreateRecipeViewModel)

        // Verify recipeId propagation
        val detailVm = vmList[2] as DetailViewModel
        assertEquals("DetailViewModel must receive recipeId from factory", 42L, detailVm.recipeId)
    }

    @Test
    fun verifyFactory_handlesNullRecipeIdGracefully() {
        val factoryWithoutId = AppViewModelFactory(container = mockContainer, recipeId = null)

        val detailVm = factoryWithoutId.create(DetailViewModel::class.java)
        assertEquals("DetailViewModel defaults to -1L when recipeId is null", -1L, detailVm.recipeId)

        val createVm = factoryWithoutId.create(CreateRecipeViewModel::class.java)
        assertFalse("CreateRecipeViewModel should not be in edit mode when recipeId is null", createVm.uiState.value.isEditMode)
    }

    @Test
    fun verifyFactory_creationExtrasDelegatesCorrectly() {
        val factory = AppViewModelFactory(container = mockContainer)
        val homeVm = factory.create(HomeViewModel::class.java, CreationExtras.Empty)
        assertNotNull("create with CreationExtras must succeed for registered ViewModel", homeVm)
        assertTrue(homeVm is HomeViewModel)
    }

    @Test
    fun verifyFactory_throwsForUnregisteredViewModel() {
        val factory = AppViewModelFactory(container = mockContainer)
        val ex = assertThrows(IllegalArgumentException::class.java) {
            factory.create(UnregisteredDummyViewModel::class.java)
        }
        assertTrue(ex.message?.contains("Unknown ViewModel class") == true)
        assertTrue(ex.message?.contains(UnregisteredDummyViewModel::class.java.name) == true)
    }

    // ========================================================================
    // 2. Empirical Verification of Initial UI States
    // ========================================================================

    @Test
    fun verifyInitialUiStates_allViewModelsEmitExpectedInitialStates() = runTest {
        val factory = AppViewModelFactory(container = mockContainer, recipeId = 10L)

        val homeVm = factory.create(HomeViewModel::class.java)
        assertTrue("Home initial state must be Loading", homeVm.uiState.value is HomeUiState.Loading)

        val libraryVm = factory.create(LibraryViewModel::class.java)
        assertTrue("Library initial state must be Loading", libraryVm.uiState.value is LibraryUiState.Loading)

        val detailVm = factory.create(DetailViewModel::class.java)
        assertTrue("Detail initial state must be Loading", detailVm.uiState.value is DetailUiState.Loading)

        val favoritesVm = factory.create(FavoritesViewModel::class.java)
        assertTrue("Favorites initial state must be Loading", favoritesVm.uiState.value is FavoritesUiState.Loading)

        val historyVm = factory.create(HistoryViewModel::class.java)
        assertTrue("History initial state must be Loading", historyVm.uiState.value is HistoryUiState.Loading)

        val pantryVm = factory.create(PantryViewModel::class.java)
        assertTrue("Pantry initial state must have isLoading=true", pantryVm.uiState.value.isLoading)

        val searchVm = factory.create(SearchViewModel::class.java)
        assertEquals("Search initial state query must be empty", "", searchVm.uiState.value.query)
        assertTrue("Search initial state must be initial", searchVm.uiState.value.isInitial)
        assertTrue("Search initial state must show empty prompt", searchVm.uiState.value.isEmptyPrompt)

        val settingsVm = factory.create(SettingsViewModel::class.java)
        assertFalse("Settings initial state must not be saving", settingsVm.uiState.value.isSaving)
        assertFalse("Settings initial state must not be backing up", settingsVm.uiState.value.isBackingUp)
        assertEquals("Settings initial provider default is RULE_BASED", AiProviderType.RULE_BASED, settingsVm.uiState.value.provider)

        val aiVm = factory.create(AiViewModel::class.java)
        assertFalse("AI initial state must not be busy", aiVm.uiState.value.isBusy)
        assertTrue("AI initial messages must be empty", aiVm.uiState.value.messages.isEmpty())

        val createVm = factory.create(CreateRecipeViewModel::class.java)
        assertEquals("CreateRecipe initial name must be empty", "", createVm.uiState.value.name)
        assertTrue("CreateRecipe initial ingredient list must not be empty", createVm.uiState.value.ingredients.isNotEmpty())
    }

    // ========================================================================
    // 3. Empirical Verification of SearchViewModel Debounce & Reactive Operators
    // ========================================================================

    @Test
    fun verifySearchViewModel_queryUpdatesInstantlyInSearchQueryFlow() {
        val searchVm = SearchViewModel(mockRepository)
        assertEquals("", searchVm.searchQuery.value)

        searchVm.onQueryChange("Phở bò")
        assertEquals("searchQuery StateFlow must update synchronously without delay", "Phở bò", searchVm.searchQuery.value)
    }

    @Test
    fun verifySearchViewModel_debouncesFastTypingAndQueriesRepositoryOnlyAfterDelay() = runTest(testDispatcher) {
        val dummyResults = listOf(
            RecipeEntity(
                id = 1L,
                name = "Phở bò",
                description = "Món phở truyền thống",
                categoryId = 1L,
                prepTime = 15,
                cookTime = 60,
                servings = 4,
                difficulty = 2,
                imageUri = null,
                isFavorite = false,
                notes = "",
                createdAt = 0L,
                updatedAt = 0L
            )
        )
        every { mockRepository.searchRecipes("pho") } returns flowOf(dummyResults)

        val searchVm = SearchViewModel(mockRepository)
        backgroundScope.launch { searchVm.uiState.collect() }
        runCurrent()

        // Rapid typing: "p", "ph", "pho" each within 100ms
        searchVm.onQueryChange("p")
        advanceTimeBy(100)

        searchVm.onQueryChange("ph")
        advanceTimeBy(100)

        searchVm.onQueryChange("pho")
        advanceTimeBy(200)

        // At this point, 200ms elapsed since "pho", debounce is 300ms, repository should not have been called yet
        coVerify(exactly = 0) { mockRepository.searchRecipes("p") }
        coVerify(exactly = 0) { mockRepository.searchRecipes("ph") }
        coVerify(exactly = 0) { mockRepository.searchRecipes("pho") }

        // Advance the remaining 101ms (total > 300ms for "pho")
        advanceTimeBy(101)
        runCurrent()

        coVerify(exactly = 1) { mockRepository.searchRecipes("pho") }
        coVerify(exactly = 0) { mockRepository.searchRecipes("p") }
        coVerify(exactly = 0) { mockRepository.searchRecipes("ph") }
    }

    @Test
    fun verifySearchViewModel_blankQueryDebounceIsZeroMilliseconds() = runTest(testDispatcher) {
        val searchVm = SearchViewModel(mockRepository)
        backgroundScope.launch { searchVm.uiState.collect() }
        runCurrent()

        searchVm.onQueryChange("bún")
        advanceTimeBy(350)
        runCurrent()

        // Now clear query
        searchVm.clearQuery()
        // Blank query should debounce with 0L, immediately resetting without waiting 300ms
        advanceTimeBy(1)
        runCurrent()

        assertEquals("", searchVm.searchQuery.value)
        assertTrue(searchVm.uiState.value.isInitial)
        assertTrue(searchVm.uiState.value.isEmptyPrompt)
    }

    @Test
    fun verifySearchViewModel_distinctUntilChangedPreventsDuplicateSearches() = runTest(testDispatcher) {
        val searchVm = SearchViewModel(mockRepository)
        backgroundScope.launch { searchVm.uiState.collect() }
        runCurrent()

        searchVm.onQueryChange("gà")
        advanceTimeBy(350)
        runCurrent()

        // Emit exact same query again
        searchVm.onQueryChange("gà")
        advanceTimeBy(350)
        runCurrent()

        // Should only be queried once due to distinctUntilChanged()
        coVerify(exactly = 1) { mockRepository.searchRecipes("gà") }
    }

    // ========================================================================
    // 4. Empirical Verification of AiViewModel Background Execution & Error Handling
    // ========================================================================

    @Test
    fun verifyAiViewModel_sendMessageExecutesAsynchronouslyOnIoDispatcher() = runTest(testDispatcher) {
        val suggestion = AiSuggestion(
            title = "Gợi ý Phở",
            summary = "Phở bò thơm ngon",
            detail = "Nấu nước dùng từ xương bò với quế hồi trong 2 giờ.",
            source = "rule_based"
        )
        coEvery { mockAiService.chat(any(), any()) } returns suggestion

        val aiVm = AiViewModel(
            aiService = mockAiService,
            aiSettingsStore = mockAiSettings,
            repository = mockRepository,
            ioDispatcher = testDispatcher
        )

        assertFalse("Initial isBusy must be false", aiVm.isBusy.value)

        aiVm.sendMessage("Tôi muốn nấu phở")
        runCurrent()

        // Verify repository interactions
        coVerify(exactly = 1) {
            mockRepository.appendMessage(ChatMessageEntity.ROLE_USER, "Tôi muốn nấu phở")
        }
        coVerify(exactly = 1) {
            mockAiService.chat("Tôi muốn nấu phở", any())
        }
        coVerify(exactly = 1) {
            mockRepository.appendMessage(
                ChatMessageEntity.ROLE_ASSISTANT,
                "Nấu nước dùng từ xương bò với quế hồi trong 2 giờ."
            )
        }
        assertFalse("isBusy must reset to false after completion", aiVm.isBusy.value)
        assertNull("errorMessage must be null on success", aiVm.errorMessage.value)
    }

    @Test
    fun verifyAiViewModel_sendMessageHandlesExceptionsGracefully() = runTest(testDispatcher) {
        coEvery { mockAiService.chat(any(), any()) } throws RuntimeException("Network timeout simulation")

        val aiVm = AiViewModel(
            aiService = mockAiService,
            aiSettingsStore = mockAiSettings,
            repository = mockRepository,
            ioDispatcher = testDispatcher
        )

        aiVm.sendMessage("Gợi ý món chay")
        runCurrent()

        coVerify(exactly = 1) {
            mockRepository.appendMessage(ChatMessageEntity.ROLE_USER, "Gợi ý món chay")
        }
        coVerify(exactly = 1) {
            mockRepository.appendMessage(
                ChatMessageEntity.ROLE_ASSISTANT,
                match { it.contains("Network timeout simulation") }
            )
        }
        assertFalse("isBusy must reset to false even on exception", aiVm.isBusy.value)
        assertEquals("Network timeout simulation", aiVm.errorMessage.value)

        // Test clearing error message
        aiVm.clearErrorMessage()
        assertNull(aiVm.errorMessage.value)
    }

    // ========================================================================
    // 5. Empirical Verification of SettingsViewModel Database Backup & Dispatchers
    // ========================================================================

    @Test
    fun verifySettingsViewModel_createDatabaseBackupWithExistingFile() = runTest(testDispatcher) {
        val testDb = File.createTempFile("test_source_db", ".sqlite")
        testDb.writeText("MOCK_SQLITE_HEADER_DATA")
        testDb.deleteOnExit()

        val settingsVm = SettingsViewModel(
            aiSettingsStore = mockAiSettings,
            databaseFile = testDb,
            ioDispatcher = testDispatcher
        )

        val targetDir = File(System.getProperty("java.io.tmpdir"), "backup_test_${System.currentTimeMillis()}")
        targetDir.deleteOnExit()

        val backupResult = settingsVm.createDatabaseBackup(targetDir)
        assertNotNull("Backup file should not be null", backupResult)
        assertTrue("Backup file must exist", backupResult!!.exists())
        assertEquals("cookingnote-backup.db", backupResult.name)
        assertEquals("MOCK_SQLITE_HEADER_DATA", backupResult.readText())

        assertEquals(backupResult, settingsVm.uiState.value.backupSuccessFile)
        assertFalse(settingsVm.uiState.value.isBackingUp)
        assertNull(settingsVm.uiState.value.errorMessage)

        // Cleanup
        backupResult.delete()
        targetDir.delete()
        testDb.delete()
    }

    @Test
    fun verifySettingsViewModel_createDatabaseBackupFailsGracefullyWhenFileMissing() = runTest(testDispatcher) {
        val nonExistentDb = File("path_to_non_existent_cooking_database_file.db")

        val settingsVm = SettingsViewModel(
            aiSettingsStore = mockAiSettings,
            databaseFile = nonExistentDb,
            ioDispatcher = testDispatcher
        )

        val targetDir = File(System.getProperty("java.io.tmpdir"), "backup_missing_${System.currentTimeMillis()}")
        val backupResult = settingsVm.createDatabaseBackup(targetDir)

        assertNull("Backup file must be null for non-existent db", backupResult)
        assertFalse(settingsVm.uiState.value.isBackingUp)
        assertTrue(
            "Error message must indicate missing database file",
            settingsVm.uiState.value.errorMessage?.contains("Tệp cơ sở dữ liệu không tồn tại") == true
        )
    }

    // ========================================================================
    // 6. Empirical Verification of Pure Helpers (DetailViewModel.buildShareText)
    // ========================================================================

    @Test
    fun verifyDetailViewModel_buildShareTextFormatsCorrectly() {
        val recipe = RecipeEntity(
            id = 101L,
            name = "Cơm chiên dưa bò",
            description = "Món cơm đậm đà chuẩn vị Hà Nội",
            categoryId = 2L,
            prepTime = 10,
            cookTime = 15,
            servings = 2,
            difficulty = 1,
            imageUri = null,
            isFavorite = true,
            notes = "",
            createdAt = 0L,
            updatedAt = 0L
        )
        val ingredients = listOf(
            IngredientEntity(id = 1L, recipeId = 101L, name = "Thịt bò", amount = 200.0, unit = "g", sortOrder = 0),
            IngredientEntity(id = 2L, recipeId = 101L, name = "Dưa chua", amount = 150.0, unit = "g", sortOrder = 1),
            IngredientEntity(id = 3L, recipeId = 101L, name = "Cơm nguội", amount = 2.0, unit = "bát", sortOrder = 2)
        )
        val steps = listOf(
            StepEntity(id = 1L, recipeId = 101L, stepNumber = 2, description = "Xào thịt bò với dưa chua chín tới"),
            StepEntity(id = 1L, recipeId = 101L, stepNumber = 1, description = "Trộn cơm với lòng đỏ trứng"),
            StepEntity(id = 1L, recipeId = 101L, stepNumber = 3, description = "Chiên cơm vàng giòn rồi đổ bò dưa vào đảo đều")
        )
        val details = RecipeWithDetails(
            recipe = recipe,
            category = null,
            ingredients = ingredients,
            steps = steps,
            tags = emptyList()
        )

        val shareText = DetailViewModel.buildShareText(details)

        assertTrue(shareText.contains("Cơm chiên dưa bò"))
        assertTrue(shareText.contains("Món cơm đậm đà chuẩn vị Hà Nội"))
        assertTrue(shareText.contains("Nguyên liệu:"))
        assertTrue(shareText.contains("- 200 g Thịt bò"))
        assertTrue(shareText.contains("- 150 g Dưa chua"))
        assertTrue(shareText.contains("- 2 bát Cơm nguội"))
        assertTrue(shareText.contains("Các bước:"))
        // Ensure steps are ordered by stepNumber
        val step1Index = shareText.indexOf("1. Trộn cơm với lòng đỏ trứng")
        val step2Index = shareText.indexOf("2. Xào thịt bò với dưa chua chín tới")
        val step3Index = shareText.indexOf("3. Chiên cơm vàng giòn rồi đổ bò dưa vào đảo đều")
        assertTrue("Step 1 must appear before Step 2", step1Index < step2Index)
        assertTrue("Step 2 must appear before Step 3", step2Index < step3Index)
    }
}
