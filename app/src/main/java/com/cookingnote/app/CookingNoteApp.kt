package com.cookingnote.app

import android.app.Application
import com.cookingnote.app.data.AppContainer

class CookingNoteApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
