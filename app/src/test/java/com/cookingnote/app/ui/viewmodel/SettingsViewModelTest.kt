package com.cookingnote.app.ui.viewmodel

import app.cash.turbine.test
import com.cookingnote.app.data.prefs.AiProviderType
import com.cookingnote.app.data.prefs.AiSettings
import com.cookingnote.app.data.prefs.AiSettingsStore
import com.cookingnote.app.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var mockStore: AiSettingsStore
    private val tempDbFile = File.createTempFile("mock_cooking_db", ".sqlite")

    @Before
    fun setUp() {
        mockStore = mockk(relaxed = true)
        every { mockStore.settings } returns MutableStateFlow(
            AiSettings(provider = AiProviderType.GEMINI, model = "gemini-1.5-flash")
        )
    }

    @Test
    fun uiState_initializesWithLoadedSettings() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(mockStore, tempDbFile, testDispatcher)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(AiProviderType.GEMINI, state.provider)
        assertEquals("gemini-1.5-flash", state.model)
        assertTrue(state.isKeyRequired)
    }

    @Test
    fun saveSettings_persistsValuesToStore() = runTest(testDispatcher) {
        var updatedSettings: AiSettings? = null
        coEvery { mockStore.update(any()) } coAnswers {
            val transform = firstArg<(AiSettings) -> AiSettings>()
            updatedSettings = transform(AiSettings())
            Unit
        }

        val viewModel = SettingsViewModel(mockStore, tempDbFile, testDispatcher)
        runCurrent()

        viewModel.onModelChanged("gemini-2.0-flash")
        viewModel.onApiKeyChanged("secret-api-key")
        viewModel.saveAiSettings()
        runCurrent()

        coVerify(exactly = 1) { mockStore.update(any()) }
        assertEquals("gemini-2.0-flash", updatedSettings?.model)
        assertEquals("secret-api-key", updatedSettings?.apiKey)
    }
}
