package com.example.pinkstream

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

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

        // ProcessLifecycleOwner tracks the whole app, not one screen: onStart when the
        // first screen becomes visible, onStop shortly after the last one is hidden.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = container.streamerBot.onAppForeground()
            override fun onStop(owner: LifecycleOwner) = container.streamerBot.onAppBackground()
        })
    }
}
