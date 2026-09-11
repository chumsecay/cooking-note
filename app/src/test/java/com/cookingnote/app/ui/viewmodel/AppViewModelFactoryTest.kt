package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.cookingnote.app.data.AppContainer
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AppViewModelFactoryTest {

    private class UnregisteredViewModel : ViewModel()

    private class AnotherUnregisteredViewModel : ViewModel()

    @Test
    fun factory_implementsViewModelProviderFactory() {
        val mockContainer = mockk<AppContainer>(relaxed = true)
        val factory = AppViewModelFactory(container = mockContainer)
        assertTrue("AppViewModelFactory must implement ViewModelProvider.Factory", factory is ViewModelProvider.Factory)
    }

    @Test
    fun factory_acceptsOptionalRecipeId() {
        val mockContainer = mockk<AppContainer>(relaxed = true)
        val factoryWithoutRecipeId = AppViewModelFactory(container = mockContainer)
        val factoryWithRecipeId = AppViewModelFactory(container = mockContainer, recipeId = 42L)
        assertNotNull(factoryWithoutRecipeId)
        assertNotNull(factoryWithRecipeId)
    }

    @Test
    fun create_throwsIllegalArgumentExceptionForUnregisteredViewModel() {
        val mockContainer = mockk<AppContainer>(relaxed = true)
        val factory = AppViewModelFactory(container = mockContainer)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            factory.create(UnregisteredViewModel::class.java)
        }

        assertTrue(
            "Exception message must mention the unknown ViewModel class name",
            exception.message?.contains(UnregisteredViewModel::class.java.name) == true
        )
        assertTrue(
            "Exception message must advise registration in AppViewModelFactory",
            exception.message?.contains("Ensure the ViewModel is registered in AppViewModelFactory") == true
        )
    }

    @Test
    fun createWithCreationExtras_delegatesAndThrowsForUnregisteredViewModel() {
        val mockContainer = mockk<AppContainer>(relaxed = true)
        val factory = AppViewModelFactory(container = mockContainer, recipeId = 100L)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            factory.create(AnotherUnregisteredViewModel::class.java, CreationExtras.Empty)
        }

        assertTrue(
            "Exception message must mention the unknown ViewModel class name",
            exception.message?.contains(AnotherUnregisteredViewModel::class.java.name) == true
        )
    }
}
