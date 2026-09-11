package com.cookingnote.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Empirical architectural conformance test for Milestone 3.
 * Verifies:
 * 1. Zero occurrences of direct repository/data layer access in Compose screens.
 * 2. 100% adoption of collectAsStateWithLifecycle() across all 10 screens.
 * 3. Existence of public stateless Content composables accepting pure UiState and callbacks.
 * 4. NavHost routing integration with AppViewModelFactory in CookingNoteRoot.
 */
class Milestone3ArchitectureConformanceTest {

    private lateinit var screensDir: File
    private lateinit var navigationFile: File

    private val expectedScreenFiles = listOf(
        "AiScreen.kt",
        "CreateRecipeScreen.kt",
        "DetailScreen.kt",
        "FavoritesScreen.kt",
        "HistoryScreen.kt",
        "HomeScreen.kt",
        "LibraryScreen.kt",
        "PantryScreen.kt",
        "SearchScreen.kt",
        "SettingsScreen.kt"
    )

    @Before
    fun setUp() {
        screensDir = listOf(
            File("src/main/java/com/cookingnote/app/ui/screens"),
            File("app/src/main/java/com/cookingnote/app/ui/screens"),
            File("../app/src/main/java/com/cookingnote/app/ui/screens")
        ).firstOrNull { it.exists() && it.isDirectory }
            ?: error("Screens directory not found in candidate paths")

        navigationFile = listOf(
            File("src/main/java/com/cookingnote/app/ui/CookingNoteRoot.kt"),
            File("app/src/main/java/com/cookingnote/app/ui/CookingNoteRoot.kt"),
            File("../app/src/main/java/com/cookingnote/app/ui/CookingNoteRoot.kt")
        ).firstOrNull { it.exists() && it.isFile }
            ?: error("CookingNoteRoot.kt not found in candidate paths")
    }

    @Test
    fun testAllTenScreensExist() {
        val actualFiles = screensDir.listFiles { file -> file.isFile && file.extension == "kt" }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

        assertEquals("Expected exactly 10 screen files", expectedScreenFiles.sorted(), actualFiles)
    }

    @Test
    fun testZeroDirectRepositoryOrDataLayerAccessInScreens() {
        screensDir.listFiles { file -> file.isFile && file.extension == "kt" }?.forEach { file ->
            val content = file.readText()
            val filename = file.name

            // 1. Zero occurrences of LocalAppContainer.current.repository
            assertFalse(
                "[$filename] Found direct repository access via LocalAppContainer.current.repository",
                content.contains("LocalAppContainer.current.repository")
            )

            // 2. Zero occurrences of direct repository references or method invocations
            assertFalse(
                "[$filename] Found direct repository reference in screen",
                content.contains(".repository") || content.contains("CookbookRepository")
            )

            // 3. Zero direct calls to aiService or aiSettings
            assertFalse(
                "[$filename] Found direct aiService access in screen",
                content.contains("aiService")
            )
            assertFalse(
                "[$filename] Found direct aiSettings access in screen",
                content.contains("aiSettings")
            )

            // 4. Any usage of LocalAppContainer.current MUST only be for AppViewModelFactory fallback
            if (content.contains("LocalAppContainer.current")) {
                val matches = Regex("""LocalAppContainer\.current""").findAll(content).toList()
                val factoryMatches = Regex("""AppViewModelFactory\(LocalAppContainer\.current""").findAll(content).toList()
                assertEquals(
                    "[$filename] LocalAppContainer.current must only be used as fallback for AppViewModelFactory",
                    matches.size,
                    factoryMatches.size
                )
            }
        }
    }

    @Test
    fun testZeroUnsafeCollectAsStateInScreens() {
        screensDir.listFiles { file -> file.isFile && file.extension == "kt" }?.forEach { file ->
            val content = file.readText()
            val filename = file.name

            val bareCollectAsState = Regex("""\bcollectAsState\(""").findAll(content).toList()
            assertTrue(
                "[$filename] Contains lifecycle-unsafe collectAsState() calls: count = ${bareCollectAsState.size}",
                bareCollectAsState.isEmpty()
            )
        }
    }

