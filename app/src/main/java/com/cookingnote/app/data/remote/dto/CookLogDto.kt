package com.cookingnote.app.data.remote.dto

import com.cookingnote.app.data.entity.CookHistoryEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class CookLogDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "recipe_id") val recipeId: Long,
    @Json(name = "cooked_at") val cookedAt: Long = System.currentTimeMillis(),
    @Json(name = "note") val note: String = ""
) {
    fun toEntity(): CookHistoryEntity =
        CookHistoryEntity(
            id = id,
            recipeId = recipeId,
            cookedAt = cookedAt,
            note = note
        )

    companion object {
        fun fromEntity(entity: CookHistoryEntity): CookLogDto =
            CookLogDto(
                id = entity.id,
                recipeId = entity.recipeId,
                cookedAt = entity.cookedAt,
                note = entity.note
            )
    }
}
