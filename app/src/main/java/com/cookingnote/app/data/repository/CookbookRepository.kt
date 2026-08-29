package com.cookingnote.app.data.repository

import com.cookingnote.app.data.dao.AiLogDao
import com.cookingnote.app.data.dao.CategoryDao
import com.cookingnote.app.data.dao.HistoryDao
import com.cookingnote.app.data.dao.IngredientDao
import com.cookingnote.app.data.dao.PantryDao
import com.cookingnote.app.data.dao.RecipeDao
import com.cookingnote.app.data.dao.StepDao
import com.cookingnote.app.data.dao.TagDao
import com.cookingnote.app.data.entity.AiQueryLogEntity
import com.cookingnote.app.data.entity.CategoryEntity
import com.cookingnote.app.data.entity.CookHistoryEntity
import com.cookingnote.app.data.entity.CookHistoryWithRecipe
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.RecipeWithDetails
import com.cookingnote.app.data.entity.StepEntity
import kotlinx.coroutines.flow.Flow

class CookbookRepository(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val stepDao: StepDao,
    private val categoryDao: CategoryDao,
    private val pantryDao: PantryDao,
    private val historyDao: HistoryDao,
    private val tagDao: TagDao,
    private val aiLogDao: AiLogDao
) {
    fun observeRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeAll()
    fun observeFavorites(): Flow<List<RecipeEntity>> = recipeDao.observeFavorites()
    fun searchRecipes(query: String): Flow<List<RecipeEntity>> =
        if (query.isBlank()) recipeDao.observeAll() else recipeDao.search(query.trim())

    fun observeRecipe(id: Long): Flow<RecipeWithDetails?> = recipeDao.observeWithDetails(id)
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()
    fun observePantry(): Flow<List<PantryItemEntity>> = pantryDao.observeAll()
    fun observeLowStock(): Flow<List<PantryItemEntity>> = pantryDao.observeLowStock()
    fun observeHistory(limit: Int = 50): Flow<List<CookHistoryWithRecipe>> = historyDao.observeRecent(limit)
    fun observeRecipeCount(): Flow<Int> = recipeDao.observeCount()
    fun observeFavoriteCount(): Flow<Int> = recipeDao.observeFavoriteCount()

    suspend fun getRecipe(id: Long): RecipeWithDetails? = recipeDao.getWithDetails(id)
    suspend fun randomRecipes(limit: Int): List<RecipeEntity> = recipeDao.random(limit)
    suspend fun suggestFromPantry(): List<RecipeEntity> {
        val names = pantryDao.getAll().map { it.name.lowercase() }
        if (names.isEmpty()) return recipeDao.random(5)
        return recipeDao.findByIngredientNames(names, minMatch = 1).take(10)
    }

    suspend fun saveRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>
    ): Long {
        val id = recipeDao.upsert(recipe)
        ingredientDao.deleteByRecipe(id)
        stepDao.deleteByRecipe(id)
        ingredientDao.upsertAll(ingredients.mapIndexed { index, item ->
            item.copy(recipeId = id, sortOrder = index)
        })
        stepDao.upsertAll(steps.mapIndexed { index, item ->
            item.copy(recipeId = id, stepNumber = index + 1)
        })
        return id
    }

    suspend fun deleteRecipe(id: Long) = recipeDao.deleteById(id)
    suspend fun setFavorite(id: Long, favorite: Boolean) = recipeDao.setFavorite(id, favorite)

    suspend fun markCooked(id: Long, note: String = "") {
        recipeDao.markCooked(id)
        historyDao.insert(CookHistoryEntity(recipeId = id, note = note))
    }

    suspend fun upsertCategory(category: CategoryEntity): Long = categoryDao.upsert(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun upsertPantryItem(item: PantryItemEntity): Long = pantryDao.upsert(item)
    suspend fun deletePantryItem(id: Long) = pantryDao.deleteById(id)

    suspend fun logAi(provider: String, promptHash: String, success: Boolean, latencyMs: Long) {
        aiLogDao.insert(
            AiQueryLogEntity(
                provider = provider,
                promptHash = promptHash,
                success = success,
                latencyMs = latencyMs
            )
        )
    }
}
