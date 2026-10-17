package com.example.pinkstream.data.obs

import android.util.Log
import com.example.pinkstream.data.obs.ObsMessages.string
import com.example.pinkstream.data.streamerbot.StreamerBotAuth
import com.example.pinkstream.data.streamerbot.StreamerBotJson
import com.example.pinkstream.data.streamerbot.maskSecrets
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response as HttpResponse
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID

/**
 * A second, read-only WebSocket straight to OBS, for what Streamer.bot doesn't pass on:
 * the scene and mute states when we connect, and every mute change after that.
 * Buttons still run through Streamer.bot; this only asks OBS questions.
 *
 * Works like StreamerBotClient: [connect] keeps reconnecting (1 s, 2 s, 4 s, then every
 * 10 s) until [disconnect], nothing stays open in the background, and a password
 * problem stops the retries.
 */
class ObsClient(private val httpClient: OkHttpClient) {

    // Every mutable field is only touched on this one-thread dispatcher (see StreamerBotClient).
    private val dispatcher = Dispatchers.Default.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _state = MutableStateFlow<ObsConnectionState>(ObsConnectionState.Disconnected)
    val state: StateFlow<ObsConnectionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ObsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ObsEvent> = _events.asSharedFlow()

    private data class Target(val host: String, val port: Int, val password: String)

    private var target: Target? = null
    private var inForeground = true
    private var socket: WebSocket? = null
    private var generation = 0
    private var failedAttempts = 0
    private var retryJob: Job? = null
    private val pending = HashMap<String, CompletableDeferred<JsonObject>>()

    fun connect(host: String, port: Int, password: String): Job =
        scope.launch {
            target = Target(host, port, password)
            failedAttempts = 0
            open(showConnecting = true)
        }

    fun disconnect() {
        scope.launch {
            target = null
            close()
            _state.value = ObsConnectionState.Disconnected
        }
    }

    fun onAppForeground() {
        scope.launch {
            inForeground = true
            if (target != null && socket == null) {
                failedAttempts = 0
                open(showConnecting = true)
            }
        }
    }

    fun onAppBackground() {
        scope.launch {
            inForeground = false
            close()
            _state.value = ObsConnectionState.Disconnected
        }
    }

    // --- Questions for OBS. Each throws ObsException. ------------------------------

    suspend fun programScene(): String? = ObsMessages.programScene(request("GetCurrentProgramScene"))

    suspend fun inputNames(): List<String> = ObsMessages.inputNames(request("GetInputList"))

    /** Throws for an input without audio (a browser source, a colour source…). */
    suspend fun inputMuted(inputName: String): Boolean? =
        ObsMessages.inputMuted(request("GetInputMute", buildJsonObject { put("inputName", inputName) }))

    // --- Connection ---------------------------------------------------------------

    /** [showConnecting] false = a quiet retry: keep showing the failure until something changes. */
    private fun open(showConnecting: Boolean) {
        val t = target ?: return
        if (!inForeground) return
        close()
        val gen = generation
        val request = try {
            Request.Builder()
                .url("ws://${t.host}:${t.port}/")
                .header("Sec-WebSocket-Protocol", "obswebsocket.json")
                .build()
        } catch (e: IllegalArgumentException) {
            _state.value = ObsConnectionState.Failed("That PC address doesn't look right.")
            return
        }
        if (showConnecting || _state.value !is ObsConnectionState.Failed) _state.value = ObsConnectionState.Connecting
        Log.d(TAG, "Connecting to ${request.url}")
        socket = httpClient.newWebSocket(request, Listener(gen))
    }

    private fun close() {
        retryJob?.cancel()
        retryJob = null
        generation++
        socket?.close(1000, null)
        socket = null
        failPending(NOT_CONNECTED)
    }

