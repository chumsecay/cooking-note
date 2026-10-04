package com.cookingnote.app.data.seed

import com.cookingnote.app.data.dao.CategoryDao
import com.cookingnote.app.data.dao.ChatMessageDao
import com.cookingnote.app.data.dao.HistoryDao
import com.cookingnote.app.data.dao.IngredientDao
import com.cookingnote.app.data.dao.PantryDao
import com.cookingnote.app.data.dao.RecipeDao
import com.cookingnote.app.data.dao.StepDao
import com.cookingnote.app.data.dao.TagDao
import com.cookingnote.app.data.entity.CategoryEntity
import com.cookingnote.app.data.entity.ChatMessageEntity
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
    val favorite: Boolean = false,
    val notes: String = ""
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
            favorite = true,
            notes = "Nướng kỹ gừng và hành tây, cạo sạch vỏ cháy trước khi cho vào nồi để nước dùng thơm dịu và trong vắt."
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
            favorite = true,
            notes = "Ướp thịt cùng một chút mỡ heo và nước hàng (nước màu) giúp chả nướng không bị khô xác và có màu nâu óng."
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
            tags = listOf("nam", "nướng"),
            notes = "Dùng nước dừa tươi hoặc chút sữa đặc ướp sườn giúp thớ thịt mềm mọng nước tự nhiên."
        ),
        SeedRecipe(
            name = "Bún bò Huế chuẩn vị",
            description = "Bún bò xứ Huế đậm đà hương sả, cay nồng ớt sa tế và thơm lừng mắm ruốc.",
            category = "Món chính",
            prepTime = 40, cookTime = 120, servings = 5, difficulty = 3,
            ingredients = listOf(
                Triple("Bắp bò", 500.0, "g"),
                Triple("Giò heo", 400.0, "g"),
                Triple("Bún tươi", 500.0, "g"),
                Triple("Mắm ruốc", 2.0, "muỗng"),
                Triple("Sả", 5.0, "nhánh"),
                Triple("Ớt sa tế", 2.0, "muỗng"),
                Triple("Hành tím", 3.0, "củ")
            ),
            steps = listOf(
                "Hầm bắp bò và giò heo với sả đập dập đến khi mềm vừa ăn.",
                "Hòa tan mắm ruốc với nước lạnh, để lắng lấy nước trong rồi cho vào nồi nước dùng.",
                "Nêm nếm gia vị và chưng ớt màu sa tế cho dậy màu đỏ đẹp mắt.",
                "Chần bún, xếp thịt bắp bò, giò heo, chan nước dùng cay nồng và ăn kèm rau chuối bào."
            ),
            tags = listOf("trung", "cay", "đặc sản"),
            favorite = true,
            notes = "Gạn kỹ cặn mắm ruốc và cho vào lúc nước sôi sùng sục để hương mắm hòa quyện mà không bị nồng gắt."
        ),
        SeedRecipe(
            name = "Bò kho bánh mì nước dừa thơm béo chuẩn vị miền Nam",
            description = "Thịt bắp bò mềm rục ngấm đượm sả quế, nước sốt sánh vàng óng ánh ăn kèm bánh mì giòn rụm.",
            category = "Món chính",
            prepTime = 30, cookTime = 90, servings = 6, difficulty = 3,
            ingredients = listOf(
                Triple("Bắp bò", 800.0, "g"),
                Triple("Nước dừa", 500.0, "ml"),
                Triple("Cà rốt", 2.0, "củ"),
                Triple("Sả", 4.0, "nhánh"),
                Triple("Bột gia vị bò kho", 1.0, "gói"),
                Triple("Tỏi", 4.0, "tép"),
                Triple("Hành tím", 3.0, "củ")
            ),
            steps = listOf(
                "Bắp bò cắt miếng vuông dày, ướp gói gia vị bò kho, tỏi băm và sả đập dập 30 phút.",
                "Xào săn thịt bò trên lửa lớn cho dậy hương thơm đặc trưng.",
                "Đổ nước dừa tươi vào hầm nhỏ lửa khoảng 1 giờ đến khi thịt bò mềm nhừ.",
                "Cho cà rốt cắt khúc vào hầm thêm 15 phút, nêm nếm vừa ăn rồi ăn kèm bánh mì nóng giòn."
            ),
            tags = listOf("nam", "bổ dưỡng", "cuối tuần"),
            notes = "Nấu hoàn toàn bằng nước dừa xiêm tươi giúp nước sốt sánh ngọt đậm đà mà không cần nêm nhiều đường."
        ),
        SeedRecipe(
            name = "Cháo gà",
            description = "Cháo gà sánh mịn, thơm lừng hành phi và tiêu cay nồng ấm bụng.",
            category = "Món chính",
            prepTime = 15, cookTime = 45, servings = 3, difficulty = 1,
            ingredients = listOf(
                Triple("Gạo", 150.0, "g"),
                Triple("Thịt gà", 300.0, "g"),
                Triple("Hành lá", 2.0, "nhánh"),
                Triple("Gừng", 10.0, "g"),
                Triple("Tiêu xay", 5.0, "g")
            ),
            steps = listOf(
                "Luộc gà với vài lát gừng lấy nước dùng ngọt thanh, xé nhỏ thịt gà.",
                "Rang gạo hơi vàng thơm rồi cho vào nồi nước luộc gà ninh nhừ.",
                "Khi cháo sánh mịn, nêm nếm gia vị vừa miệng.",
                "Múc cháo ra tô, xếp thịt gà xé, rắc hành lá, tía tô và tiêu xay."
            ),
            tags = listOf("ấm nóng", "dễ tiêu", "bồi bổ"),
            notes = "Rang gạo trước khi nấu giúp hạt cháo nở bung đều, sánh mượt mà không bị vữa nước."
        ),
        SeedRecipe(
            name = "Canh chua cá",
            description = "Canh chua miền Tây với cá và rau thơm.",
            category = "Món canh",
            prepTime = 15, cookTime = 20, servings = 4, difficulty = 1,
            ingredients = listOf(
                Triple("Cá lóc", 400.0, "g"),
                Triple("Cà chua", 2.0, "quả"),
                Triple("Dứa", 0.5, "quả"),
                Triple("Đậu bắp", 100.0, "g"),
                Triple("Me chua", 30.0, "g"),
                Triple("Giá đỗ", 100.0, "g"),
                Triple("Rau ngò gai", 20.0, "g")
            ),
            steps = listOf(
                "Nấu sôi nước, dầm me lấy nước chua.",
                "Cho cá vào nấu chín tới, vớt bọt.",
                "Thêm cà chua, dứa, đậu bắp, nêm nước mắm và đường.",
                "Tắt bếp, cho giá đỗ và ngò gai, ớt lát."
            ),
            tags = listOf("nam", "chua", "nóng"),
            notes = "Phi thơm tỏi băm rồi xào sơ cà chua và dứa trước khi châm nước giúp nước canh có màu đỏ cam óng ánh."
        ),
        SeedRecipe(
            name = "Canh bí đỏ thịt bằm",
            description = "Canh bí đỏ bùi ngọt tự nhiên kết hợp thịt nạc xay giàu dinh dưỡng cho bữa cơm gia đình.",
            category = "Món canh",
            prepTime = 15, cookTime = 20, servings = 4, difficulty = 1,
            ingredients = listOf(
                Triple("Bí đỏ", 400.0, "g"),
                Triple("Thịt nạc xay", 150.0, "g"),
                Triple("Hành lá", 2.0, "nhánh"),
                Triple("Hành tím", 2.0, "củ"),
                Triple("Nước mắm", 1.5, "muỗng"),
                Triple("Tiêu xay", 10.0, "g")
            ),
            steps = listOf(
                "Bí đỏ gọt vỏ, rửa sạch, cắt miếng vuông vừa ăn.",
                "Ướp thịt xay với hành tím băm, chút nước mắm và tiêu.",
                "Phi thơm hành tím, xào săn thịt bằm rồi đổ 800ml nước vào đun sôi.",
                "Cho bí đỏ vào nấu đến khi bí mềm nhừ, nêm lại gia vị vừa ăn, rắc hành lá cắt nhỏ và tiêu."
            ),
            tags = listOf("ngọt", "gia đình", "dễ nấu"),
            notes = "Không đảo quá mạnh tay sau khi bí đã chín mềm để miếng bí không bị nát vụn vào nước canh."
        ),
        SeedRecipe(
            name = "Canh rau ngót nấu tôm",
            description = "Món canh thanh mát giải nhiệt mùa hè, nước canh ngọt lịm từ tôm tươi và rau ngót.",
            category = "Món canh",
            prepTime = 15, cookTime = 15, servings = 3, difficulty = 1,
            ingredients = listOf(
                Triple("Rau ngót", 1.0, "bó"),
                Triple("Tôm tươi", 150.0, "g"),
                Triple("Hành tím", 2.0, "củ"),
                Triple("Nước mắm", 1.0, "muỗng"),
                Triple("Dầu ăn", 1.0, "muỗng")
            ),
            steps = listOf(
                "Rau ngót tuốt lá, rửa sạch, dùng tay vò nhẹ cho lá hơi dập.",
                "Tôm bóc vỏ, rút chỉ đen, băm nhỏ hoặc giã hơi dập, ướp chút tiêu và hạt nêm.",
                "Phi thơm hành tím với chút dầu ăn, cho tôm vào xào săn thơm.",
                "Thêm nước đun sôi rồi thả rau ngót vào nấu khoảng 3 phút cho rau chín mềm, nêm nước mắm vừa ăn."
            ),
            tags = listOf("thanh mát", "nhanh", "dễ nấu"),
            notes = "Vò nhẹ rau ngót trước khi nấu giúp rau nhanh mềm và tiết vị ngọt đậm đà vào nước canh."
        ),
        SeedRecipe(
            name = "Gỏi cuốn",
            description = "Gỏi cuốn tôm thịt chấm tương đen bùi béo.",
            category = "Món nhanh",
            prepTime = 20, cookTime = 15, servings = 3, difficulty = 1,
            ingredients = listOf(
                Triple("Bánh tráng", 10.0, "cái"),
                Triple("Tôm tươi", 200.0, "g"),
                Triple("Thịt ba chỉ", 200.0, "g"),
                Triple("Bún tươi", 200.0, "g"),
                Triple("Xà lách", 100.0, "g"),
                Triple("Hẹ", 50.0, "g")
            ),
            steps = listOf(
                "Luộc tôm và thịt ba chỉ, thái mỏng thịt, bóc vỏ tôm chẻ đôi.",
                "Làm ẩm bánh tráng, xếp xà lách, bún, thịt, tôm và hẹ.",
                "Cuốn chặt tay thành cuộn tròn đều.",
                "Ăn kèm tương đen pha đậu phộng rang."
            ),
            tags = listOf("nam", "cuốn", "nhanh"),
            favorite = true,
            notes = "Thoa khăn ẩm lên mặt bánh tráng thay vì nhúng ngập nước để bánh dai giòn, không rách khi cuốn."
        ),
        SeedRecipe(
            name = "Đậu phụ sốt cà",
            description = "Đậu phụ rán vàng sốt cà chua hành hoa thơm dịu.",
            category = "Món chay",
            prepTime = 10, cookTime = 15, servings = 2, difficulty = 1,
            ingredients = listOf(
                Triple("Đậu phụ", 3.0, "miếng"),
                Triple("Cà chua", 3.0, "quả"),
                Triple("Hành lá", 2.0, "nhánh"),
                Triple("Nước mắm", 2.0, "muỗng"),
                Triple("Dầu ăn", 2.0, "muỗng")
            ),
            steps = listOf(
                "Cắt đậu phụ thành miếng vuông, rán vàng đều các mặt.",
                "Cà chua băm nhỏ, xào với dầu ăn đến khi nhuyễn.",
                "Cho đậu phụ rán vào đảo nhẹ, nêm nước mắm và đường.",
                "Đun nhỏ lửa 5 phút cho ngấm, rắc hành lá rồi tắt bếp."
            ),
            tags = listOf("chay", "nhanh", "bình dân"),
            notes = "Rán đậu phụ vàng giòn các mặt trước khi sốt giúp miếng đậu giữ form và thấm đều sốt cà chua."
        ),
        SeedRecipe(
            name = "Nấm đùi gà kho tiêu chay",
            description = "Nấm đùi gà dai giòn sần sật, kho nước tương tiêu đen cay nồng đậm đà bắt cơm.",
            category = "Món chay",
            prepTime = 10, cookTime = 20, servings = 3, difficulty = 1,
            ingredients = listOf(
                Triple("Nấm đùi gà", 300.0, "g"),
                Triple("Tiêu đen hạt", 15.0, "g"),
                Triple("Nước tương", 3.0, "muỗng"),
                Triple("Đường", 1.5, "muỗng"),
                Triple("Dầu ăn", 1.0, "muỗng"),
                Triple("Hành boa-rô", 1.0, "cây")
            ),
            steps = listOf(
                "Nấm đùi gà rửa sạch, cắt khoanh tròn dày 1.5cm, khía nhẹ vảy rồng trên hai mặt.",
                "Chiên sơ hai mặt nấm với chút dầu ăn cho xém vàng thơm.",
                "Phi thơm boa-rô, cho nước tương, đường, tiêu đập dập và chút nước vào đun sôi.",
                "Thả nấm vào kho liu riu trên lửa nhỏ đến khi nước sốt sánh kẹo lại, rắc thêm tiêu xay."
            ),
            tags = listOf("chay", "đậm đà", "bắt cơm"),
            notes = "Khía vảy rồng trên thân nấm giúp nước sốt ngấm sâu vào từng thớ nấm, tạo độ giòn ngọt mọng nước."
        ),
        SeedRecipe(
            name = "Súp nấm hạt sen chay",
            description = "Món khai vị thanh đạm, bùi bùi hạt sen cùng vị ngọt tự nhiên của các loại nấm.",
            category = "Món chay",
            prepTime = 20, cookTime = 25, servings = 4, difficulty = 2,
            ingredients = listOf(
                Triple("Hạt sen", 100.0, "g"),
                Triple("Nấm hương", 50.0, "g"),
                Triple("Nấm đùi gà", 100.0, "g"),
                Triple("Bột năng", 30.0, "g"),
                Triple("Ngô ngọt", 50.0, "g"),
                Triple("Ngò rí", 10.0, "g")
            ),
            steps = listOf(
                "Ninh mềm hạt sen tươi với 800ml nước.",
                "Nấm hương ngâm nở, cắt sợi; nấm đùi gà thái hạt lựu.",
                "Cho nấm và ngô ngọt vào nấu cùng hạt sen khoảng 5 phút.",
                "Hòa tan bột năng với nước, rót từ từ vào nồi khuấy đều tay đến khi sánh nhẹ, rắc tiêu và ngò."
            ),
            tags = listOf("chay", "thanh đạm", "khai vị"),
            notes = "Nấu hạt sen chín bở trước rồi mới thả nấm vào sau để nấm giữ được độ giòn ngọt tự nhiên."
        ),
        SeedRecipe(
            name = "Mì xào bò",
            description = "Mì xào giòn dai cùng thịt bò mềm mọng và rau cải xanh giòn ngọt.",
            category = "Món nhanh",
            prepTime = 10, cookTime = 10, servings = 2, difficulty = 1,
            ingredients = listOf(
                Triple("Mì gói", 2.0, "gói"),
                Triple("Thịt bò", 150.0, "g"),
                Triple("Rau cải", 150.0, "g"),
                Triple("Tỏi", 3.0, "tép"),
                Triple("Dầu hào", 1.0, "muỗng")
            ),
            steps = listOf(
                "Chần mì qua nước sôi vừa chín tới, xả nước lạnh để ráo.",
                "Ướp thịt bò với tỏi, dầu hào, tiêu.",
                "Xào thịt bò trên lửa lớn vừa chín tới thì trút ra đĩa riêng.",
                "Xào rau cải, cho mì và thịt bò vào đảo nhanh tay, rắc tiêu thơm."
            ),
            tags = listOf("nhanh", "dễ làm", "bữa sáng"),
            notes = "Xào thịt bò trên lửa cực lớn trong 1 phút rồi trút ra ngay để thớ thịt giữ trọn độ mềm mọng."
        ),
        SeedRecipe(
            name = "Trứng chiên hành",
            description = "Món ăn quốc dân nhanh gọn, thơm nức mùi hành lá.",
            category = "Món nhanh",
            prepTime = 5, cookTime = 5, servings = 2, difficulty = 1,
            ingredients = listOf(
                Triple("Trứng gà", 3.0, "quả"),
                Triple("Hành lá", 3.0, "nhánh"),
                Triple("Nước mắm", 1.0, "muỗng"),
                Triple("Tiêu xay", 5.0, "g"),
                Triple("Dầu ăn", 2.0, "muỗng")
            ),
            steps = listOf(
                "Đập trứng vào tô, thêm hành lá cắt nhỏ, nước mắm và tiêu xay.",
                "Đánh tan đều trứng đến khi sủi bọt tăm.",
                "Làm nóng chảo với dầu ăn, đổ trứng vào chiên trên lửa vừa.",
                "Khi mặt dưới vàng giòn, lật mặt chiên tiếp 1 phút rồi cuộn tròn ra đĩa."
            ),
            tags = listOf("nhanh", "tiết kiệm", "dễ làm"),
            notes = "Đánh trứng kỹ cùng 1 muỗng cà phê nước ấm giúp trứng nở phồng xốp mềm khi chiên."
        ),
        SeedRecipe(
            name = "Salad ức gà sốt mè rang",
            description = "Món ăn thanh nhẹ, giàu đạm dành cho thực đơn ăn kiêng lành mạnh.",
            category = "Món nhanh",
            prepTime = 15, cookTime = 10, servings = 2, difficulty = 1,
            ingredients = listOf(
                Triple("Ức gà", 200.0, "g"),
                Triple("Xà lách", 150.0, "g"),
                Triple("Cà chua", 2.0, "quả"),
                Triple("Dưa leo", 1.0, "quả"),
                Triple("Sốt mè rang", 3.0, "muỗng")
            ),
            steps = listOf(
                "Luộc chín ức gà với chút muối và gừng, xé sợi vừa ăn.",
                "Rau xà lách rửa sạch ngâm nước muối loãng, cắt khúc.",
                "Cà chua và dưa leo thái lát mỏng.",
                "Xếp rau, gà vào đĩa lớn, rưới sốt mè rang béo thơm lên trên và trộn đều."
            ),
            tags = listOf("healthy", "diet", "nhanh"),
            notes = "Luộc ức gà vừa chín tới rồi ngâm ngay vào nước đá lạnh để thớ thịt gà trắng mềm không khô bở."
        ),
        SeedRecipe(
            name = "Bánh flan",
            description = "Bánh flan caramel mềm mịn, thơm mùi vani và cà phê đắng nhẹ.",
            category = "Tráng miệng",
            prepTime = 25, cookTime = 30, servings = 6, difficulty = 2,
            ingredients = listOf(
                Triple("Trứng gà", 5.0, "quả"),
                Triple("Sữa tươi không đường", 400.0, "ml"),
                Triple("Sữa đặc", 150.0, "g"),
                Triple("Đường", 80.0, "g")
            ),
            steps = listOf(
                "Thắng đường với chút nước đến màu cánh gián, tráng đều đáy khuôn flan.",
                "Đun ấm sữa tươi và sữa đặc đến khoảng 50°C.",
                "Đánh tan nhẹ trứng (tránh tạo bọt khí), rót sữa ấm vào khuấy đều.",
                "Lọc hỗn hợp qua rây 2 lần, rót vào khuôn và hấp cách thủy nhỏ lửa trong 30 phút."
            ),
            tags = listOf("ngọt", "mát", "trẻ em"),
            favorite = true,
            notes = "Lọc hỗn hợp qua rây 2 lần và che khăn trên nắp xửng hấp để bánh láng mịn, hoàn toàn không bị rỗ mặt."
        ),
        SeedRecipe(
            name = "Chè bưởi An Giang",
            description = "Chè bưởi giòn sần sật, bùi bùi đậu xanh và béo ngậy nước cốt dừa thơm phức.",
            category = "Tráng miệng",
            prepTime = 40, cookTime = 30, servings = 5, difficulty = 2,
            ingredients = listOf(
                Triple("Cùi bưởi", 200.0, "g"),
                Triple("Đậu xanh xát vỏ", 150.0, "g"),
                Triple("Nước cốt dừa", 200.0, "ml"),
                Triple("Đường", 150.0, "g"),
                Triple("Bột năng", 60.0, "g")
            ),
            steps = listOf(
                "Cùi bưởi cắt hạt lựu, bóp muối và xả nước nhiều lần cho hết đắng, vắt ráo rồi luộc sơ.",
                "Áo cùi bưởi với đường và bột năng, luộc chín trong rồi vớt ra ngâm nước đá cho giòn.",
                "Đậu xanh hấp chín tới, giữ nguyên hạt.",
                "Nấu sôi nước đường phèn, hòa bột năng tạo độ sánh rồi thả cùi bưởi, đậu xanh vào khuấy đều, chan nước cốt dừa."
            ),
            tags = listOf("chè", "giòn ngọt", "truyền thống"),
            notes = "Bóp xả cùi bưởi thật kỹ với muối hạt và ngâm nước đá lạnh sau khi luộc để cùi bưởi giòn sần sật."
        ),
        SeedRecipe(
            name = "Rau câu dừa xiêm",
            description = "Thạch rau câu hai tầng thanh mát, hòa quyện giữa nước dừa tươi ngọt lành và lớp dừa béo thơm.",
            category = "Tráng miệng",
            prepTime = 15, cookTime = 15, servings = 6, difficulty = 1,
            ingredients = listOf(
                Triple("Nước dừa", 800.0, "ml"),
                Triple("Bột rau câu", 10.0, "g"),
                Triple("Nước cốt dừa", 150.0, "ml"),
                Triple("Đường", 80.0, "g")
            ),
            steps = listOf(
                "Trộn đều bột rau câu với đường.",
                "Đun sôi nước dừa, từ từ đổ hỗn hợp rau câu vào khuấy tan hoàn toàn trên lửa nhỏ 5 phút.",
                "Chia làm 2 phần: 1 phần giữ nguyên đổ vào khuôn làm lớp đáy; 1 phần hòa thêm nước cốt dừa.",
                "Khi bề mặt lớp thạch trong se lại, nhẹ nhàng rót lớp nước cốt dừa lên trên rồi để lạnh trong ngăn mát."
            ),
            tags = listOf("thanh mát", "giải nhiệt", "dễ làm"),
            notes = "Chỉ đổ lớp thạch thứ hai khi mặt lớp thứ nhất vừa se cứng nhẹ để hai tầng thạch dính liền mà không hòa lẫn."
        ),
        SeedRecipe(
            name = "Trà đào cam sả",
            description = "Trà đen thơm dịu vị đào, thanh mát sả tươi và chua nhẹ của cam vàng.",
            category = "Đồ uống",
            prepTime = 10, cookTime = 10, servings = 2, difficulty = 1,
            ingredients = listOf(
                Triple("Trà đen", 10.0, "g"),
                Triple("Cam", 1.0, "quả"),
                Triple("Sả", 3.0, "cây"),
                Triple("Đào ngâm", 4.0, "miếng"),
                Triple("Đường", 40.0, "g")
            ),
            steps = listOf(
                "Đập dập 2 cây sả, đun với 300ml nước lấy hương thơm.",
                "Dùng nước sả nóng ủ trà đen trong 10 phút rồi lọc bỏ bã.",
                "Vắt nửa quả cam lấy nước cốt, nửa quả còn lại thái lát mỏng.",
                "Pha nước trà, nước cốt cam, nước đường và đá lạnh; trang trí đào ngâm và cam lát."
            ),
            tags = listOf("lạnh", "thanh nhiệt", "giải khát"),
            notes = "Đập dập sả trước khi đun để tinh dầu sả khuếch tán tối đa vào nước trà."
        ),
        SeedRecipe(
            name = "Cà phê muối xứ Huế",
            description = "Hương vị cà phê phin truyền thống hòa quyện cùng lớp kem muối béo ngậy mằn mặn độc đáo.",
            category = "Đồ uống",
            prepTime = 10, cookTime = 5, servings = 1, difficulty = 1,
            ingredients = listOf(
                Triple("Cà phê bột", 25.0, "g"),
                Triple("Sữa đặc", 30.0, "ml"),
                Triple("Kem béo", 50.0, "ml"),
                Triple("Muối", 1.0, "g")
            ),
            steps = listOf(
                "Pha cà phê phin truyền thống với 80ml nước sôi, rót sữa đặc vào đáy ly.",
                "Đánh bông nhẹ hỗn hợp kem béo thực vật cùng 1g muối tinh đến khi sệt mịn.",
                "Rót cà phê phin lên trên lớp sữa đặc.",
                "Nhẹ nhàng phủ lớp kem muối bồng bềnh lên bề mặt và thưởng thức."
            ),
            tags = listOf("cà phê", "đặc sản", "thời thượng"),
            notes = "Đánh kem muối vừa độ sệt dẻo, không đánh quá cứng để kem hòa tan êm dịu khi nhấp ngụm cà phê."
        )
    )

    private val now = System.currentTimeMillis()
    private val oneDayMs = 24L * 60L * 60L * 1000L

    val pantry = listOf(
        PantryItemEntity(name = "Trứng gà", amount = 10.0, unit = "quả", expiryDate = now + 14 * oneDayMs, lowStockThreshold = 4.0),
        PantryItemEntity(name = "Gạo", amount = 2.0, unit = "kg", expiryDate = now + 90 * oneDayMs, lowStockThreshold = 0.5),
        PantryItemEntity(name = "Nước mắm", amount = 1.0, unit = "chai", expiryDate = now + 180 * oneDayMs, lowStockThreshold = 0.2),
        PantryItemEntity(name = "Tỏi", amount = 8.0, unit = "tép", expiryDate = now + 30 * oneDayMs, lowStockThreshold = 3.0),
        PantryItemEntity(name = "Hành tím", amount = 5.0, unit = "củ", expiryDate = now + 30 * oneDayMs, lowStockThreshold = 2.0),
        PantryItemEntity(name = "Đường", amount = 500.0, unit = "g", expiryDate = now + 365 * oneDayMs, lowStockThreshold = 100.0),
        PantryItemEntity(name = "Dầu ăn", amount = 0.8, unit = "lít", expiryDate = now + 180 * oneDayMs, lowStockThreshold = 0.2),
        PantryItemEntity(name = "Cà chua", amount = 4.0, unit = "quả", expiryDate = now + 5 * oneDayMs, lowStockThreshold = 2.0),
        PantryItemEntity(name = "Thịt ba chỉ", amount = 400.0, unit = "g", expiryDate = now + 3 * oneDayMs, lowStockThreshold = 200.0),
        PantryItemEntity(name = "Đậu phụ", amount = 3.0, unit = "miếng", expiryDate = now + 2 * oneDayMs, lowStockThreshold = 1.0),
        PantryItemEntity(name = "Rau muống", amount = 1.0, unit = "bó", expiryDate = now + 3 * oneDayMs, lowStockThreshold = 0.5),
        PantryItemEntity(name = "Hành lá", amount = 3.0, unit = "nhánh", expiryDate = now + 4 * oneDayMs, lowStockThreshold = 1.0),
        PantryItemEntity(name = "Tiêu xay", amount = 50.0, unit = "g", expiryDate = now + 180 * oneDayMs, lowStockThreshold = 10.0),
        PantryItemEntity(name = "Tiêu đen hạt", amount = 5.0, unit = "g", expiryDate = now + 180 * oneDayMs, lowStockThreshold = 20.0),
        PantryItemEntity(name = "Bơ lạt", amount = 50.0, unit = "g", expiryDate = now + 30 * oneDayMs, lowStockThreshold = 100.0),
        PantryItemEntity(name = "Sữa đặc", amount = 1.0, unit = "hộp", expiryDate = now + 90 * oneDayMs, lowStockThreshold = 0.5),
        PantryItemEntity(name = "Thịt bò", amount = 400.0, unit = "g", expiryDate = now + 3 * oneDayMs, lowStockThreshold = 200.0),
        PantryItemEntity(name = "Tôm tươi", amount = 300.0, unit = "g", expiryDate = now + 2 * oneDayMs, lowStockThreshold = 100.0),
        PantryItemEntity(name = "Bún tươi", amount = 500.0, unit = "g", expiryDate = now + 2 * oneDayMs, lowStockThreshold = 200.0),
        PantryItemEntity(name = "Bánh phở", amount = 500.0, unit = "g", expiryDate = now + 2 * oneDayMs, lowStockThreshold = 200.0),
        PantryItemEntity(name = "Bí đỏ", amount = 500.0, unit = "g", expiryDate = now + 10 * oneDayMs, lowStockThreshold = 200.0),
        PantryItemEntity(name = "Rau ngót", amount = 1.0, unit = "bó", expiryDate = now + 3 * oneDayMs, lowStockThreshold = 0.5),
        PantryItemEntity(name = "Nấm đùi gà", amount = 300.0, unit = "g", expiryDate = now + 5 * oneDayMs, lowStockThreshold = 100.0),
        PantryItemEntity(name = "Nước cốt dừa", amount = 300.0, unit = "ml", expiryDate = now + 60 * oneDayMs, lowStockThreshold = 100.0),
        PantryItemEntity(name = "Đậu xanh xát vỏ", amount = 200.0, unit = "g", expiryDate = now + 180 * oneDayMs, lowStockThreshold = 100.0)
    )

    suspend fun populate(
        categoryDao: CategoryDao,
        recipeDao: RecipeDao,
        ingredientDao: IngredientDao,
        stepDao: StepDao,
        tagDao: TagDao,
        pantryDao: PantryDao,
        historyDao: HistoryDao? = null,
        chatDao: ChatMessageDao? = null
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
                    isFavorite = seed.favorite,
                    notes = seed.notes
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

        // Seed lịch sử nấu ăn đa dạng cho Home stats và History screen
        if (historyDao != null) {
            insertedRecipeIds["Phở bò"]?.let { id ->
                historyDao.insert(
                    CookHistoryEntity(
                        recipeId = id,
                        cookedAt = now - 3 * oneDayMs,
                        note = "Nấu cho cả nhà cuối tuần"
                    )
                )
            }
            insertedRecipeIds["Cơm tấm sườn"]?.let { id ->
                historyDao.insert(
                    CookHistoryEntity(
                        recipeId = id,
                        cookedAt = now - 2 * oneDayMs,
                        note = "Sườn ướp ngon vừa miệng"
                    )
                )
            }
            insertedRecipeIds["Canh chua cá"]?.let { id ->
                historyDao.insert(
                    CookHistoryEntity(
                        recipeId = id,
                        cookedAt = now - 1 * oneDayMs,
                        note = "Bữa trưa thanh mát"
                    )
                )
            }
            insertedRecipeIds["Bún bò Huế chuẩn vị"]?.let { id ->
                historyDao.insert(
                    CookHistoryEntity(
                        recipeId = id,
                        cookedAt = now - 5 * 60 * 60 * 1000L,
                        note = "Nấu dịp sum họp gia đình"
                    )
                )
            }
            insertedRecipeIds["Canh bí đỏ thịt bằm"]?.let { id ->
                historyDao.insert(
                    CookHistoryEntity(
                        recipeId = id,
                        cookedAt = now - 1 * 60 * 60 * 1000L,
                        note = "Món canh ngọt lành cho bé"
                    )
                )
            }
        }

        // Seed tin nhắn mẫu ban đầu cho trợ lý AI
        if (chatDao != null) {
            chatDao.insert(
                ChatMessageEntity(
                    role = ChatMessageEntity.ROLE_USER,
                    content = "Tủ lạnh nhà tôi còn thịt ba chỉ, cà chua và trứng gà thì nấu được món gì ngon?",
                    createdAt = now - 30 * 60 * 1000L
                )
            )
            val matchedId = insertedRecipeIds["Đậu phụ sốt cà"] ?: insertedRecipeIds["Trứng chiên hành"]
            chatDao.insert(
                ChatMessageEntity(
                    role = ChatMessageEntity.ROLE_ASSISTANT,
                    content = "Với thịt ba chỉ, cà chua và trứng gà trong tủ lạnh, bạn có thể nấu ngay: 1. Đậu phụ sốt cà chua (nếu có thêm đậu) hoặc Thịt ba chỉ rim cà chua đậm đà; 2. Trứng chiên hành thơm mềm nhanh gọn. Bạn muốn xem chi tiết công thức nào?",
                    matchedRecipeId = matchedId,
                    createdAt = now - 29 * 60 * 1000L
                )
            )
        }
    }
}
