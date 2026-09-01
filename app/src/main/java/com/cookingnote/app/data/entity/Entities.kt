package com.cookingnote.app.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
@Immutable
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "restaurant",
    val color: Long = 0xFFFF6B35
)

@Entity(
    tableName = "recipes",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("name"), Index("isFavorite")]
)
@Immutable
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val categoryId: Long? = null,
    val prepTime: Int = 0,
    val cookTime: Int = 0,
    val servings: Int = 2,
    val difficulty: Int = 1,
    val imageUri: String? = null,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastCookedAt: Long? = null
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId"), Index("name")]
)
@Immutable
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String,
    val amount: Double = 0.0,
    val unit: String = "",
    val isOptional: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
@Immutable
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val stepNumber: Int,
    val description: String,
    val imageUri: String? = null
)

@Entity(tableName = "pantry_items")
@Immutable
data class PantryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double = 0.0,
    val unit: String = "",
    val expiryDate: Long? = null,
    val lowStockThreshold: Double = 1.0
)

@Entity(
    tableName = "cook_history",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId"), Index("cookedAt")]
)
@Immutable
data class CookHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val cookedAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "recipe_tags",
    primaryKeys = ["recipeId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tagId")]
)
data class RecipeTagCrossRef(
    val recipeId: Long,
    val tagId: Long
)

@Entity(tableName = "ai_query_logs")
data class AiQueryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val provider: String,
    val promptHash: String,
    val success: Boolean,
    val latencyMs: Long,
    val createdAt: Long = System.currentTimeMillis()
)
