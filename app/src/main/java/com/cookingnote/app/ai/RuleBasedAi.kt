package com.cookingnote.app.ai

import com.cookingnote.app.data.repository.CookbookRepository

class RuleBasedAi(
    private val repository: CookbookRepository
) {
    suspend fun suggest(prompt: String, ingredients: List<String> = emptyList()): List<AiSuggestion> {
        val all = repository.randomRecipes(20)
        val lower = prompt.lowercase()
        val wantsQuick = lower.contains("nhanh") || lower.contains("15 phút") || lower.contains("10 phút")
        val wantsVegetarian = lower.contains("chay")
        val wantsSpicy = lower.contains("cay")
        val filtered = all.filter { r ->
            (!wantsQuick || (r.prepTime + r.cookTime) <= 30) &&
                (!wantsVegetarian || r.name.contains("chay", ignoreCase = true)) &&
                (!wantsSpicy || true)
        }.take(5)
        val base = filtered.ifEmpty { all.take(5) }
        return base.map { rec ->
            AiSuggestion(
                title = rec.name,
                summary = rec.description.ifBlank { "Gợi ý từ thư viện của bạn." },
                matchedRecipe = rec,
                detail = "⏱ ${rec.prepTime + rec.cookTime} phút · 👥 ${rec.servings} người",
                source = "rule-based"
            )
        }
    }
}