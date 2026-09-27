package com.cookingnote.app.data.remote.datasource

import com.cookingnote.app.data.remote.dto.CookLogDto
import com.cookingnote.app.data.remote.dto.PantryDto
import com.cookingnote.app.data.remote.dto.RecipeDto
import com.cookingnote.app.data.remote.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    suspend fun fetchRecipes(): Result<List<RecipeDto>>
    suspend fun fetchRecipe(id: Long): Result<RecipeDto>
    suspend fun uploadRecipe(recipe: RecipeDto): Result<RecipeDto>
    suspend fun deleteRecipe(id: Long): Result<Unit>
    suspend fun fetchPantryItems(): Result<List<PantryDto>>
    suspend fun uploadPantryItem(item: PantryDto): Result<PantryDto>
    suspend fun fetchCookHistory(): Result<List<CookLogDto>>
    suspend fun uploadCookHistory(history: CookLogDto): Result<CookLogDto>
    suspend fun login(email: String, password: String): Result<com.cookingnote.app.data.remote.dto.AuthResponseDto>
    suspend fun register(email: String, password: String, fullName: String): Result<com.cookingnote.app.data.remote.dto.AuthResponseDto>
    suspend fun getProfile(): Result<com.cookingnote.app.data.remote.dto.UserProfileDto>
    suspend fun matchPantry(limit: Int = 20): Result<List<com.cookingnote.app.data.remote.dto.RecipeMatchDto>>
    fun observeSyncStatus(): Flow<SyncStatus>
}
