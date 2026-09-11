package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.ai.AiService
import com.cookingnote.app.ai.AiSuggestion
import com.cookingnote.app.data.entity.ChatMessageEntity
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.data.repository.CookbookRepository
import com.cookingnote.app.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var mockAiService: AiService
    private lateinit var mockSettingsStore: AiSettingsStore
    private lateinit var mockRepository: CookbookRepository

    @Before
    fun setUp() {
        mockAiService = mockk(relaxed = true)
        mockSettingsStore = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)

        every { mockRepository.observeChat() } returns flowOf(emptyList())
        every { mockRepository.observePantry() } returns flowOf(emptyList())
        every { mockSettingsStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.RULE_BASED)
        )
    }

    @Test
    fun uiState_whenRuleBasedProvider_showsLocalSuggestionBanner() = runTest(testDispatcher) {
        val viewModel = AiViewModel(mockAiService, mockSettingsStore, mockRepository, testDispatcher)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isCloudConfigured)
        assertTrue(state.statusBannerText.contains("rule-based") || state.statusBannerText.contains("cục bộ"))
    }

    @Test
    fun sendQuery_appendsMessagesAndQueriesAi() = runTest(testDispatcher) {
        val suggestion = AiSuggestion(title = "Món chay", summary = "Đậu sốt", source = "mock")
        coEvery { mockAiService.chat(any(), any()) } returns suggestion

        val viewModel = AiViewModel(mockAiService, mockSettingsStore, mockRepository, testDispatcher)
        runCurrent()

        viewModel.sendMessage("Gợi ý món chay")
        runCurrent()

        coVerify(exactly = 1) { mockRepository.appendMessage("user", "Gợi ý món chay") }
        coVerify(exactly = 1) { mockAiService.chat(any(), any()) }
        coVerify(atLeast = 1) { mockRepository.appendMessage(eq("assistant"), any()) }
    }

    @Test
    fun clearChat_delegatesToRepository() = runTest(testDispatcher) {
        val viewModel = AiViewModel(mockAiService, mockSettingsStore, mockRepository, testDispatcher)
        runCurrent()

        viewModel.clearChat()
        runCurrent()

        coVerify(exactly = 1) { mockRepository.clearChat() }
    }
}
