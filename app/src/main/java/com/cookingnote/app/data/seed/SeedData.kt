package com.cookingnote.app.data.seed

import com.cookingnote.app.data.dao.CategoryDao
import com.cookingnote.app.data.dao.HistoryDao
import com.cookingnote.app.data.dao.IngredientDao
import com.cookingnote.app.data.dao.PantryDao
import com.cookingnote.app.data.dao.RecipeDao
import com.cookingnote.app.data.dao.StepDao
import com.cookingnote.app.data.dao.TagDao
import com.cookingnote.app.data.entity.CategoryEntity
import com.cookingnote.app.data.entity.CookHistoryEntity
import com.cookingnote.app.data.entity.IngredientEntity
import com.cookingnote.app.data.entity.PantryItemEntity
import com.cookingnote.app.data.entity.RecipeEntity
import com.cookingnote.app.data.entity.RecipeTagCrossRef
import com.cookingnote.app.data.entity.StepEntity
import com.cookingnote.app.data.entity.TagEntity

data class SeedRecipe(
    val name: String,
    val description: String,
    val category: String,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val difficulty: Int,
    val ingredients: List<Triple<String, Double, String>>,
    val steps: List<String>,
    val tags: List<String>,
    val favorite: Boolean = false
)

object SeedData {
    val categories = listOf(
        CategoryEntity(name = "Món chính", icon = "dinner_dining", color = 0xFFFF6B35),
        CategoryEntity(name = "Món canh", icon = "soup_kitchen", color = 0xFF34C759),
        CategoryEntity(name = "Món chay", icon = "eco", color = 0xFF30D158),
        CategoryEntity(name = "Món nhanh", icon = "bolt", color = 0xFFFF9500),
        CategoryEntity(name = "Tráng miệng", icon = "cake", color = 0xFFFF2D55),
        CategoryEntity(name = "Đồ uống", icon = "local_cafe", color = 0xFF5856D6)
    )

