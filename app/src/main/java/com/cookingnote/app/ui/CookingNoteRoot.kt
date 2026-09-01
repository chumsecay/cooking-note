package com.cookingnote.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cookingnote.app.R
import com.cookingnote.app.ui.screens.AiScreen
import com.cookingnote.app.ui.screens.CreateRecipeScreen
import com.cookingnote.app.ui.screens.DetailScreen
import com.cookingnote.app.ui.screens.FavoritesScreen
import com.cookingnote.app.ui.screens.HistoryScreen
import com.cookingnote.app.ui.screens.HomeScreen
import com.cookingnote.app.ui.screens.LibraryScreen
import com.cookingnote.app.ui.screens.PantryScreen
import com.cookingnote.app.ui.screens.SearchScreen
import com.cookingnote.app.ui.screens.SettingsScreen

private data class BottomItem(
    val route: Route,
    val labelRes: Int,
    val icon: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Route.Home, R.string.nav_home, Icons.Filled.Home),
    BottomItem(Route.Library, R.string.nav_library, Icons.Filled.RestaurantMenu),
    BottomItem(Route.Ai, R.string.nav_ai, Icons.Filled.AutoAwesome),
    BottomItem(Route.Pantry, R.string.nav_pantry, Icons.Filled.Kitchen)
)

private val bottomRoutes = bottomItems.map { it.route.path }.toSet()

@Composable
fun CookingNoteRoot() {
    val nav = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(nav) }
    ) { padding ->
        AppNavHost(nav = nav, padding = padding)
    }
}

@Composable
private fun BottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    if (current !in bottomRoutes) return
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        bottomItems.forEach { item ->
            val selected = current == item.route.path
            NavigationBarItem(
                selected = selected,
                onClick = {
                    nav.navigate(item.route.path) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelRes)) }
            )
        }
    }
}

@Composable
private fun AppNavHost(nav: NavHostController, padding: PaddingValues) {
    NavHost(
        navController = nav,
        startDestination = Route.Home.path,
        modifier = Modifier.padding(padding)
    ) {
        composable(Route.Home.path) {
            HomeScreen(
                onOpenRecipe = { id -> nav.navigate(Route.Detail.of(id)) },
                onOpenLibrary = { nav.navigate(Route.Library.path) },
                onOpenAi = { nav.navigate(Route.Ai.path) },
                onOpenSettings = { nav.navigate(Route.Settings.path) },
                onOpenSearch = { nav.navigate(Route.Search.path) }
            )
        }
        composable(Route.Library.path) {
            LibraryScreen(
                onOpenRecipe = { id -> nav.navigate(Route.Detail.of(id)) },
                onCreate = { nav.navigate(Route.Create.path) },
                onOpenFavorites = { nav.navigate(Route.Favorites.path) },
                onOpenHistory = { nav.navigate(Route.History.path) },
                onOpenSearch = { nav.navigate(Route.Search.path) },
                onOpenSettings = { nav.navigate(Route.Settings.path) }
            )
        }
        composable(Route.Pantry.path) {
            PantryScreen()
        }
        composable(Route.Ai.path) {
            AiScreen(
                onOpenRecipe = { id -> nav.navigate(Route.Detail.of(id)) }
            )
        }
        composable(Route.Settings.path) {
            SettingsScreen()
        }
        composable(Route.Favorites.path) {
            FavoritesScreen(
                onOpenRecipe = { id -> nav.navigate(Route.Detail.of(id)) }
            )
        }
        composable(Route.History.path) {
            HistoryScreen(
                onOpenRecipe = { id -> nav.navigate(Route.Detail.of(id)) }
            )
        }
        composable(Route.Search.path) {
            SearchScreen(
                onOpenRecipe = { id -> nav.navigate(Route.Detail.of(id)) }
            )
        }
        composable(Route.Create.path) {
            CreateRecipeScreen(
                onDone = { nav.popBackStack() }
            )
        }
        composable(
            route = Route.Detail.path,
            arguments = listOf(navArgument(Route.Detail.ARG_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Route.Detail.ARG_ID) ?: -1L
            DetailScreen(
                recipeId = id,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate(Route.Edit.of(id)) }
            )
        }
        composable(
            route = Route.Edit.path,
            arguments = listOf(navArgument(Route.Edit.ARG_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Route.Edit.ARG_ID) ?: -1L
            CreateRecipeScreen(
                recipeId = id,
                onDone = { nav.popBackStack() }
            )
        }
    }
}