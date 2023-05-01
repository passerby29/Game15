package dev.passerby.game15

import android.app.Application

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        GamePreferences.init(this)
    }
}