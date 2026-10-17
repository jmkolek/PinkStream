package com.example.pinkstream.data.live

import android.util.Log
import com.example.pinkstream.data.obs.ObsClient
import com.example.pinkstream.data.obs.ObsConnectionState
import com.example.pinkstream.data.obs.ObsException
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.data.streamerbot.StreamerBotException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The live state the tiles read. Two sources:
 * - OBS directly ([ObsClient]): scene and mute states, read when it connects and kept
 *   up to date from its events. Used whenever that connection is up.
 * - Streamer.bot's Obs.SceneChanged events: the scene only. The fallback, so scenes
 *   still light up if the OBS connection isn't working (e.g. no `adb reverse` for it).
 *
 * Each source starts unknown when it connects and is unknown while it's down.
 * [preview] is fake data from the debug-only preview and wins while set.
 */
class LiveStateRepository(
    private val streamerBot: StreamerBotClient,
    private val obs: ObsClient,
    private val scope: CoroutineScope,
) {
    private val fromStreamerBot = MutableStateFlow(LiveState())
    private val fromObs = MutableStateFlow(LiveState())

    private val _preview = MutableStateFlow<LiveState?>(null)
    val preview: StateFlow<LiveState?> = _preview.asStateFlow()

    val state: StateFlow<LiveState> =
        combine(fromObs, fromStreamerBot, obs.state, _preview) { fromObs, fromStreamerBot, obsState, preview ->
            preview ?: if (obsState is ObsConnectionState.Connected) fromObs else fromStreamerBot
        }.stateIn(scope, SharingStarted.Eagerly, LiveState())

    // How many scene-change events have arrived from either source. Only touched on the main thread.
    private var sceneChanges = 0
    private var obsLoad: Job? = null

    init {
        scope.launch {
            streamerBot.state.collect { connection ->
                fromStreamerBot.value = LiveState()
                if (connection is ConnectionState.Connected) subscribe()
            }
        }
        scope.launch {
            streamerBot.events.collect { event ->
                if (StreamerBotEvents.isSceneChange(event)) sceneChanges++
                fromStreamerBot.update { StreamerBotEvents.apply(it, event) }
            }
        }
        scope.launch {
            obs.state.collect { connection ->
                obsLoad?.cancel()
                fromObs.value = LiveState()
                if (connection is ObsConnectionState.Connected) obsLoad = scope.launch { loadFromObs() }
            }
        }
        scope.launch {
            obs.events.collect { event ->
                if (ObsLiveEvents.isSceneChange(event)) sceneChanges++
                fromObs.update { ObsLiveEvents.apply(it, event) }
            }
        }
    }

    /** Pass to [markOnAir] after the action runs. Take it before sending the action. */
    fun sceneVersion(): Int = sceneChanges

    /**
     * Optimistic update: a scene button's action just succeeded, so show its scene on
     * air now; the scene event that follows confirms or corrects it. If OBS's event
     * already arrived since [sinceVersion], it is the truth and wins.
     */
    fun markOnAir(scene: String, sinceVersion: Int) {
        if (sceneChanges != sinceVersion) return
        fromObs.update { it.copy(currentScene = scene) }
        fromStreamerBot.update { it.copy(currentScene = scene) }
    }

    /** Debug builds only. null = back to the real state. */
    fun setPreview(state: LiveState?) {
        _preview.value = state
    }

    private fun subscribe() {
        // Launched so a slow answer doesn't hold up the next connection state.
        scope.launch {
            try {
                streamerBot.subscribe(StreamerBotEvents.SUBSCRIPTION)
            } catch (e: StreamerBotException) {
                Log.w(TAG, "Subscribe failed, so scene changes won't come from Streamer.bot: ${e.message}")
            }
        }
    }

    /**
     * Asks OBS for the current scene and every input's mute state. Each answer is applied
     * as it arrives; events that come in meanwhile are newer and are applied after it,
     * because OBS answers and announces in order on the one socket.
     */
    private suspend fun loadFromObs() {
        try {
            obs.programScene()?.let { scene -> fromObs.update { it.copy(currentScene = scene) } }
            for (input in obs.inputNames()) {
                val muted = try {
                    obs.inputMuted(input)
                } catch (e: ObsException) {
                    null   // inputs without audio (browser, colour, display capture) say so
                } ?: continue
                fromObs.update { it.copy(muted = it.muted + (input to muted)) }
            }
        } catch (e: ObsException) {
            Log.w(OBS_TAG, "Couldn't read the state from OBS: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "StreamerBot"
        const val OBS_TAG = "Obs"
    }
}
