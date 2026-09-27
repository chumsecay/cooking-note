package com.cookingnote.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "fullName") val fullName: String
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "token") val token: String,
    @Json(name = "refreshToken") val refreshToken: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "fullName") val fullName: String? = null,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "email") val email: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "role") val role: String? = null,
    @Json(name = "dietaryPreferences") val dietaryPreferences: String? = null
)
