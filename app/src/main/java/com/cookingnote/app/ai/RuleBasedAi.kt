package com.cookingnote.app.ai

import com.cookingnote.app.data.repository.CookbookRepository

class RuleBasedAi(
    private val repository: CookbookRepository
) {
    suspend fun suggest(prompt: String, ingredients: List<String> = emptyList()): List<AiSuggestion> {
        val lower = prompt.lowercase()
        val wantsPantry = lower.contains("tủ lạnh") || lower.contains("tu lanh") ||
            lower.contains("nguyên liệu") || lower.contains("nguyen lieu")
        val wantsCookOrSuggest = lower.contains("nấu") || lower.contains("nau") ||
            lower.contains("gợi ý") || lower.contains("goi y") ||
            lower.contains("món") || lower.contains("mon") ||
            lower.contains("làm gì") || lower.contains("lam gi")

        if (wantsPantry) {
            val pantryItems = repository.getPantrySnapshot()
            val isCheckingOnly = !wantsCookOrSuggest || lower.contains("check") ||
                lower.contains("xem") || lower.contains("có gì") || lower.contains("co gi") ||
                lower.contains("danh sách") || lower.contains("danh sach")

            if (isCheckingOnly) {
                if (pantryItems.isEmpty()) {
                    return listOf(
                        AiSuggestion(
                            title = "Tủ lạnh trống",
                            summary = "Tủ lạnh chưa có nguyên liệu nào.",
                            matchedRecipe = null,
                            detail = "🧊 Tủ lạnh của bạn hiện đang trống.\n\nHãy vào mục 'Tủ lạnh' để thêm nguyên liệu đang có, sau đó hỏi lại để mình gợi ý món nhé!",
                            source = "rule-based"
                        )
                    )
                }
                val itemsList = pantryItems.joinToString("\n") { "• ${it.name}: ${it.amount} ${it.unit}" }
                return listOf(
                    AiSuggestion(
                        title = "Tủ lạnh (${pantryItems.size} món)",
                        summary = "Tủ lạnh có ${pantryItems.size} nguyên liệu.",
                        matchedRecipe = null,
                        detail = "🧊 Các nguyên liệu hiện có trong tủ lạnh:\n$itemsList\n\n💡 Bạn có thể bấm chip 'Từ tủ lạnh' để nhận gợi ý món nấu ngay!",
                        source = "rule-based"
                    )
                )
            }

            // Gợi ý món từ tủ lạnh (kết hợp các món khớp nguyên liệu + bổ sung công thức sẵn có để đủ 3 món)
            val matchedFromPantry = repository.suggestFromPantry()
            val available = repository.randomRecipes(10)
            val combined = (matchedFromPantry + available).distinctBy { it.id }.take(3)

            if (combined.isNotEmpty()) {
                val pantryNames = pantryItems.map { it.name }.take(6)
                val pantryHeader = if (pantryNames.isNotEmpty()) {
                    "🧊 Dựa trên nguyên liệu có sẵn (${pantryNames.joinToString(", ")}):\n\n"
                } else {
                    "🍳 Gợi ý các món ngon dễ nấu dành cho bạn:\n\n"
                }

                val fullResponse = buildString {
                    append(pantryHeader)
                    combined.forEachIndexed { index, rec ->
                        val time = rec.prepTime + rec.cookTime
                        val desc = rec.description.ifBlank { "Món ăn thanh đạm, thơm ngon, dễ chuẩn bị." }
                        append("${index + 1}. 🍽 **${rec.name}** (⏱ $time phút · 👥 ${rec.servings} người)\n")
                        append("   - $desc\n")
                    }
                    append("\n💡 Bạn có thể bấm vào công thức tương ứng trong thư viện để xem chi tiết các bước nấu!")
                }

                return listOf(
                    AiSuggestion(
                        title = "Gợi ý ${combined.size} món từ tủ lạnh",
                        summary = combined.joinToString(", ") { it.name },
                        matchedRecipe = combined.firstOrNull(),
                        detail = fullResponse,
                        source = "rule-based"
                    )
                )
            }
        }

        val all = repository.randomRecipes(20)
        val wantsQuick = lower.contains("nhanh") || lower.contains("15 phút") || lower.contains("10 phút")
        val wantsVegetarian = lower.contains("chay")
        val wantsSpicy = lower.contains("cay")
        val filtered = all.filter { r ->
            (!wantsQuick || (r.prepTime + r.cookTime) <= 30) &&
                (!wantsVegetarian || r.name.contains("chay", ignoreCase = true)) &&
                (!wantsSpicy || true)
        }.take(3)
        val base = filtered.ifEmpty { all.take(3) }

        if (base.isNotEmpty()) {
            val responseText = buildString {
                append("🍽 Gợi ý các món phù hợp cho bạn:\n\n")
                base.forEachIndexed { index, rec ->
                    val time = rec.prepTime + rec.cookTime
                    val desc = rec.description.ifBlank { "Món ngon từ thư viện." }
                    append("${index + 1}. **${rec.name}** (⏱ $time phút · 👥 ${rec.servings} người)\n")
                    append("   - $desc\n")
                }
            }
            return listOf(
                AiSuggestion(
                    title = base.first().name,
                    summary = base.joinToString(", ") { it.name },
                    matchedRecipe = base.first(),
                    detail = responseText,
                    source = "rule-based"
                )
            )
        }

        return listOf(
            AiSuggestion(
                title = "Chưa có gợi ý",
                summary = "Chưa tìm thấy món phù hợp.",
                matchedRecipe = null,
                detail = "Chưa tìm thấy công thức phù hợp trong thư viện.",
                source = "rule-based"
            )
        )
    }
}