package com.example.pinkstream

import android.content.Context
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Holds the app-wide objects (the Streamer.bot client, repositories) so screens can
 * share one instance of each. This is our hand-written replacement for a DI framework.
 */
class AppContainer(val appContext: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        // A WebSocket sits idle between messages; the pings below detect a dead link instead.
        .readTimeout(0, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    val streamerBot = StreamerBotClient(httpClient)
}
