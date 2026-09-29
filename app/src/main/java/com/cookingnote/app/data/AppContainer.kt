package com.cookingnote.app.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cookingnote.app.ai.AiService
import com.cookingnote.app.ai.DefaultAiService
import com.cookingnote.app.data.database.AppDatabase
import com.cookingnote.app.data.prefs.AiCloudDefaults
import com.cookingnote.app.data.prefs.AiRemoteConfig
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.prefs.UserPrefsStore
import com.cookingnote.app.data.remote.datasource.DefaultRemoteDataSource
import com.cookingnote.app.data.remote.datasource.RemoteDataSource
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.data.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        AppDatabase.DB_NAME
    )
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch {
                    database.withTransaction {
                        SeedData.populate(
                            categoryDao = database.categoryDao,
                            recipeDao = database.recipeDao,
                            ingredientDao = database.ingredientDao,
                            stepDao = database.stepDao,
                            tagDao = database.tagDao,
                            pantryDao = database.pantryDao,
                            historyDao = database.historyDao
                        )
                    }
                }
            }
        })
        .build()

    val userPrefs = UserPrefsStore(appContext)
    val aiSettings = AiSettingsStore(appContext)
    val userSession = com.cookingnote.app.data.prefs.UserSessionStore(appContext)

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    private val authOkHttpClient: okhttp3.OkHttpClient by lazy {
        okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = userSession.session.value.token
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    private val backendBaseUrl: String
        get() = "http://10.0.2.2:8080/"

    private val retrofit: retrofit2.Retrofit by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(backendBaseUrl)
            .client(authOkHttpClient)
            .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
            .build()
    }

    private val recipeApiService: com.cookingnote.app.data.remote.api.RecipeApiService by lazy {
        retrofit.create(com.cookingnote.app.data.remote.api.RecipeApiService::class.java)
    }

    private val authApiService: com.cookingnote.app.data.remote.api.AuthApiService by lazy {
        retrofit.create(com.cookingnote.app.data.remote.api.AuthApiService::class.java)
    }

    val remoteDataSource: RemoteDataSource = DefaultRemoteDataSource(
        apiService = recipeApiService,
        authApiService = authApiService
    )

    val repository = CookbookRepository(
        recipeDao = database.recipeDao,
        ingredientDao = database.ingredientDao,
        stepDao = database.stepDao,
        categoryDao = database.categoryDao,
        pantryDao = database.pantryDao,
        historyDao = database.historyDao,
        tagDao = database.tagDao,
        aiLogDao = database.aiLogDao,
        chatDao = database.chatDao,
        remoteDataSource = remoteDataSource
    )

    val aiService: AiService = DefaultAiService(
        settingsStore = aiSettings,
        repository = repository
    )

    fun databaseFile(): java.io.File =
        appContext.getDatabasePath(AppDatabase.DB_NAME)

    init {
        refreshCloudConfig()
        ensurePantrySeeded()
    }

    private fun ensurePantrySeeded() {
        scope.launch {
            runCatching {
                // Chỉ bổ sung nếu DB đã tồn tại công thức từ trước nhưng pantry bị rỗng (tránh race condition với onCreate)
                if (database.recipeDao.observeAllSnapshot().isNotEmpty() && database.pantryDao.getAll().isEmpty()) {
                    SeedData.pantry.forEach { database.pantryDao.upsert(it) }
                }
            }
        }
    }

    /**
     * Tải cấu hình AI từ xa (nếu bật trong [AiCloudDefaults]) rồi merge vào
     * [aiSettings]. Chạy fire-and-forget: offline-first, mất mạng thì giữ
     * nguyên cấu hình sẵn trong code. JSON remote không bao giờ chứa API key.
     */
    private fun refreshCloudConfig() {
        if (!AiCloudDefaults.REMOTE_ENABLED) return
        val url = AiCloudDefaults.REMOTE_CONFIG_URL
        if (url.isBlank()) return
        scope.launch {
            val remote = AiRemoteConfig.fetch(url)
            if (remote != null) {
                runCatching { aiSettings.applyRemoteConfig(remote) }
            }
        }
    }
}