    val recipes = listOf(
        SeedRecipe(
            name = "Phở bò",
            description = "Phở bò Hà Nội với nước dùng trong, thơm gừng hành.",
            category = "Món chính",
            prepTime = 30, cookTime = 180, servings = 4, difficulty = 3,
            ingredients = listOf(
                Triple("Xương bò", 1.0, "kg"),
                Triple("Thịt bò", 400.0, "g"),
                Triple("Bánh phở", 500.0, "g"),
                Triple("Hành tây", 2.0, "củ"),
                Triple("Gừng", 50.0, "g"),
                Triple("Hoa hồi", 3.0, "cái"),
                Triple("Quế", 1.0, "thanh"),
                Triple("Nước mắm", 3.0, "muỗng")
            ),
            steps = listOf(
                "Chần xương bò, rửa sạch rồi ninh với nước lạnh.",
                "Nướng hành tây và gừng đến thơm, cho vào nồi nước dùng.",
                "Thêm hoa hồi, quế, ninh nhỏ lửa 2–3 giờ.",
                "Trụng bánh phở, xếp thịt bò, chan nước dùng, thêm hành ngò."
            ),
            tags = listOf("bắc", "nóng", "gia đình"),
            favorite = true
        ),
        SeedRecipe(
            name = "Bún chả",
            description = "Thịt nướng than hoa, nước chấm chua ngọt kiểu Hà Nội.",
            category = "Món chính",
            prepTime = 25, cookTime = 20, servings = 3, difficulty = 2,
            ingredients = listOf(
                Triple("Thịt ba chỉ", 400.0, "g"),
                Triple("Thịt nạc vai", 200.0, "g"),
                Triple("Bún tươi", 500.0, "g"),
                Triple("Đu đủ xanh", 200.0, "g"),
                Triple("Nước mắm", 4.0, "muỗng"),
                Triple("Đường", 2.0, "muỗng"),
                Triple("Tỏi", 4.0, "tép"),
                Triple("Ớt", 2.0, "trái")
            ),
            steps = listOf(
                "Ướp thịt với nước mắm, đường, tỏi, tiêu khoảng 30 phút.",
                "Nướng thịt trên than hoặc chảo đến vàng thơm.",
                "Pha nước chấm chua ngọt, thêm đu đủ bào.",
                "Ăn kèm bún, rau sống và thịt nướng."
            ),
            tags = listOf("bắc", "nướng", "gia đình"),
            favorite = true
        ),
        SeedRecipe(
            name = "Cơm tấm sườn",
            description = "Cơm tấm Sài Gòn với sườn nướng và đồ chua.",
            category = "Món chính",
            prepTime = 20, cookTime = 25, servings = 2, difficulty = 2,
            ingredients = listOf(
                Triple("Sườn non", 400.0, "g"),
                Triple("Gạo tấm", 300.0, "g"),
                Triple("Trứng gà", 2.0, "quả"),
                Triple("Dưa chua", 100.0, "g"),
                Triple("Nước mắm", 3.0, "muỗng"),
                Triple("Đường", 2.0, "muỗng"),
                Triple("Tỏi", 3.0, "tép")
            ),
            steps = listOf(
                "Ướp sườn với nước mắm, đường, tỏi, dầu ăn.",
                "Nướng sườn đến chín vàng.",
                "Nấu cơm tấm, chiên trứng ốp la.",
                "Bày cơm, sườn, trứng, dưa chua và nước mắm pha."
            ),
            tags = listOf("nam", "nướng")
        ),
        SeedRecipe(
            name = "Canh chua cá",
            description = "Canh chua miền Tây với cá và rau thơm.",
            category = "Món canh",
            prepTime = 15, cookTime = 20, servings = 4, difficulty = 1,
            ingredients = listOf(
                Triple("Cá lóc", 400.0, "g"),
                Triple("Cà chua", 2.0, "quả"),
                Triple("Thơm", 150.0, "g"),
                Triple("Đậu bắp", 4.0, "trái"),
                Triple("Me chín", 30.0, "g"),
                Triple("Ngò om", 1.0, "bó"),
                Triple("Giá đỗ", 100.0, "g")
            ),
            steps = listOf(
                "Nấu nước me, nêm muối đường nước mắm.",
                "Cho cà chua, thơm, đậu bắp vào nấu.",
                "Thêm cá, nấu đến chín.",
                "Tắt bếp, cho giá và ngò om."
            ),
            tags = listOf("nam", "canh", "chua")
        ),
        SeedRecipe(
            name = "Gỏi cuốn",
            description = "Cuốn tôm thịt với rau sống, chấm tương đậu phộng.",
            category = "Món nhanh",
            prepTime = 30, cookTime = 10, servings = 4, difficulty = 1,
            ingredients = listOf(
                Triple("Bánh tráng", 20.0, "cái"),
                Triple("Tôm", 300.0, "g"),
                Triple("Thịt ba chỉ", 200.0, "g"),
                Triple("Bún tươi", 200.0, "g"),
                Triple("Rau sống", 200.0, "g"),
                Triple("Đậu phộng", 50.0, "g"),
                Triple("Tương đen", 3.0, "muỗng")
            ),
            steps = listOf(
                "Luộc tôm và thịt, thái vừa ăn.",
                "Pha nước tương đậu phộng.",
                "Nhúng bánh tráng, xếp rau, bún, tôm thịt rồi cuốn.",
                "Chấm tương và thưởng thức."
            ),
            tags = listOf("nhanh", "cuốn", "lạnh"),
            favorite = true
        ),
        SeedRecipe(
            name = "Đậu phụ sốt cà",
            description = "Món chay đơn giản, đậm đà sốt cà chua.",
            category = "Món chay",
            prepTime = 10, cookTime = 15, servings = 3, difficulty = 1,
            ingredients = listOf(
                Triple("Đậu phụ", 400.0, "g"),
                Triple("Cà chua", 3.0, "quả"),
                Triple("Hành tím", 2.0, "củ"),
                Triple("Tỏi", 3.0, "tép"),
                Triple("Nước tương", 2.0, "muỗng"),
                Triple("Đường", 1.0, "muỗng")
            ),
            steps = listOf(
                "Chiên đậu phụ vàng, để ráo.",
                "Xào hành tỏi, thêm cà chua dằm.",
                "Nêm nước tương đường, cho đậu phụ vào sốt.",
                "Rắc hành lá và tắt bếp."
            ),
            tags = listOf("chay", "nhanh")
        ),
        SeedRecipe(
            name = "Bánh flan",
            description = "Bánh flan caramel mềm mịn.",
            category = "Tráng miệng",
            prepTime = 15, cookTime = 40, servings = 6, difficulty = 2,
            ingredients = listOf(
                Triple("Trứng gà", 5.0, "quả"),
                Triple("Sữa tươi", 400.0, "ml"),
                Triple("Đường", 120.0, "g"),
                Triple("Vani", 1.0, "muỗng")
            ),
            steps = listOf(
                "Thắng caramel đường, đổ vào khuôn.",
                "Đánh trứng với sữa và đường, lọc hỗn hợp.",
                "Đổ vào khuôn, hấp cách thủy 35–40 phút.",
                "Để nguội rồi ướp lạnh trước khi ăn."
            ),
            tags = listOf("ngọt", "tráng miệng")
        ),
        SeedRecipe(
            name = "Trà đào cam sả",
            description = "Thức uống mát, thơm sả và đào.",
            category = "Đồ uống",
            prepTime = 10, cookTime = 10, servings = 2, difficulty = 1,
            ingredients = listOf(
                Triple("Trà đen", 2.0, "túi"),
                Triple("Đào đóng hộp", 4.0, "miếng"),
                Triple("Cam", 1.0, "quả"),
                Triple("Sả", 2.0, "cây"),
                Triple("Đường", 2.0, "muỗng"),
                Triple("Đá viên", 10.0, "viên")
            ),
            steps = listOf(
                "Pha trà đen đậm, để nguội.",
                "Giã nhẹ sả, thêm đường và đào.",
                "Cho đá, trà, cam thái lát vào ly.",
                "Trang trí và thưởng thức."
            ),
            tags = listOf("đồ uống", "mát")
        ),
        SeedRecipe(
            name = "Mì xào bò",
            description = "Mì trứng xào bò và rau cải nhanh gọn.",
            category = "Món nhanh",
            prepTime = 15, cookTime = 12, servings = 2, difficulty = 1,
            ingredients = listOf(
                Triple("Mì trứng", 200.0, "g"),
                Triple("Thịt bò", 200.0, "g"),
                Triple("Cải thìa", 150.0, "g"),
                Triple("Tỏi", 3.0, "tép"),
                Triple("Nước tương", 2.0, "muỗng"),
                Triple("Dầu hào", 1.0, "muỗng")
            ),
            steps = listOf(
                "Trụng mì sơ, để ráo.",
                "Xào bò với tỏi nhanh tay, để riêng.",
                "Xào cải, thêm mì và gia vị.",
                "Cho bò vào đảo đều rồi tắt bếp."
            ),
            tags = listOf("nhanh", "xào")
        ),
        SeedRecipe(
            name = "Cháo gà",
            description = "Cháo gà thơm hành, dễ ăn khi mệt.",
            category = "Món chính",
            prepTime = 15, cookTime = 45, servings = 4, difficulty = 1,
            ingredients = listOf(
                Triple("Gà ta", 500.0, "g"),
                Triple("Gạo tẻ", 150.0, "g"),
                Triple("Gừng", 20.0, "g"),
                Triple("Hành lá", 1.0, "bó"),
                Triple("Muối", 1.0, "muỗng"),
                Triple("Tiêu", 0.5, "muỗng")
            ),
            steps = listOf(
                "Ninh gà lấy nước dùng.",
                "Vo gạo, nấu với nước dùng đến nhừ.",
                "Xé gà, cho vào cháo, nêm vừa ăn.",
                "Rắc hành tiêu trước khi ăn."
            ),
            tags = listOf("ấm bụng", "gia đình")
        ),
        SeedRecipe(
            name = "Trứng chiên hành",
            description = "Món ăn quốc dân nhanh gọn, thơm nức mùi hành lá.",
            category = "Món nhanh",
            prepTime = 5, cookTime = 5, servings = 2, difficulty = 1,
            ingredients = listOf(
                Triple("Trứng gà", 3.0, "quả"),
                Triple("Hành lá", 2.0, "nhánh"),
                Triple("Nước mắm", 1.0, "muỗng")
            ),
            steps = listOf(
                "Đập trứng ra bát, cắt nhỏ hành lá, thêm nước mắm rồi đánh đều.",
                "Đun nóng dầu ăn trong chảo, đổ trứng vào chiên vàng hai mặt."
            ),
            tags = listOf("nhanh", "dễ", "tiết kiệm")
        ),
        SeedRecipe(
            name = "Salad ức gà sốt mè rang",
            description = "Món ăn thanh nhẹ, giàu đạm dành cho thực đơn ăn kiêng lành mạnh.",
            category = "Món nhanh",
            prepTime = 15, cookTime = 10, servings = 2, difficulty = 2,
            ingredients = listOf(
                Triple("Ức gà", 250.0, "g"),
                Triple("Xà lách", 150.0, "g"),
                Triple("Cà chua bi", 10.0, "quả"),
                Triple("Dưa leo", 1.0, "quả"),
                Triple("Sốt mè rang", 3.0, "muỗng"),
                Triple("Dầu ô liu", 1.0, "muỗng"),
                Triple("Tiêu xay", 0.5, "muỗng")
            ),
            steps = listOf(
                "Áp chảo ức gà với chút dầu ô liu và tiêu đến khi chín vàng, xé miếng vừa ăn.",
                "Rửa sạch xà lách, cà chua bi cắt đôi, dưa leo cắt lát mỏng.",
                "Bày rau ra đĩa, xếp ức gà lên trên và rưới đều sốt mè rang trước khi thưởng thức."
            ),
            tags = listOf("healthy", "eatclean", "protein")
        ),
        SeedRecipe(
            name = "Cà phê muối xứ Huế",
            description = "Vị đậm đà của cà phê phin quyện cùng lớp kem muối béo ngậy đặc trưng.",
            category = "Đồ uống",
            prepTime = 10, cookTime = 5, servings = 1, difficulty = 1,
            ingredients = listOf(
                Triple("Cà phê phin", 25.0, "g"),
                Triple("Sữa đặc", 30.0, "ml"),
                Triple("Kem béo thực vật", 40.0, "ml"),
                Triple("Muối tinh", 0.5, "g"),
                Triple("Đá viên", 5.0, "viên")
            ),
            steps = listOf(
                "Pha cà phê bằng phin truyền thống với nước sôi 95 độ C.",
                "Đánh bông nhẹ kem béo với muối tinh và sữa đặc tạo thành lớp kem muối.",
                "Rót cà phê vào ly đá, nhẹ nhàng đổ lớp kem muối lên trên cùng."
            ),
            tags = listOf("đồ uống", "cà phê", "đặc sản"),
            favorite = true
        ),
        SeedRecipe(
            name = "Súp nấm hạt sen chay",
            description = "Món khai vị thanh đạm, bùi bùi hạt sen cùng vị ngọt tự nhiên của các loại nấm.",
            category = "Món chay",
            prepTime = 20, cookTime = 25, servings = 4, difficulty = 2,
            ingredients = listOf(
                Triple("Hạt sen tươi", 100.0, "g"),
                Triple("Nấm hương tươi", 80.0, "g"),
                Triple("Nấm đùi gà", 100.0, "g"),
                Triple("Bắp ngọt", 1.0, "trái"),
                Triple("Bột năng", 2.0, "muỗng"),
                Triple("Ngò rí", 1.0, "nhánh"),
                Triple("Hạt nêm chay", 1.5, "muỗng")
            ),
            steps = listOf(
                "Luộc hạt sen và bắp ngọt đến khi chín mềm ngọt nước.",
                "Thái nhỏ nấm hương và nấm đùi gà, cho vào nồi nấu cùng hạt sen.",
                "Hòa tan bột năng với nước lạnh rồi từ từ khuấy vào nồi tạo độ sánh mịn.",
                "Nêm hạt nêm chay vừa khẩu vị, tắt bếp và rắc ngò rí thái nhỏ."
            ),
            tags = listOf("chay", "thanh đạm", "ấm bụng")
        ),
        SeedRecipe(
            name = "Bò kho bánh mì nước dừa thơm béo chuẩn vị miền Nam",
            description = "Thịt bắp bò mềm rục ngấm đượm sả quế, nước sốt sánh vàng óng ánh ăn kèm bánh mì giòn rụm.",
            category = "Món chính",
            prepTime = 30, cookTime = 90, servings = 6, difficulty = 3,
            ingredients = listOf(
                Triple("Bắp bò", 700.0, "g"),
                Triple("Cà rốt", 2.0, "củ"),
                Triple("Nước dừa tươi", 500.0, "ml"),
                Triple("Sả", 4.0, "cây"),
                Triple("Gừng", 30.0, "g"),
                Triple("Bột gia vị bò kho", 1.0, "gói"),
                Triple("Hành tím", 4.0, "củ"),
                Triple("Tỏi", 5.0, "tép"),
                Triple("Dầu điều", 2.0, "muỗng"),
                Triple("Nước mắm", 3.0, "muỗng"),
                Triple("Bánh mì", 4.0, "ổ")
            ),
            steps = listOf(
                "Thái bắp bò quân cờ, ướp với gia vị bò kho, sả đập dập, tỏi hành băm và nước mắm 45 phút.",
                "Phi thơm dầu điều, xào săn thịt bò trên lửa lớn cho ngấm màu đẹp mắt.",
                "Đổ nước dừa tươi vào nồi, đun sôi rồi hạ nhỏ lửa hầm khoảng 60 phút cho thịt mềm.",
                "Thêm cà rốt tỉa hoa vào hầm thêm 15 phút đến khi cà rốt vừa chín tới.",
                "Hòa chút bột năng tạo độ sánh nhẹ cho nước sốt bò kho.",
                "Múc bò kho ra tô, rắc rau húng quế và ớt tươi.",
                "Thưởng thức nóng hổi kèm bánh mì giòn hoặc hủ tiếu."
            ),
            tags = listOf("nam", "bò", "đậm đà", "tiệc", "gia đình")
        )
    )

