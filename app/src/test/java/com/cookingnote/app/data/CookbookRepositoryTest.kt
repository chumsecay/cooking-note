package com.cookingnote.app.data

import com.cookingnote.app.data.dao.AiLogDao
import com.cookingnote.app.data.dao.CategoryDao
import com.cookingnote.app.data.dao.ChatMessageDao
import com.cookingnote.app.data.dao.HistoryDao
import com.cookingnote.app.data.dao.IngredientDao
import com.cookingnote.app.data.dao.PantryDao
import com.cookingnote.app.data.dao.RecipeDao
import com.cookingnote.app.data.dao.StepDao
import com.cookingnote.app.data.dao.TagDao
import com.cookingnote.app.data.entity.ChatMessageEntity
import com.cookingnote.app.data.entity.CookHistoryEntity
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.StepEntity
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CookbookRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var recipeDao: RecipeDao
    private lateinit var ingredientDao: IngredientDao
    private lateinit var stepDao: StepDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var pantryDao: PantryDao
    private lateinit var historyDao: HistoryDao
    private lateinit var tagDao: TagDao
    private lateinit var aiLogDao: AiLogDao
    private lateinit var chatDao: ChatMessageDao

    private lateinit var repository: CookbookRepository

    @Before
    fun setUp() {
        recipeDao = mockk(relaxed = true)
        ingredientDao = mockk(relaxed = true)
        stepDao = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        pantryDao = mockk(relaxed = true)
        historyDao = mockk(relaxed = true)
        tagDao = mockk(relaxed = true)
        aiLogDao = mockk(relaxed = true)
        chatDao = mockk(relaxed = true)

        repository = CookbookRepository(
            recipeDao = recipeDao,
            ingredientDao = ingredientDao,
            stepDao = stepDao,
            categoryDao = categoryDao,
            pantryDao = pantryDao,
            historyDao = historyDao,
            tagDao = tagDao,
            aiLogDao = aiLogDao,
            chatDao = chatDao
        )
    }

    @Test
    fun saveRecipe_insertsRecipeAndReplacesIngredientsAndSteps() = runTest {
        val recipe = RecipeEntity(id = 0, name = "Phở Bò", description = "Món phở truyền thống")
        val ingredients = listOf(
            IngredientEntity(recipeId = 0L, name = "Thịt bò", amount = 200.0, unit = "g"),
            IngredientEntity(recipeId = 0L, name = "Bánh phở", amount = 300.0, unit = "g")
        )
        val steps = listOf(
            StepEntity(recipeId = 0L, stepNumber = 1, description = "Nấu nước dùng"),
            StepEntity(recipeId = 0L, stepNumber = 2, description = "Trần bánh phở và xếp thịt")
        )

        coEvery { recipeDao.upsert(recipe) } returns 42L

        val savedId = repository.saveRecipe(recipe, ingredients, steps)

        assertEquals(42L, savedId)
        coVerify(exactly = 1) { recipeDao.upsert(recipe) }
        coVerify(exactly = 1) { ingredientDao.deleteByRecipe(42L) }
        coVerify(exactly = 1) { stepDao.deleteByRecipe(42L) }
        coVerify(exactly = 1) {
            ingredientDao.upsertAll(match { list ->
                list.size == 2 && list[0].recipeId == 42L && list[0].sortOrder == 0 &&
                    list[1].recipeId == 42L && list[1].sortOrder == 1
            })
        }
        coVerify(exactly = 1) {
            stepDao.upsertAll(match { list ->
                list.size == 2 && list[0].recipeId == 42L && list[0].stepNumber == 1 &&
                    list[1].recipeId == 42L && list[1].stepNumber == 2
            })
        }
    }

    @Test
    fun markCooked_updatesRecipeAndInsertsCookHistory() = runTest {
        val recipeId = 15L
        val note = "Rất ngon, vừa miệng"

        repository.markCooked(recipeId, note)

        coVerify(exactly = 1) { recipeDao.markCooked(recipeId, any()) }
        coVerify(exactly = 1) {
            historyDao.insert(match { entry ->
                entry.recipeId == recipeId && entry.note == note
            })
        }
    }

    @Test
    fun setFavorite_delegatesToRecipeDao() = runTest {
        repository.setFavorite(99L, true)
        coVerify(exactly = 1) { recipeDao.setFavorite(99L, true, any()) }

        repository.setFavorite(99L, false)
        coVerify(exactly = 1) { recipeDao.setFavorite(99L, false, any()) }
    }

    @Test
    fun deleteRecipe_delegatesToRecipeDao() = runTest {
        repository.deleteRecipe(77L)
        coVerify(exactly = 1) { recipeDao.deleteById(77L) }
    }

    @Test
    fun suggestFromPantry_whenPantryHasItems_queriesMatchingRecipes() = runTest {
        val pantryItems = listOf(
            PantryItemEntity(name = "Trứng", amount = 4.0, unit = "quả"),
            PantryItemEntity(name = "Cà chua", amount = 2.0, unit = "quả")
        )
        val matchingRecipes = listOf(
            RecipeEntity(id = 1, name = "Trứng sốt cà chua")
        )

        coEvery { pantryDao.getAll() } returns pantryItems
        coEvery { recipeDao.findByIngredientNames(listOf("trứng", "cà chua"), minMatch = 1) } returns matchingRecipes

        val result = repository.suggestFromPantry()

        assertEquals(1, result.size)
        assertEquals("Trứng sốt cà chua", result.first().name)
        coVerify(exactly = 1) { recipeDao.findByIngredientNames(listOf("trứng", "cà chua"), minMatch = 1) }
    }

    @Test
    fun chatOperations_appendAndRetrieveMessagesCorrectly() = runTest {
        val messageList = listOf(
            ChatMessageEntity(id = 1, role = "user", content = "Gợi ý món"),
            ChatMessageEntity(id = 2, role = "assistant", content = "Bạn có thể nấu canh chua")
        )

        coEvery { chatDao.recent(10) } returns messageList.reversed()

        repository.appendMessage("user", "Gợi ý món")
        coVerify(exactly = 1) {
            chatDao.insert(match { it.role == "user" && it.content == "Gợi ý món" })
        }

        val retrieved = repository.recentMessages(10)
        assertEquals(2, retrieved.size)
        assertEquals("user", retrieved[0].role)
        assertEquals("assistant", retrieved[1].role)

        repository.clearChat()
        coVerify(exactly = 1) { chatDao.clear() }
    }
}
