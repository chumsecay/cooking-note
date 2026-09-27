package com.cookingnote.app.data.remote.datasource

import com.cookingnote.app.data.remote.api.RecipeApiService
import com.cookingnote.app.data.remote.dto.CookLogDto
import com.cookingnote.app.data.remote.dto.PantryDto
import com.cookingnote.app.data.remote.dto.RecipeDto
import com.cookingnote.app.data.remote.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class DefaultRemoteDataSource(
    private val apiService: RecipeApiService? = null,
    private val authApiService: com.cookingnote.app.data.remote.api.AuthApiService? = null
) : RemoteDataSource {

    private val _syncStatus = MutableStateFlow(SyncStatus.Synced)

    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    override suspend fun fetchRecipes(): Result<List<RecipeDto>> = withContext(Dispatchers.IO) {
        if (apiService == null) {
            // Offline-first stub: simulate successful empty sync or offline availability
            return@withContext Result.success(emptyList())
        }
        _syncStatus.value = SyncStatus.Syncing
        runCatching {
            val response = apiService.getRecipes()
            if (response.isSuccessful) {
                _syncStatus.value = SyncStatus.Synced
                response.body() ?: emptyList()
            } else {
                _syncStatus.value = SyncStatus.Error
                throw Exception("HTTP ${response.code()}: ${response.message()}")
            }
        }.onFailure {
            _syncStatus.value = SyncStatus.Offline
        }
    }

    override suspend fun fetchRecipe(id: Long): Result<RecipeDto> = withContext(Dispatchers.IO) {
        if (apiService == null) {
            return@withContext Result.failure(NoSuchElementException("Remote recipe not found for id $id"))
        }
        runCatching {
            val response = apiService.getRecipe(id)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw NoSuchElementException("Recipe $id not found on remote")
            }
        }
    }

    override suspend fun uploadRecipe(recipe: RecipeDto): Result<RecipeDto> = withContext(Dispatchers.IO) {
        if (apiService == null) {
            // Offline-first stub: accept upload locally
            return@withContext Result.success(recipe)
        }
        _syncStatus.value = SyncStatus.Syncing
        runCatching {
            val response = if (recipe.id == 0L) {
                apiService.createRecipe(recipe)
            } else {
                apiService.updateRecipe(recipe.id, recipe)
            }
            if (response.isSuccessful && response.body() != null) {
                _syncStatus.value = SyncStatus.Synced
                response.body()!!
            } else {
                _syncStatus.value = SyncStatus.Error
                throw Exception("Failed to upload recipe: HTTP ${response.code()}")
            }
        }.onFailure {
            _syncStatus.value = SyncStatus.Offline
        }
    }

    override suspend fun deleteRecipe(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        if (apiService == null) {
            return@withContext Result.success(Unit)
        }
        _syncStatus.value = SyncStatus.Syncing
        runCatching {
            val response = apiService.deleteRecipe(id)
            if (response.isSuccessful) {
                _syncStatus.value = SyncStatus.Synced
                Unit
            } else {
                _syncStatus.value = SyncStatus.Error
                throw Exception("Failed to delete recipe: HTTP ${response.code()}")
            }
        }.onFailure {
            _syncStatus.value = SyncStatus.Offline
        }
    }

    override suspend fun fetchPantryItems(): Result<List<PantryDto>> = withContext(Dispatchers.IO) {
        if (apiService == null) {
            return@withContext Result.success(emptyList())
        }
        runCatching {
            val response = apiService.getPantryItems()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("HTTP ${response.code()}")
            }
        }
    }

    override suspend fun uploadPantryItem(item: PantryDto): Result<PantryDto> = withContext(Dispatchers.IO) {
        if (apiService == null) {
            return@withContext Result.success(item)
        }
        runCatching {
            val response = apiService.syncPantryItem(item)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("HTTP ${response.code()}")
            }
        }
    }

    override suspend fun fetchCookHistory(): Result<List<CookLogDto>> = withContext(Dispatchers.IO) {
        if (apiService == null) {
            return@withContext Result.success(emptyList())
        }
        runCatching {
            val response = apiService.getCookHistory()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("HTTP ${response.code()}")
            }
        }
    }

    override suspend fun uploadCookHistory(history: CookLogDto): Result<CookLogDto> = withContext(Dispatchers.IO) {
        if (apiService == null) {
            return@withContext Result.success(history)
        }
        runCatching {
            val response = apiService.recordCookHistory(history)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("HTTP ${response.code()}")
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<com.cookingnote.app.data.remote.dto.AuthResponseDto> =
        withContext(Dispatchers.IO) {
            if (authApiService == null) {
                return@withContext Result.failure(IllegalStateException("AuthApiService chưa được cấu hình."))
            }
            runCatching {
                val resp = authApiService.login(com.cookingnote.app.data.remote.dto.LoginRequestDto(email, password))
                if (resp.isSuccessful && resp.body() != null) {
                    resp.body()!!
                } else {
                    val errorMsg = resp.errorBody()?.string()?.take(200) ?: "HTTP ${resp.code()}"
                    throw Exception("Đăng nhập thất bại: $errorMsg")
                }
            }
        }

    override suspend fun register(email: String, password: String, fullName: String): Result<com.cookingnote.app.data.remote.dto.AuthResponseDto> =
        withContext(Dispatchers.IO) {
            if (authApiService == null) {
                return@withContext Result.failure(IllegalStateException("AuthApiService chưa được cấu hình."))
            }
            runCatching {
                val resp = authApiService.register(com.cookingnote.app.data.remote.dto.RegisterRequestDto(email, password, fullName))
                if (resp.isSuccessful && resp.body() != null) {
                    resp.body()!!
                } else {
                    val errorMsg = resp.errorBody()?.string()?.take(200) ?: "HTTP ${resp.code()}"
                    throw Exception("Đăng ký thất bại: $errorMsg")
                }
            }
        }

    override suspend fun getProfile(): Result<com.cookingnote.app.data.remote.dto.UserProfileDto> =
        withContext(Dispatchers.IO) {
            if (authApiService == null) {
                return@withContext Result.failure(IllegalStateException("AuthApiService chưa được cấu hình."))
            }
            runCatching {
                val resp = authApiService.getProfile()
                if (resp.isSuccessful && resp.body() != null) {
                    resp.body()!!
                } else {
                    throw Exception("Lỗi tải hồ sơ: HTTP ${resp.code()}")
                }
            }
        }

    override suspend fun matchPantry(limit: Int): Result<List<com.cookingnote.app.data.remote.dto.RecipeMatchDto>> =
        withContext(Dispatchers.IO) {
            if (apiService == null) {
                return@withContext Result.success(emptyList())
            }
            runCatching {
                val resp = apiService.matchPantry(limit)
                if (resp.isSuccessful) {
                    resp.body() ?: emptyList()
                } else {
                    throw Exception("HTTP ${resp.code()}")
                }
            }
        }
}