    val pantry = listOf(
        PantryItemEntity(name = "Trứng gà", amount = 10.0, unit = "quả", lowStockThreshold = 4.0),
        PantryItemEntity(name = "Gạo", amount = 2.0, unit = "kg", lowStockThreshold = 0.5),
        PantryItemEntity(name = "Nước mắm", amount = 1.0, unit = "chai", lowStockThreshold = 0.2),
        PantryItemEntity(name = "Tỏi", amount = 8.0, unit = "tép", lowStockThreshold = 3.0),
        PantryItemEntity(name = "Hành tím", amount = 5.0, unit = "củ", lowStockThreshold = 2.0),
        PantryItemEntity(name = "Đường", amount = 500.0, unit = "g", lowStockThreshold = 100.0),
        PantryItemEntity(name = "Dầu ăn", amount = 0.8, unit = "lít", lowStockThreshold = 0.2),
        PantryItemEntity(name = "Cà chua", amount = 4.0, unit = "quả", lowStockThreshold = 2.0),
        PantryItemEntity(name = "Thịt heo ba chỉ", amount = 400.0, unit = "g", lowStockThreshold = 200.0),
        PantryItemEntity(name = "Đậu hũ trắng", amount = 3.0, unit = "miếng", lowStockThreshold = 1.0),
        PantryItemEntity(name = "Rau muống", amount = 1.0, unit = "bó", lowStockThreshold = 0.5),
        PantryItemEntity(name = "Hành lá", amount = 3.0, unit = "nhánh", lowStockThreshold = 1.0),
        PantryItemEntity(name = "Tiêu xay", amount = 50.0, unit = "g", lowStockThreshold = 10.0),
        PantryItemEntity(name = "Tiêu đen hạt", amount = 5.0, unit = "g", lowStockThreshold = 20.0),
        PantryItemEntity(name = "Bơ lạt", amount = 50.0, unit = "g", lowStockThreshold = 100.0),
        PantryItemEntity(name = "Sữa đặc", amount = 1.0, unit = "hộp", lowStockThreshold = 0.5)
    )