    @Test
    fun testAllTenScreensUseCollectAsStateWithLifecycle() {
        screensDir.listFiles { file -> file.isFile && file.extension == "kt" }?.forEach { file ->
            val content = file.readText()
            val filename = file.name

            assertTrue(
                "[$filename] Must import collectAsStateWithLifecycle",
                content.contains("import androidx.lifecycle.compose.collectAsStateWithLifecycle")
            )

            val matches = Regex("""collectAsStateWithLifecycle\(\)""").findAll(content).toList()
            assertTrue(
                "[$filename] Must collect state using collectAsStateWithLifecycle()",
                matches.isNotEmpty()
            )
        }
    }

    @Test
    fun testStatelessContentComposablesFollowUdfContract() {
        val screenPairs = listOf(
            "HomeScreen.kt" to ("HomeContent" to "HomeUiState"),
            "LibraryScreen.kt" to ("LibraryContent" to "LibraryUiState"),
            "DetailScreen.kt" to ("DetailContent" to "DetailUiState"),
            "PantryScreen.kt" to ("PantryContent" to "PantryUiState"),
            "FavoritesScreen.kt" to ("FavoritesContent" to "FavoritesUiState"),
            "HistoryScreen.kt" to ("HistoryContent" to "HistoryUiState"),
            "SearchScreen.kt" to ("SearchContent" to "SearchUiState"),
            "SettingsScreen.kt" to ("SettingsContent" to "SettingsUiState"),
            "AiScreen.kt" to ("AiContent" to "AiUiState"),
            "CreateRecipeScreen.kt" to ("CreateRecipeContent" to "CreateRecipeUiState")
        )

        for ((fileName, pair) in screenPairs) {
            val (contentFunctionName, stateClassName) = pair
            val file = File(screensDir, fileName)
            val text = file.readText()

            // 1. Verify existence of public Content Composable
            val contentDefRegex = Regex("""fun\s+$contentFunctionName\s*\(""")
            assertTrue(
                "[$fileName] Must define public stateless composable fun $contentFunctionName",
                contentDefRegex.containsMatchIn(text)
            )

            // 2. Extract function signature
            val funcStartIndex = text.indexOf("fun $contentFunctionName")
            val paramsEndIndex = text.indexOf(")", funcStartIndex)
            val signature = text.substring(funcStartIndex, paramsEndIndex + 1)

            // 3. Verify signature takes pure UiState
            assertTrue(
                "[$fileName] $contentFunctionName signature must accept uiState: $stateClassName, but found: $signature",
                signature.contains("uiState: $stateClassName")
            )

            // 4. Verify signature does NOT accept ViewModel or repository or container
            assertFalse(
                "[$fileName] Stateless $contentFunctionName must NOT accept ViewModel instances",
                signature.contains("ViewModel")
            )
            assertFalse(
                "[$fileName] Stateless $contentFunctionName must NOT accept AppContainer instances",
                signature.contains("AppContainer")
            )
            assertFalse(
                "[$fileName] Stateless $contentFunctionName must NOT accept Repository instances",
                signature.contains("Repository")
            )
        }
    }

    @Test
    fun testCookingNoteRootWiresAllRoutesWithViewModelFactory() {
        val content = navigationFile.readText()

        assertTrue(
            "CookingNoteRoot must import AppViewModelFactory",
            content.contains("import com.cookingnote.app.ui.viewmodel.AppViewModelFactory")
        )

        val factoryInvocations = Regex("""AppViewModelFactory\(container""").findAll(content).toList()
        assertTrue(
            "CookingNoteRoot must instantiate ViewModels via AppViewModelFactory for each destination (found ${factoryInvocations.size})",
            factoryInvocations.size >= 10
        )

        val destinationRoutes = listOf(
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

        for (route in destinationRoutes) {
            assertTrue(
                "CookingNoteRoot must handle composable route $route",
                content.contains(route)
            )
        }
    }
}
