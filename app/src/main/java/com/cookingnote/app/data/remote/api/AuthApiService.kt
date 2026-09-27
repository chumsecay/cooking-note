package com.cookingnote.app.data.remote.api

import com.cookingnote.app.data.remote.dto.AuthResponseDto
import com.cookingnote.app.data.remote.dto.LoginRequestDto
import com.cookingnote.app.data.remote.dto.RegisterRequestDto
import com.cookingnote.app.data.remote.dto.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @GET("api/v1/auth/me")
    suspend fun getProfile(): Response<UserProfileDto>
}
