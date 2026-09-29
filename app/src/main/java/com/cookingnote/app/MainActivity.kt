package com.cookingnote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookingnote.app.ui.CookingNoteRoot
import com.cookingnote.app.ui.local.LocalAppContainer
import com.cookingnote.app.ui.theme.CookingNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CookingNoteApp).container
        setContent {
            val prefs = remember { container.userPrefs }
            val darkOverride by prefs.darkMode.collectAsStateWithLifecycle(initialValue = null)
            CompositionLocalProvider(LocalAppContainer provides container) {
                CookingNoteTheme(
                    darkTheme = darkOverride ?: isSystemInDarkTheme()
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        CookingNoteRoot()
                    }
                }
            }
        }
    }
}
