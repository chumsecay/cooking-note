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
                            pantryDao = database.pantryDao
                        )
                    }
                }
            }
        })
        .build()

    val userPrefs = UserPrefsStore(appContext)
    val aiSettings = AiSettingsStore(appContext)

    val repository = CookbookRepository(
        recipeDao = database.recipeDao,
        ingredientDao = database.ingredientDao,
        stepDao = database.stepDao,
        categoryDao = database.categoryDao,
        pantryDao = database.pantryDao,
        historyDao = database.historyDao,
        tagDao = database.tagDao,
        aiLogDao = database.aiLogDao,
        chatDao = database.chatDao
    )

    val aiService: AiService = DefaultAiService(
        settingsStore = aiSettings,
        repository = repository
    )

    fun databaseFile(): java.io.File =
        appContext.getDatabasePath(AppDatabase.DB_NAME)

    init {
        refreshCloudConfig()
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