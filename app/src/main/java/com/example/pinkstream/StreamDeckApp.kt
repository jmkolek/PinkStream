package com.example.pinkstream

import android.app.Application

/**
 * Android creates this once, before any screen, and keeps it for the life of the
 * process: a good home for the [AppContainer]. Registered in AndroidManifest.xml.
 */
class StreamDeckApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
