package com.cookingnote.app.data.remote.api

import com.cookingnote.app.data.remote.dto.CookLogDto
import com.cookingnote.app.data.remote.dto.PantryDto
import com.cookingnote.app.data.remote.dto.RecipeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecipeApiService {
    @GET("api/v2/recipes")
    suspend fun getRecipes(): Response<List<RecipeDto>>

    @GET("api/v2/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Long): Response<RecipeDto>

    @POST("api/v2/recipes")
    suspend fun createRecipe(@Body recipe: RecipeDto): Response<RecipeDto>

    @PUT("api/v2/recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: Long, @Body recipe: RecipeDto): Response<RecipeDto>

    @DELETE("api/v2/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Long): Response<Unit>

    @GET("api/v2/pantry")
    suspend fun getPantryItems(): Response<List<PantryDto>>

    @POST("api/v2/pantry")
    suspend fun syncPantryItem(@Body item: PantryDto): Response<PantryDto>

    @GET("api/v2/history")
    suspend fun getCookHistory(): Response<List<CookLogDto>>

    @POST("api/v2/history")
    suspend fun recordCookHistory(@Body history: CookLogDto): Response<CookLogDto>

    @GET("api/v1/recipes/match-pantry")
    suspend fun matchPantry(@retrofit2.http.Query("limit") limit: Int = 20): Response<List<com.cookingnote.app.data.remote.dto.RecipeMatchDto>>
}
