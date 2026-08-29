package com.cookingnote.app.ui.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.cookingnote.app.data.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer chưa được cung cấp")
}
