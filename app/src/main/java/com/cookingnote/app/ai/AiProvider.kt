package com.cookingnote.app.ai

import com.cookingnote.app.data.entity.RecipeEntity

data class AiSuggestion(
    val title: String,
    val summary: String,
    val matchedRecipe: RecipeEntity? = null,
    val detail: String? = null,
    val source: String
)

interface AiService {
    suspend fun chat(prompt: String, history: List<Pair<String, String>> = emptyList()): AiSuggestion
    suspend fun suggestFromIngredients(ingredients: List<String>): List<AiSuggestion>
    suspend fun suggestFromImage(imageBytes: ByteArray): AiSuggestion
    val isCloudConfigured: Boolean
    val providerLabel: String
}