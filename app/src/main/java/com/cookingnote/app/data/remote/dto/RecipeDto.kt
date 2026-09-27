package com.cookingnote.app.data.remote.dto

import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.StepEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class IngredientDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "recipe_id") val recipeId: Long = 0,
    @Json(name = "name") val name: String,
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "unit") val unit: String = "",
    @Json(name = "is_optional") val isOptional: Boolean = false,
    @Json(name = "sort_order") val sortOrder: Int = 0
) {
    fun toEntity(assignedRecipeId: Long = recipeId): IngredientEntity =
        IngredientEntity(
            id = id,
            recipeId = assignedRecipeId,
            name = name,
            amount = amount,
            unit = unit,
            isOptional = isOptional,
            sortOrder = sortOrder
        )

    companion object {
        fun fromEntity(entity: IngredientEntity): IngredientDto =
            IngredientDto(
                id = entity.id,
                recipeId = entity.recipeId,
                name = entity.name,
                amount = entity.amount,
                unit = entity.unit,
                isOptional = entity.isOptional,
                sortOrder = entity.sortOrder
            )
    }
}

@JsonClass(generateAdapter = false)
data class StepDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "recipe_id") val recipeId: Long = 0,
    @Json(name = "step_number") val stepNumber: Int,
    @Json(name = "description") val description: String,
    @Json(name = "image_uri") val imageUri: String? = null
) {
    fun toEntity(assignedRecipeId: Long = recipeId): StepEntity =
        StepEntity(
            id = id,
            recipeId = assignedRecipeId,
            stepNumber = stepNumber,
            description = description,
            imageUri = imageUri
        )

    companion object {
        fun fromEntity(entity: StepEntity): StepDto =
            StepDto(
                id = entity.id,
                recipeId = entity.recipeId,
                stepNumber = entity.stepNumber,
                description = entity.description,
                imageUri = entity.imageUri
            )
    }
}

@JsonClass(generateAdapter = false)
data class RecipeDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "category_id") val categoryId: Long? = null,
    @Json(name = "prep_time") val prepTime: Int = 0,
    @Json(name = "cook_time") val cookTime: Int = 0,
    @Json(name = "servings") val servings: Int = 2,
    @Json(name = "difficulty") val difficulty: Int = 1,
    @Json(name = "image_uri") val imageUri: String? = null,
    @Json(name = "is_favorite") val isFavorite: Boolean = false,
    @Json(name = "notes") val notes: String = "",
    @Json(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @Json(name = "last_cooked_at") val lastCookedAt: Long? = null,
    @Json(name = "ingredients") val ingredients: List<IngredientDto> = emptyList(),
    @Json(name = "steps") val steps: List<StepDto> = emptyList()
) {
    fun toEntity(): RecipeEntity =
        RecipeEntity(
            id = id,
            name = name,
            description = description,
            categoryId = categoryId,
            prepTime = prepTime,
            cookTime = cookTime,
            servings = servings,
            difficulty = difficulty,
            imageUri = imageUri,
            isFavorite = isFavorite,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastCookedAt = lastCookedAt
        )

    companion object {
        fun fromEntity(
            recipe: RecipeEntity,
            ingredients: List<IngredientEntity> = emptyList(),
            steps: List<StepEntity> = emptyList()
        ): RecipeDto =
            RecipeDto(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                categoryId = recipe.categoryId,
                prepTime = recipe.prepTime,
                cookTime = recipe.cookTime,
                servings = recipe.servings,
                difficulty = recipe.difficulty,
                imageUri = recipe.imageUri,
                isFavorite = recipe.isFavorite,
                notes = recipe.notes,
                createdAt = recipe.createdAt,
                updatedAt = recipe.updatedAt,
                lastCookedAt = recipe.lastCookedAt,
                ingredients = ingredients.map { IngredientDto.fromEntity(it) },
                steps = steps.map { StepDto.fromEntity(it) }
            )
    }
}
