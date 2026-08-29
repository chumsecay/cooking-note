package com.cookingnote.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cookingnote.app.data.dao.AiLogDao
import com.cookingnote.app.data.dao.CategoryDao
import com.cookingnote.app.data.dao.HistoryDao
import com.cookingnote.app.data.dao.IngredientDao
import com.cookingnote.app.data.dao.PantryDao
import com.cookingnote.app.data.dao.RecipeDao
import com.cookingnote.app.data.dao.StepDao
import com.cookingnote.app.data.dao.TagDao
import com.cookingnote.app.data.entity.AiQueryLogEntity
import com.cookingnote.app.data.entity.CategoryEntity
import com.cookingnote.app.data.entity.CookHistoryEntity
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.RecipeTagCrossRef
import com.cookingnote.app.data.entity.StepEntity
import com.cookingnote.app.data.entity.TagEntity
import com.cookingnote.app.data.util.Converters

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        CategoryEntity::class,
        PantryItemEntity::class,
        CookHistoryEntity::class,
        TagEntity::class,
        RecipeTagCrossRef::class,
        AiQueryLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val recipeDao: RecipeDao
    abstract val ingredientDao: IngredientDao
    abstract val stepDao: StepDao
    abstract val categoryDao: CategoryDao
    abstract val pantryDao: PantryDao
    abstract val historyDao: HistoryDao
    abstract val tagDao: TagDao
    abstract val aiLogDao: AiLogDao

    companion object {
        const val DB_NAME = "cookingnote.db"
    }
}
