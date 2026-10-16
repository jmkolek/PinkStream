package com.example.pinkstream

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.pinkstream.data.deck.ActionCatalog
import com.example.pinkstream.data.deck.DeckRepository
import com.example.pinkstream.data.live.LiveStateRepository
import com.example.pinkstream.data.settings.SettingsRepository
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// One DataStore per file for the whole process; the delegate makes sure of that.
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val Context.deckDataStore: DataStore<Preferences> by preferencesDataStore(name = "deck")

/**
 * Holds the app-wide objects (the Streamer.bot client, repositories) so screens can
 * share one instance of each. This is our hand-written replacement for a DI framework.
 */
class AppContainer(val appContext: Context) {

    /**
     * Lives as long as the app. For work that must finish even if the screen that
     * started it closes, such as saving a setting just before navigating back.
     * Main.immediate runs each launch straight away, so saves happen in the order asked.
     */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        // A WebSocket sits idle between messages; the pings below detect a dead link instead.
        .readTimeout(0, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    val streamerBot = StreamerBotClient(httpClient)

    val settings = SettingsRepository(appContext.settingsDataStore)

    val deck = DeckRepository(appContext.deckDataStore)

    val actions = ActionCatalog(streamerBot, deck, appScope)

    val liveState = LiveStateRepository(appScope)

    /** Called once at startup: connect to wherever Settings says the PC is. */
    fun connectWithSavedSettings() {
        appScope.launch {
            val saved = settings.settings.first()
            streamerBot.connect(saved.host, saved.port, saved.password)
        }
    }
}
