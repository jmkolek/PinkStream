package com.example.pinkstream.data.deck

import android.util.Log
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.data.streamerbot.StreamerBotException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

/**
 * The actions Streamer.bot has, fetched after every connect. The deck uses them to
 * mark tiles whose action is missing, and the layout's missing action ids are filled
 * in from them. The last list is kept while offline.
 */
class ActionCatalog(
    private val client: StreamerBotClient,
    private val deck: DeckRepository,
    private val scope: CoroutineScope,
) {
    private val _actions = MutableStateFlow<List<ActionInfo>?>(null)
    /** null until the first successful GetActions. */
    val actions: StateFlow<List<ActionInfo>?> = _actions.asStateFlow()

    private var refreshJob: Job? = null

    init {
        scope.launch {
            client.state.collect { if (it is ConnectionState.Connected) refresh() }
        }
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            try {
                val list = client.getActions()
                _actions.value = list
                deck.fillActionIds(list)
            } catch (e: StreamerBotException) {
                Log.w(TAG, "GetActions failed: ${e.message}")
            } catch (e: SerializationException) {
                Log.w(TAG, "GetActions answer didn't parse", e)
            }
        }
    }

    private companion object {
        const val TAG = "StreamerBot"
    }
}
