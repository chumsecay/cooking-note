package com.cookingnote.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.ui.components.RecipeCard
import com.cookingnote.app.ui.local.LocalAppContainer

@Composable
fun HomeScreen(
    onOpenRecipe: (Long) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAi: () -> Unit
) {
    val container = LocalAppContainer.current
    val recipeCount by container.repository.observeRecipeCount().collectAsState(0)
    val favoriteCount by container.repository.observeFavoriteCount().collectAsState(0)
    val favorites by container.repository.observeFavorites().collectAsState(emptyList())
    var today by remember { mutableStateOf<List<RecipeEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        today = container.repository.randomRecipes(3)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Xin chào 👋", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Sổ tay công thức của bạn",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Công thức", "$recipeCount", Modifier.weight(1f))
                StatCard("Yêu thích", "$favoriteCount", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenAi) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Text(" AI gợi ý")
                }
                TextButton(onClick = onOpenLibrary) {
                    Icon(Icons.Filled.RestaurantMenu, contentDescription = null)
                    Text(" Thư viện")
                }
            }
        }
        item {
            Text("Gợi ý hôm nay", style = MaterialTheme.typography.titleLarge)
        }
        items(today, key = { it.id }) { recipe ->
            RecipeCard(recipe = recipe, onClick = { onOpenRecipe(recipe.id) })
        }
        if (favorites.isNotEmpty()) {
            item {
                Text("Yêu thích", style = MaterialTheme.typography.titleLarge)
            }
            items(favorites.take(5), key = { "fav-${it.id}" }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onOpenRecipe(recipe.id) },
                    onToggleFavorite = {
                        // handled in detail/library
                    }
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
