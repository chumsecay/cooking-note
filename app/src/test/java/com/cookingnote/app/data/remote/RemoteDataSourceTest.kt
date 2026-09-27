package com.cookingnote.app.data.remote

import com.cookingnote.app.data.entity.CookHistoryEntity
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.StepEntity
import com.cookingnote.app.data.remote.api.RecipeApiService
import com.cookingnote.app.data.remote.datasource.DefaultRemoteDataSource
import com.cookingnote.app.data.remote.datasource.RemoteDataSource
import com.cookingnote.app.data.remote.dto.CookLogDto
import com.cookingnote.app.data.remote.dto.IngredientDto
import com.cookingnote.app.data.remote.dto.PantryDto
import com.cookingnote.app.data.remote.dto.RecipeDto
import com.cookingnote.app.data.remote.dto.StepDto
import com.cookingnote.app.data.remote.model.SyncStatus
import com.cookingnote.app.testutil.MainDispatcherRule
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteDataSourceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun moshi_serializesAndDeserializesRecipeDto() {
        val adapter = moshi.adapter(RecipeDto::class.java)

        val recipeDto = RecipeDto(
            id = 100L,
            name = "Phở Gà",
            description = "Món phở truyền thống Hà Nội",
            categoryId = 2L,
            prepTime = 20,
            cookTime = 60,
            servings = 4,
            difficulty = 2,
            imageUri = "https://example.com/pho.jpg",
            isFavorite = true,
            notes = "Dùng gà ta thả vườn",
            ingredients = listOf(
                IngredientDto(id = 1L, recipeId = 100L, name = "Thịt gà", amount = 500.0, unit = "g", isOptional = false, sortOrder = 0)
            ),
            steps = listOf(
                StepDto(id = 1L, recipeId = 100L, stepNumber = 1, description = "Luộc gà với gừng hành")
            )
        )

        val json = adapter.toJson(recipeDto)
        assertNotNull(json)
        assertTrue(json.contains("Phở Gà"))
        assertTrue(json.contains("recipe_id"))
        assertTrue(json.contains("step_number"))

        val deserialized = adapter.fromJson(json)
        assertNotNull(deserialized)
        assertEquals(100L, deserialized?.id)
        assertEquals("Phở Gà", deserialized?.name)
        assertEquals(1, deserialized?.ingredients?.size)
        assertEquals("Thịt gà", deserialized?.ingredients?.first()?.name)
        assertEquals(1, deserialized?.steps?.size)
        assertEquals("Luộc gà với gừng hành", deserialized?.steps?.first()?.description)
    }

    @Test
    fun moshi_serializesAndDeserializesPantryDto() {
        val adapter = moshi.adapter(PantryDto::class.java)
        val pantryDto = PantryDto(
            id = 5L,
            name = "Hạt nêm",
            amount = 200.0,
            unit = "g",
            expiryDate = 1750000000000L,
            lowStockThreshold = 50.0
        )

        val json = adapter.toJson(pantryDto)
        assertTrue(json.contains("low_stock_threshold"))
        assertTrue(json.contains("expiry_date"))

        val deserialized = adapter.fromJson(json)
        assertNotNull(deserialized)
        assertEquals(5L, deserialized?.id)
        assertEquals("Hạt nêm", deserialized?.name)
        assertEquals(200.0, deserialized?.amount ?: 0.0, 0.001)
    }

    @Test
    fun moshi_serializesAndDeserializesCookLogDto() {
        val adapter = moshi.adapter(CookLogDto::class.java)
        val cookLogDto = CookLogDto(
            id = 12L,
            recipeId = 42L,
            cookedAt = 1710000000000L,
            note = "Rất ngon"
        )

        val json = adapter.toJson(cookLogDto)
        assertTrue(json.contains("recipe_id"))
        assertTrue(json.contains("cooked_at"))

        val deserialized = adapter.fromJson(json)
        assertNotNull(deserialized)
        assertEquals(12L, deserialized?.id)
        assertEquals(42L, deserialized?.recipeId)
        assertEquals("Rất ngon", deserialized?.note)
    }

    @Test
    fun dtoToEntityAndBack_conversionsAreAccurate() {
        val recipeEntity = RecipeEntity(
            id = 10L,
            name = "Bún Chả",
            description = "Hà Nội",
            categoryId = 1L,
            prepTime = 15,
            cookTime = 30,
            servings = 2,
            difficulty = 3,
            imageUri = null,
            isFavorite = true,
            notes = "Ăn kèm bún và rau sống",
            createdAt = 1000L,
            updatedAt = 2000L,
            lastCookedAt = 3000L
        )
        val ingredient = IngredientEntity(id = 1L, recipeId = 10L, name = "Thịt ba chỉ", amount = 300.0, unit = "g")
        val step = StepEntity(id = 1L, recipeId = 10L, stepNumber = 1, description = "Nướng thịt than hoa")

        val dto = RecipeDto.fromEntity(recipeEntity, listOf(ingredient), listOf(step))
        assertEquals(recipeEntity.id, dto.id)
        assertEquals(recipeEntity.name, dto.name)
        assertEquals(1, dto.ingredients.size)
        assertEquals(1, dto.steps.size)

        val convertedEntity = dto.toEntity()
        assertEquals(recipeEntity.id, convertedEntity.id)
        assertEquals(recipeEntity.name, convertedEntity.name)
        assertEquals(recipeEntity.isFavorite, convertedEntity.isFavorite)

        val pantryEntity = PantryItemEntity(id = 3L, name = "Nước mắm", amount = 1.0, unit = "chai")
        val pantryDto = PantryDto.fromEntity(pantryEntity)
        assertEquals(pantryEntity.id, pantryDto.id)
        assertEquals(pantryEntity.name, pantryDto.name)
        val reconvertedPantry = pantryDto.toEntity()
        assertEquals(pantryEntity.id, reconvertedPantry.id)
        assertEquals(pantryEntity.name, reconvertedPantry.name)

        val cookHistoryEntity = CookHistoryEntity(id = 7L, recipeId = 10L, cookedAt = 12345L, note = "Hơi ngọt")
        val cookLogDto = CookLogDto.fromEntity(cookHistoryEntity)
        assertEquals(cookHistoryEntity.id, cookLogDto.id)
        assertEquals(cookHistoryEntity.recipeId, cookLogDto.recipeId)
        val reconvertedCookHistory = cookLogDto.toEntity()
        assertEquals(cookHistoryEntity.id, reconvertedCookHistory.id)
        assertEquals(cookHistoryEntity.recipeId, reconvertedCookHistory.recipeId)
    }

    @Test
    fun defaultRemoteDataSource_stubOfflineMode_returnsSuccess() = runTest {
        val dataSource: RemoteDataSource = DefaultRemoteDataSource(apiService = null)

        val syncStatus = dataSource.observeSyncStatus().first()
        assertEquals(SyncStatus.Synced, syncStatus)

        val recipesResult = dataSource.fetchRecipes()
        assertTrue(recipesResult.isSuccess)
        assertTrue(recipesResult.getOrNull()?.isEmpty() == true)

        val dummyRecipe = RecipeDto(name = "Test Recipe")
        val uploadResult = dataSource.uploadRecipe(dummyRecipe)
        assertTrue(uploadResult.isSuccess)
        assertEquals("Test Recipe", uploadResult.getOrNull()?.name)

        val deleteResult = dataSource.deleteRecipe(1L)
        assertTrue(deleteResult.isSuccess)

        val pantryResult = dataSource.fetchPantryItems()
        assertTrue(pantryResult.isSuccess)
        assertTrue(pantryResult.getOrNull()?.isEmpty() == true)

        val cookHistoryResult = dataSource.fetchCookHistory()
        assertTrue(cookHistoryResult.isSuccess)
        assertTrue(cookHistoryResult.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun defaultRemoteDataSource_withApiService_handlesSuccessfulCalls() = runTest {
        val mockApi = mockk<RecipeApiService>()
        val mockRecipe = RecipeDto(id = 1L, name = "Cơm Tấm")

        coEvery { mockApi.getRecipes() } returns Response.success(listOf(mockRecipe))
        coEvery { mockApi.getRecipe(1L) } returns Response.success(mockRecipe)
        coEvery { mockApi.createRecipe(mockRecipe) } returns Response.success(mockRecipe)
        coEvery { mockApi.updateRecipe(1L, mockRecipe) } returns Response.success(mockRecipe)
        coEvery { mockApi.deleteRecipe(1L) } returns Response.success(Unit)

        val dataSource = DefaultRemoteDataSource(apiService = mockApi)

        val recipesResult = dataSource.fetchRecipes()
        assertTrue(recipesResult.isSuccess)
        assertEquals(1, recipesResult.getOrNull()?.size)
        assertEquals("Cơm Tấm", recipesResult.getOrNull()?.first()?.name)

        val recipeResult = dataSource.fetchRecipe(1L)
        assertTrue(recipeResult.isSuccess)
        assertEquals(1L, recipeResult.getOrNull()?.id)

        val uploadResult = dataSource.uploadRecipe(mockRecipe)
        assertTrue(uploadResult.isSuccess)

        val deleteResult = dataSource.deleteRecipe(1L)
        assertTrue(deleteResult.isSuccess)

        val finalSyncStatus = dataSource.observeSyncStatus().first()
        assertEquals(SyncStatus.Synced, finalSyncStatus)
    }

    @Test
    fun defaultRemoteDataSource_withApiServiceError_updatesSyncStatus() = runTest {
        val mockApi = mockk<RecipeApiService>()
        val errorResponseBody = "{\"error\":\"Internal Server Error\"}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { mockApi.getRecipes() } returns Response.error(500, errorResponseBody)

        val dataSource = DefaultRemoteDataSource(apiService = mockApi)

        val recipesResult = dataSource.fetchRecipes()
        assertTrue(recipesResult.isFailure)

        // After failure in runCatching, it transitions to Offline or Error
        val syncStatus = dataSource.observeSyncStatus().first()
        assertTrue(syncStatus == SyncStatus.Offline || syncStatus == SyncStatus.Error)
    }
}
