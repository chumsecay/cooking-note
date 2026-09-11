package com.cookingnote.app.ai

import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.repository.CookbookRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RuleBasedAiTest {

    private lateinit var mockRepository: CookbookRepository
    private lateinit var ruleBasedAi: RuleBasedAi

    private val sampleRecipes = listOf(
        RecipeEntity(id = 1, name = "Đậu sốt cà chua chay", prepTime = 5, cookTime = 15, servings = 2),
        RecipeEntity(id = 2, name = "Phở Bò Hà Nội", prepTime = 30, cookTime = 90, servings = 4),
        RecipeEntity(id = 3, name = "Salad rau củ", prepTime = 10, cookTime = 5, servings = 2),
        RecipeEntity(id = 4, name = "Bò kho", prepTime = 20, cookTime = 60, servings = 4)
    )

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
        coEvery { mockRepository.randomRecipes(any()) } returns sampleRecipes
        ruleBasedAi = RuleBasedAi(mockRepository)
    }

    @Test
    fun suggest_withQuickKeyword_filtersUnder30Minutes() = runTest {
        val suggestions = ruleBasedAi.suggest("Cho tôi món nhanh")

        assertTrue("Should return suggestions", suggestions.isNotEmpty())
        suggestions.forEach { suggestion ->
            val totalTime = suggestion.matchedRecipe!!.prepTime + suggestion.matchedRecipe!!.cookTime
            assertTrue("Total time must be <= 30 mins: $totalTime", totalTime <= 30)
        }
    }

    @Test
    fun suggest_withVegetarianKeyword_filtersVegetarianDishes() = runTest {
        val suggestions = ruleBasedAi.suggest("Tôi muốn ăn chay")

        assertTrue("Should return vegetarian suggestion", suggestions.isNotEmpty())
        assertEquals("Đậu sốt cà chua chay", suggestions.first().matchedRecipe?.name)
    }

    @Test
    fun suggest_fallback_returnsAvailableRecipesWhenNoMatch() = runTest {
        val suggestions = ruleBasedAi.suggest("Món gì thật phức tạp lạ lẫm")

        assertTrue("Should fallback to returning available recipes", suggestions.isNotEmpty())
        assertEquals("rule-based", suggestions.first().source)
    }
}
