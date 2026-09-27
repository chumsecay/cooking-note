package com.cookingnote.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecipeMatchDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "cookTimeMinutes") val cookTimeMinutes: Int? = null,
    @Json(name = "difficulty") val difficulty: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "needCount") val needCount: Long = 0,
    @Json(name = "haveCount") val haveCount: Long = 0,
    @Json(name = "coverageRatio") val coverageRatio: Double = 0.0,
    @Json(name = "missingIngredients") val missingIngredients: List<String> = emptyList()
)