    suspend fun populate(
        categoryDao: CategoryDao,
        recipeDao: RecipeDao,
        ingredientDao: IngredientDao,
        stepDao: StepDao,
        tagDao: TagDao,
        pantryDao: PantryDao,
        historyDao: HistoryDao? = null
    ) {
        val categoryIds = mutableMapOf<String, Long>()
        categories.forEach { cat ->
            categoryIds[cat.name] = categoryDao.upsert(cat)
        }

        val tagIds = mutableMapOf<String, Long>()
        recipes.flatMap { it.tags }.distinct().forEach { tag ->
            tagIds[tag] = tagDao.upsert(TagEntity(name = tag))
        }

        val insertedRecipeIds = mutableMapOf<String, Long>()
        recipes.forEach { seed ->
            val recipeId = recipeDao.upsert(
                RecipeEntity(
                    name = seed.name,
                    description = seed.description,
                    categoryId = categoryIds[seed.category],
                    prepTime = seed.prepTime,
                    cookTime = seed.cookTime,
                    servings = seed.servings,
                    difficulty = seed.difficulty,
                    isFavorite = seed.favorite
                )
            )
            insertedRecipeIds[seed.name] = recipeId
            ingredientDao.upsertAll(
                seed.ingredients.mapIndexed { index, (name, amount, unit) ->
                    IngredientEntity(
                        recipeId = recipeId,
                        name = name,
                        amount = amount,
                        unit = unit,
                        sortOrder = index
                    )
                }
            )
            stepDao.upsertAll(
                seed.steps.mapIndexed { index, text ->
                    StepEntity(
                        recipeId = recipeId,
                        stepNumber = index + 1,
                        description = text
                    )
                }
            )
            seed.tags.forEach { tag ->
                tagIds[tag]?.let { tagId ->
                    tagDao.link(RecipeTagCrossRef(recipeId, tagId))
                }
            }
        }

        pantry.forEach { pantryDao.upsert(it) }

        // Seed 3 historical cook logs so Home stats and History screen have data
        if (historyDao != null) {
            val now = System.currentTimeMillis()
            val oneDayMs = 24L * 60L * 60L * 1000L
            insertedRecipeIds["Phở bò"]?.let { id ->
                historyDao.insert(
                    CookHistoryEntity(
                        recipeId = id,
                        cookedAt = now - 2 * oneDayMs,
                        note = "Nấu cho cả nhà cuối tuần"
                    )
                )
            }
            insertedRecipeIds["Cơm tấm sườn"]?.let { id ->
                historyDao.insert(
                    CookHistoryEntity(
                        recipeId = id,
                        cookedAt = now - 1 * oneDayMs,
                        note = "Sườn ướp ngon vừa miệng"
                    )
                )
            }
            insertedRecipeIds["Canh chua cá"]?.let { id ->
                historyDao.insert(
                    CookHistoryEntity(
                        recipeId = id,
                        cookedAt = now - 2 * 60 * 60 * 1000L,
                        note = "Bữa trưa thanh mát"
                    )
                )
            }
        }
    }
}
