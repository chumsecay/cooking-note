package com.cookingnote.app.data.util

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        value?.joinToString(separator = "\u0001") ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        value?.takeIf { it.isNotBlank() }?.split("\u0001") ?: emptyList()
}
