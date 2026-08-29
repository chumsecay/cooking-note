package com.cookingnote.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cookingnote.app.data.entity.AiQueryLogEntity
import com.cookingnote.app.data.entity.CategoryEntity
import com.cookingnote.app.data.entity.CookHistoryEntity
import com.cookingnote.app.data.entity.CookHistoryWithRecipe
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.RecipeTagCrossRef
import com.cookingnote.app.data.entity.RecipeWithDetails
import com.cookingnote.app.data.entity.StepEntity
import com.cookingnote.app.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun getAll(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity): Long

    @Delete
    suspend fun delete(category: CategoryEntity)
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<RecipeEntity>>

    @Query(
        """
        SELECT * FROM recipes
        WHERE name LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
           OR id IN (
                SELECT recipeId FROM ingredients WHERE name LIKE '%' || :query || '%'
           )
        ORDER BY updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE categoryId = :categoryId ORDER BY updatedAt DESC")
    fun observeByCategory(categoryId: Long): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes ORDER BY RANDOM() LIMIT :limit")
    suspend fun random(limit: Int): List<RecipeEntity>

    @Query("SELECT COUNT(*) FROM recipes")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM recipes WHERE isFavorite = 1")
    fun observeFavoriteCount(): Flow<Int>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeWithDetails(id: Long): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getWithDetails(id: Long): RecipeWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recipe: RecipeEntity): Long

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Query("UPDATE recipes SET isFavorite = :favorite, updatedAt = :now WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean, now: Long = System.currentTimeMillis())

    @Query("UPDATE recipes SET lastCookedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun markCooked(id: Long, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT DISTINCT r.* FROM recipes r
        INNER JOIN ingredients i ON i.recipeId = r.id
        WHERE LOWER(i.name) IN (:names)
        GROUP BY r.id
        HAVING COUNT(DISTINCT LOWER(i.name)) >= :minMatch
        ORDER BY COUNT(DISTINCT LOWER(i.name)) DESC
        """
    )
    suspend fun findByIngredientNames(names: List<String>, minMatch: Int = 1): List<RecipeEntity>
}

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY sortOrder, id")
    suspend fun getByRecipe(recipeId: Long): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteByRecipe(recipeId: Long)
}

@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY stepNumber")
    suspend fun getByRecipe(recipeId: Long): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StepEntity>)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteByRecipe(recipeId: Long)
}

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY name")
    fun observeAll(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items ORDER BY name")
    suspend fun getAll(): List<PantryItemEntity>

    @Query("SELECT * FROM pantry_items WHERE amount <= lowStockThreshold ORDER BY name")
    fun observeLowStock(): Flow<List<PantryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PantryItemEntity): Long

    @Delete
    suspend fun delete(item: PantryItemEntity)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface HistoryDao {
    @Transaction
    @Query("SELECT * FROM cook_history ORDER BY cookedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<CookHistoryWithRecipe>>

    @Insert
    suspend fun insert(item: CookHistoryEntity): Long

    @Query("DELETE FROM cook_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name")
    fun observeAll(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun link(crossRef: RecipeTagCrossRef)

    @Query("DELETE FROM recipe_tags WHERE recipeId = :recipeId")
    suspend fun clearLinks(recipeId: Long)
}

@Dao
interface AiLogDao {
    @Insert
    suspend fun insert(log: AiQueryLogEntity): Long

    @Query("SELECT COUNT(*) FROM ai_query_logs WHERE success = 1")
    fun observeSuccessCount(): Flow<Int>
}
