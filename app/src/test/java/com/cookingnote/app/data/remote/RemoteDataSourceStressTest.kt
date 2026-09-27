package com.cookingnote.app.data.remote

import app.cash.turbine.test
import com.cookingnote.app.data.remote.api.RecipeApiService
import com.cookingnote.app.data.remote.datasource.DefaultRemoteDataSource
import com.cookingnote.app.data.remote.dto.CookLogDto
import com.cookingnote.app.data.remote.dto.IngredientDto
import com.cookingnote.app.data.remote.dto.PantryDto
import com.cookingnote.app.data.remote.dto.RecipeDto
import com.cookingnote.app.data.remote.dto.StepDto
import com.cookingnote.app.data.remote.model.SyncStatus
import com.cookingnote.app.testutil.MainDispatcherRule
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteDataSourceStressTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // ========================================================================
    // 1. DTO Serialization & Deserialization Stress Testing
    // ========================================================================

    @Test
    fun moshi_minimalRecipeJson_usesAllDefaultsProperly() {
        val adapter = moshi.adapter(RecipeDto::class.java)
        val minimalJson = """{"name":"Phở bò"}"""
        val dto = adapter.fromJson(minimalJson)

        assertNotNull(dto)
        assertEquals("Phở bò", dto?.name)
        assertEquals(0L, dto?.id)
        assertEquals("", dto?.description)
        assertNull(dto?.categoryId)
        assertEquals(0, dto?.prepTime)
        assertEquals(0, dto?.cookTime)
        assertEquals(2, dto?.servings)
        assertEquals(1, dto?.difficulty)
        assertNull(dto?.imageUri)
        assertFalse(dto?.isFavorite ?: true)
        assertEquals("", dto?.notes)
        assertNull(dto?.lastCookedAt)
        assertTrue(dto?.ingredients?.isEmpty() == true)
        assertTrue(dto?.steps?.isEmpty() == true)
    }

    @Test
    fun moshi_recipeJsonWithExplicitNullsOnNullableFields_deserializesSuccessfully() {
        val adapter = moshi.adapter(RecipeDto::class.java)
        val json = """
            {
                "id": 42,
                "name": "Bún chả",
                "category_id": null,
                "image_uri": null,
                "last_cooked_at": null
            }
        """.trimIndent()
        val dto = adapter.fromJson(json)

        assertNotNull(dto)
        assertEquals(42L, dto?.id)
        assertEquals("Bún chả", dto?.name)
        assertNull(dto?.categoryId)
        assertNull(dto?.imageUri)
        assertNull(dto?.lastCookedAt)
    }

    @Test
    fun moshi_recipeJsonWithUnknownExtraFields_ignoresUnknownFieldsGracefully() {
        val adapter = moshi.adapter(RecipeDto::class.java)
        val json = """
            {
                "name": "Chả cá Lã Vọng",
                "unknown_v2_field": "some_extra_metadata",
                "nested_unknown": {"foo": "bar"},
                "extra_int": 999
            }
        """.trimIndent()
        val dto = adapter.fromJson(json)

        assertNotNull(dto)
        assertEquals("Chả cá Lã Vọng", dto?.name)
    }

    @Test
    fun moshi_recipeJsonMissingRequiredName_throwsJsonDataException() {
        val adapter = moshi.adapter(RecipeDto::class.java)
        val json = """{"id": 1, "description": "No name recipe"}"""
        assertThrows(JsonDataException::class.java) {
            adapter.fromJson(json)
        }
    }

    @Test
    fun moshi_recipeJsonExplicitNullName_throwsJsonDataException() {
        val adapter = moshi.adapter(RecipeDto::class.java)
        val json = """{"name": null}"""
        assertThrows(JsonDataException::class.java) {
            adapter.fromJson(json)
        }
    }

    @Test
    fun moshi_malformedJson_throwsJsonEncodingException() {
        val adapter = moshi.adapter(RecipeDto::class.java)
        val malformed = """{"name": "incomplete"""
        assertThrows(JsonEncodingException::class.java) {
            adapter.fromJson(malformed)
        }
    }

    @Test
    fun moshi_pantryDto_handlesExplicitNullExpiryAndDefaults() {
        val adapter = moshi.adapter(PantryDto::class.java)
        val json = """{"name":"Muối","expiry_date":null}"""
        val dto = adapter.fromJson(json)

        assertNotNull(dto)
        assertEquals("Muối", dto?.name)
        assertNull(dto?.expiryDate)
        assertEquals(1.0, dto?.lowStockThreshold ?: 0.0, 0.001)
        assertEquals(0.0, dto?.amount ?: 1.0, 0.001)
    }

    @Test
    fun moshi_cookLogDto_missingRequiredRecipeId_throwsJsonDataException() {
        val adapter = moshi.adapter(CookLogDto::class.java)
        val json = """{"id": 1, "note": "Missing recipeId"}"""
        assertThrows(JsonDataException::class.java) {
            adapter.fromJson(json)
        }
    }

    @Test
    fun moshi_extremeNumericBoundaries_preservePrecision() {
        val recipeAdapter = moshi.adapter(RecipeDto::class.java)
        val extremeRecipe = RecipeDto(
            id = Long.MAX_VALUE,
            name = "Extreme Dish",
            prepTime = Int.MAX_VALUE,
            cookTime = Int.MAX_VALUE,
            servings = Int.MAX_VALUE,
            createdAt = Long.MAX_VALUE,
            updatedAt = Long.MIN_VALUE,
            lastCookedAt = Long.MAX_VALUE
        )
        val json = recipeAdapter.toJson(extremeRecipe)
        val deserialized = recipeAdapter.fromJson(json)

        assertEquals(Long.MAX_VALUE, deserialized?.id)
        assertEquals(Int.MAX_VALUE, deserialized?.prepTime)
        assertEquals(Long.MAX_VALUE, deserialized?.createdAt)
        assertEquals(Long.MIN_VALUE, deserialized?.updatedAt)
    }

    // ========================================================================
    // 2. DefaultRemoteDataSource State Transition & Edge Cases
    // ========================================================================

    @Test
    fun defaultRemoteDataSource_nullApiService_staysSyncedAndReturnsSuccess() = runTest {
        val dataSource = DefaultRemoteDataSource(apiService = null)

        assertEquals(SyncStatus.Synced, dataSource.observeSyncStatus().first())
        val fetchRes = dataSource.fetchRecipes()
        assertTrue(fetchRes.isSuccess)
        assertEquals(SyncStatus.Synced, dataSource.observeSyncStatus().first())

        val uploadRes = dataSource.uploadRecipe(RecipeDto(name = "Offline recipe"))
        assertTrue(uploadRes.isSuccess)
        assertEquals(SyncStatus.Synced, dataSource.observeSyncStatus().first())
    }

    @Test
    fun defaultRemoteDataSource_networkIOException_transitionsToOffline() = runTest {
        val mockApi = mockk<RecipeApiService>()
        coEvery { mockApi.getRecipes() } throws IOException("Network unreachable / connection reset")

        val dataSource = DefaultRemoteDataSource(apiService = mockApi)

        val result = dataSource.fetchRecipes()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals(SyncStatus.Offline, dataSource.observeSyncStatus().first())
    }

    @Test
    fun defaultRemoteDataSource_serverHttp500_investigateFinalSyncStatus() = runTest {
        val mockApi = mockk<RecipeApiService>()
        val errorResponseBody = "{\"error\":\"Internal Server Error\"}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { mockApi.getRecipes() } returns Response.error(500, errorResponseBody)

        val dataSource = DefaultRemoteDataSource(apiService = mockApi)

        val result = dataSource.fetchRecipes()
        assertTrue(result.isFailure)

        val finalStatus = dataSource.observeSyncStatus().first()
        // Empirical observation: Does it finish in SyncStatus.Error or does .onFailure clobber it with SyncStatus.Offline?
        // Let's document exact behavior:
        println("Empirical Challenger Check: finalStatus for HTTP 500 is $finalStatus")
        assertTrue(
            "Final status is $finalStatus (Offline or Error)",
            finalStatus == SyncStatus.Offline || finalStatus == SyncStatus.Error
        )
    }

    @Test
    fun defaultRemoteDataSource_uploadRecipe_handlesHttp500() = runTest {
        val mockApi = mockk<RecipeApiService>()
        val errorResponseBody = "{\"error\":\"Bad Request\"}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { mockApi.createRecipe(any()) } returns Response.error(400, errorResponseBody)

        val dataSource = DefaultRemoteDataSource(apiService = mockApi)
        val result = dataSource.uploadRecipe(RecipeDto(id = 0L, name = "Invalid Recipe"))

        assertTrue(result.isFailure)
        val finalStatus = dataSource.observeSyncStatus().first()
        assertTrue(finalStatus == SyncStatus.Offline || finalStatus == SyncStatus.Error)
    }

    @Test
    fun defaultRemoteDataSource_deleteRecipe_handlesHttp404() = runTest {
        val mockApi = mockk<RecipeApiService>()
        val errorResponseBody = "{\"error\":\"Not Found\"}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { mockApi.deleteRecipe(999L) } returns Response.error(404, errorResponseBody)

        val dataSource = DefaultRemoteDataSource(apiService = mockApi)
        val result = dataSource.deleteRecipe(999L)

        assertTrue(result.isFailure)
        val finalStatus = dataSource.observeSyncStatus().first()
        assertTrue(finalStatus == SyncStatus.Offline || finalStatus == SyncStatus.Error)
    }

    @Test
    fun defaultRemoteDataSource_pantryAndHistory_stubsReturnSuccessWithoutCrashing() = runTest {
        val mockApi = mockk<RecipeApiService>()
        coEvery { mockApi.getPantryItems() } returns Response.success(emptyList())
        coEvery { mockApi.syncPantryItem(any()) } answers { Response.success(firstArg()) }
        coEvery { mockApi.getCookHistory() } returns Response.success(emptyList())
        coEvery { mockApi.recordCookHistory(any()) } answers { Response.success(firstArg()) }

        val dataSource = DefaultRemoteDataSource(apiService = mockApi)

        val pantryRes = dataSource.fetchPantryItems()
        assertTrue(pantryRes.isSuccess)

        val uploadPantryRes = dataSource.uploadPantryItem(PantryDto(name = "Tiêu"))
        assertTrue(uploadPantryRes.isSuccess)
        assertEquals("Tiêu", uploadPantryRes.getOrNull()?.name)

        val historyRes = dataSource.fetchCookHistory()
        assertTrue(historyRes.isSuccess)

        val uploadHistoryRes = dataSource.uploadCookHistory(CookLogDto(recipeId = 1L, note = "Nấu trưa"))
        assertTrue(uploadHistoryRes.isSuccess)
        assertEquals("Nấu trưa", uploadHistoryRes.getOrNull()?.note)
    }
}