    private fun onDropped(code: Int?, why: String) {
        val wasConnected = _state.value is ObsConnectionState.Connected
        Log.d(TAG, "Connection dropped: $why")
        generation++
        socket = null
        failPending(NOT_CONNECTED)
        if (target == null || !inForeground) {
            _state.value = ObsConnectionState.Disconnected
            return
        }
        if (code == ObsMessages.CLOSE_AUTHENTICATION_FAILED) {
            // Retrying with the same password won't help.
            _state.value = ObsConnectionState.Failed("Wrong OBS password. Check it and try again.")
            return
        }
        _state.value = ObsConnectionState.Failed(
            if (wasConnected) "Lost the connection to OBS." else "Can't reach OBS. Is it running, with its WebSocket server on?"
        )
        val delaySeconds = RETRY_DELAYS_SECONDS.getOrElse(failedAttempts) { RETRY_DELAYS_SECONDS.last() }
        failedAttempts++
        retryJob = scope.launch {
            delay(delaySeconds * 1000L)
            retryJob = null
            open(showConnecting = false)
        }
    }

    private fun onMessage(text: String) {
        val message = try {
            StreamerBotJson.parseToJsonElement(text).jsonObject
        } catch (e: Exception) {
            Log.w(TAG, "Ignoring a message that isn't a JSON object")
            return
        }
        val op = (message["op"] as? JsonPrimitive)?.intOrNull
        val d = message["d"] as? JsonObject ?: return
        when (op) {
            ObsMessages.OP_HELLO -> onHello(d)
            ObsMessages.OP_IDENTIFIED -> {
                failedAttempts = 0
                _state.value = ObsConnectionState.Connected
            }
            ObsMessages.OP_EVENT -> {
                val type = d.string("eventType") ?: return
                _events.tryEmit(ObsEvent(type, d["eventData"] as? JsonObject ?: JsonObject(emptyMap())))
            }
            ObsMessages.OP_REQUEST_RESPONSE -> d.string("requestId")?.let { pending.remove(it) }?.complete(d)
        }
    }

    /** OBS says hello; we answer with Identify, plus the password proof if it asks for one. */
    private fun onHello(d: JsonObject) {
        val challenge = d["authentication"] as? JsonObject
        val authentication = if (challenge == null) {
            null
        } else {
            val password = target?.password.orEmpty()
            if (password.isEmpty()) {
                _state.value = ObsConnectionState.Failed("OBS password needed", passwordNeeded = true)
                close()
                return
            }
            // OBS uses the same challenge-response scheme as Streamer.bot.
            StreamerBotAuth.authentication(password, challenge.string("salt").orEmpty(), challenge.string("challenge").orEmpty())
        }
        val text = ObsMessages.identify(authentication)
        Log.d(TAG, "→ ${maskSecrets(text)}")
        socket?.send(text)
    }

    // --- Requests -----------------------------------------------------------------

    /** Sends one request and returns its responseData (null if it has none). */
    private suspend fun request(type: String, data: JsonObject? = null): JsonObject? = withContext(dispatcher) {
        if (_state.value !is ObsConnectionState.Connected) throw ObsException(NOT_CONNECTED)
        val ws = socket ?: throw ObsException(NOT_CONNECTED)
        val id = UUID.randomUUID().toString()
        val text = ObsMessages.request(id, type, data)
        val answer = CompletableDeferred<JsonObject>()
        pending[id] = answer
        try {
            Log.d(TAG, "→ $text")
            if (!ws.send(text)) throw ObsException(NOT_CONNECTED)
            val d = withTimeoutOrNull(TIMEOUT_MS) { answer.await() }
                ?: throw ObsException("OBS didn't answer in time.")
            val status = d["requestStatus"] as? JsonObject
            if ((status?.get("result") as? JsonPrimitive)?.booleanOrNull != true) {
                throw ObsException(status?.string("comment") ?: "OBS couldn't do that.")
            }
            d["responseData"] as? JsonObject
        } finally {
            pending.remove(id)
        }
    }

    private fun failPending(reason: String) {
        pending.values.forEach { it.completeExceptionally(ObsException(reason)) }
        pending.clear()
    }

    private inner class Listener(private val gen: Int) : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "← $text")
            scope.launch { if (gen == generation) onMessage(text) }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            scope.launch { if (gen == generation) onDropped(code, "closed ($code $reason)") }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: HttpResponse?) {
            scope.launch { if (gen == generation) onDropped(null, t.toString()) }
        }
    }

    private companion object {
        const val TAG = "Obs"
        const val TIMEOUT_MS = 5_000L
        const val NOT_CONNECTED = "Not connected to OBS"
        val RETRY_DELAYS_SECONDS = listOf(1L, 2L, 4L, 10L)
    }
}
