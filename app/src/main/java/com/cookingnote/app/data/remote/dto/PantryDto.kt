package com.cookingnote.app.data.remote.dto

import com.cookingnote.app.data.entity.PantryItemEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class PantryDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String,
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "unit") val unit: String = "",
    @Json(name = "expiry_date") val expiryDate: Long? = null,
    @Json(name = "low_stock_threshold") val lowStockThreshold: Double = 1.0
) {
    fun toEntity(): PantryItemEntity =
        PantryItemEntity(
            id = id,
            name = name,
            amount = amount,
            unit = unit,
            expiryDate = expiryDate,
            lowStockThreshold = lowStockThreshold
        )

    companion object {
        fun fromEntity(entity: PantryItemEntity): PantryDto =
            PantryDto(
                id = entity.id,
                name = entity.name,
                amount = entity.amount,
                unit = entity.unit,
                expiryDate = entity.expiryDate,
                lowStockThreshold = entity.lowStockThreshold
            )
    }
}
