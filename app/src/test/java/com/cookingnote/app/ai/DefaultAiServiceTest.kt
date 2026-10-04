package com.cookingnote.app.ai

import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.repository.CookbookRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DefaultAiServiceTest {

    private lateinit var mockSettingsStore: AiSettingsStore
    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockSettingsStore = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
        every { mockRepository.observePantry() } returns flowOf(emptyList())
        coEvery { mockRepository.recentFavorites(any()) } returns emptyList()
        coEvery { mockRepository.recentCooked(any()) } returns emptyList()
    }

    @Test
    fun isCloudConfigured_returnsFalse_whenRuleBased() {
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.RULE_BASED, apiKey = "test-key")
        )
        val service = DefaultAiService(mockSettingsStore, mockRepository)
        assertFalse(service.isCloudConfigured)
    }

    @Test
    fun isCloudConfigured_returnsFalse_whenApiKeyIsBlank() {
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.OPENAI_CHAT, apiKey = "")
        )
        val service = DefaultAiService(mockSettingsStore, mockRepository)
        assertFalse(service.isCloudConfigured)
    }

    @Test
    fun isCloudConfigured_returnsTrue_whenCloudProviderAndKeyPresent() {
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.OPENAI_CHAT, apiKey = "sk-123456")
        )
        val service = DefaultAiService(mockSettingsStore, mockRepository)
        assertTrue(service.isCloudConfigured)
    }

    @Test
    fun chat_fallsBackToRuleBased_whenRuleBasedProvider() = runTest {
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.RULE_BASED)
        )
        val service = DefaultAiService(mockSettingsStore, mockRepository)
        val result = service.chat("Gợi ý món ăn")

        assertEquals("rule-based", result.source)
        assertTrue(result.summary.contains("AI chưa cấu hình"))
    }

    @Test
    fun chat_fallsBackToRuleBased_whenApiKeyIsBlank() = runTest {
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.OPENAI_CHAT, apiKey = "")
        )
        val service = DefaultAiService(mockSettingsStore, mockRepository)
        val result = service.chat("Gợi ý món ăn")

        assertEquals("rule-based", result.source)
        assertTrue(result.summary.contains("AI chưa cấu hình"))
    }

    @Test
    fun suggestFromIngredients_returnsLocal_whenApiKeyIsBlank() = runTest {
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.OPENAI_CHAT, apiKey = "")
        )
        coEvery { mockRepository.suggestFromPantry() } returns emptyList()

        val service = DefaultAiService(mockSettingsStore, mockRepository)
        val result = service.suggestFromIngredients(listOf("Trứng", "Cà chua"))

        assertTrue(result.isEmpty())
    }

    @Test
    fun suggestFromImage_returnsFallback_whenApiKeyIsBlank() = runTest {
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.OPENAI_CHAT, apiKey = "")
        )

        val service = DefaultAiService(mockSettingsStore, mockRepository)
        val result = service.suggestFromImage(byteArrayOf(1, 2, 3))

        assertEquals("AI vision chưa cấu hình", result.title)
        assertEquals("rule-based", result.source)
    }

    @Test
    fun isCloudConfigured_returnsTrue_whenLocalProxyEvenIfKeyBlank() {
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.OPENAI_CHAT, baseUrl = "http://localhost:20128/v1", apiKey = "")
        )
        val service = DefaultAiService(mockSettingsStore, mockRepository)
        assertTrue(service.isCloudConfigured)
    }
}
