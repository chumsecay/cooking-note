package com.cookingnote.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.ui.components.RecipeCard
import com.cookingnote.app.ui.components.SyncStatusBadge
import com.cookingnote.app.ui.local.LocalAppContainer
import com.cookingnote.app.ui.viewmodel.AppViewModelFactory
import com.cookingnote.app.ui.viewmodel.HomeUiState
import com.cookingnote.app.ui.viewmodel.HomeViewModel

/**
 * Stateful Home screen composable.
 * Wires [HomeViewModel] state and dispatches user events.
 */
@Composable
fun HomeScreen(
    onOpenRecipe: (Long) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = AppViewModelFactory(LocalAppContainer.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onOpenRecipe = onOpenRecipe,
        onOpenLibrary = onOpenLibrary,
        onOpenAi = onOpenAi,
        onOpenSettings = onOpenSettings,
        onOpenSearch = onOpenSearch,
        onToggleFavorite = { recipe -> viewModel.toggleFavorite(recipe) },
        modifier = modifier
    )
}

/**
 * Stateless Home screen content composable.
 * Purely renders the [HomeUiState] and emits user interactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onOpenRecipe: (Long) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onToggleFavorite: (RecipeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Sổ tay Nấu ăn", fontWeight = FontWeight.SemiBold) },
                actions = {
                    SyncStatusBadge(
                        status = uiState.syncStatus,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Cài đặt")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is HomeUiState.Empty -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Xin chào 👋", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Sổ tay công thức của bạn",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onOpenAi) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Text(" Trợ lý AI")
                            }
                            Button(onClick = onOpenLibrary) {
                                Icon(Icons.Filled.RestaurantMenu, contentDescription = null)
                                Text(" Thư viện")
                            }
                        }
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Chưa có công thức nào trong sổ tay.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Hãy vào Thư viện để bắt đầu tạo hoặc khám phá các công thức mới!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = onOpenLibrary,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Mở Thư viện")
                                }
                            }
                        }
                    }
                }
            }

            is HomeUiState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("Xin chào 👋", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Sổ tay công thức của bạn",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                label = "Công thức",
                                value = "${uiState.stats.total}",
                                icon = Icons.Filled.RestaurantMenu,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Yêu thích",
                                value = "${uiState.stats.favorites}",
                                icon = Icons.Filled.Favorite,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onOpenAi) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Text(" Trợ lý AI")
                            }
                            Button(onClick = onOpenLibrary) {
                                Icon(Icons.Filled.RestaurantMenu, contentDescription = null)
                                Text(" Thư viện")
                            }
                        }
                    }
                    if (uiState.todaysPicks.isNotEmpty()) {
                        item { Text("Gợi ý hôm nay", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                        items(uiState.todaysPicks, key = { "pick_${it.id}" }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onOpenRecipe(recipe.id) },
                                onToggleFavorite = { onToggleFavorite(recipe) }
                            )
                        }
                    }
                    if (uiState.favorites.isNotEmpty()) {
                        item { Text("Yêu thích", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                        items(uiState.favorites.take(5), key = { "fav_${it.id}" }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onOpenRecipe(recipe.id) },
                                onToggleFavorite = { onToggleFavorite(recipe) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}