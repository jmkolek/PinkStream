package com.example.pinkstream.data.streamerbot

import android.util.Log
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
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response as HttpResponse
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID

/**
 * The WebSocket connection to Streamer.bot.
 *
 * - [connect] remembers where to connect and keeps reconnecting (1 s, 2 s, 4 s, then
 *   every 10 s) until [disconnect]. Auth problems stop the retries: retrying won't fix them.
 * - The app calls [onAppBackground]/[onAppForeground] so nothing stays open in the background.
 * - Requests are matched to responses by their `id` and give up after 5 seconds.
 */
class StreamerBotClient(private val httpClient: OkHttpClient) {

    // Every mutable field below is only touched on this one-thread dispatcher, so no
    // locks are needed. OkHttp calls the listener on its own threads; the listener
    // hops over with scope.launch.
    private val dispatcher = Dispatchers.Default.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<EventEnvelope>(extraBufferCapacity = 64)
    /** Subscribed events (used from milestone 5). */
    val events: SharedFlow<EventEnvelope> = _events.asSharedFlow()

    private data class Target(val host: String, val port: Int, val password: String)

    private var target: Target? = null              // null = the user doesn't want a connection
    private var inForeground = true
    private var socket: WebSocket? = null
    private var generation = 0                      // bumped per socket, so late callbacks from an old one are ignored
    private var failedAttempts = 0
    private var retryJob: Job? = null
    private val pending = HashMap<String, CompletableDeferred<JsonObject>>()

    /**
     * Drops any current connection and connects to the new place. The returned Job
     * finishes once the new attempt has started (state is Connecting, or Failed for a
     * bad address), so a caller can wait for it before watching [state] for the result.
     */
    fun connect(host: String, port: Int, password: String): Job =
        scope.launch {
            target = Target(host, port, password)
            failedAttempts = 0
            open()
        }

    fun disconnect() {
        scope.launch {
            target = null
            close()
            _state.value = ConnectionState.Disconnected
        }
    }

    fun onAppForeground() {
        scope.launch {
            inForeground = true
            if (target != null && socket == null) {
                failedAttempts = 0
                open()
            }
        }
    }

    fun onAppBackground() {
        scope.launch {
            inForeground = false
            close()
            _state.value = ConnectionState.Disconnected
        }
    }

    suspend fun getActions(): List<ActionInfo> {
        val response = request { id -> StreamerBotRequests.getActions(id) }
        return StreamerBotJson.decodeFromJsonElement(GetActionsResponse.serializer(), response).actions
    }

    /** Runs an action. Prefer [id]; [name] is the fallback. Throws [StreamerBotException]. */
    suspend fun doAction(id: String?, name: String?, args: Map<String, String> = emptyMap()) {
        request { requestId -> StreamerBotRequests.doAction(requestId, id, name, args) }
    }

    // --- Connection ---------------------------------------------------------------

    private fun open() {
        val t = target ?: return
        if (!inForeground) return
        close()
        val gen = generation
        val request = try {
            Request.Builder().url("ws://${t.host}:${t.port}/").build()
        } catch (e: IllegalArgumentException) {
            _state.value = ConnectionState.Failed("That PC address doesn't look right.")
            return
        }
        _state.value = ConnectionState.Connecting
        Log.d(TAG, "Connecting to ${request.url}")
        socket = httpClient.newWebSocket(request, Listener(gen))
    }

    /** Drops the current socket (if any) without changing [state]. */
    private fun close() {
        retryJob?.cancel()
        retryJob = null
        generation++
        socket?.close(1000, null)
        socket = null
        failPending(NOT_CONNECTED)
    }

    private fun onDropped(why: String) {
        val wasConnected = _state.value is ConnectionState.Connected
        Log.d(TAG, "Connection dropped: $why")
        generation++   // anything still waiting on this socket (e.g. Authenticate) is now stale
        socket = null
        failPending(NOT_CONNECTED)
        if (target == null || !inForeground) {
            _state.value = ConnectionState.Disconnected
            return
        }
        _state.value = ConnectionState.Failed(
            if (wasConnected) "Lost the connection to your PC." else "Can't reach your PC. Is Streamer.bot running?"
        )
        val delaySeconds = RETRY_DELAYS_SECONDS.getOrElse(failedAttempts) { RETRY_DELAYS_SECONDS.last() }
        failedAttempts++
        retryJob = scope.launch {
            delay(delaySeconds * 1000L)
            retryJob = null
            open()
        }
    }

    private fun onMessage(text: String) {
        val message = try {
            StreamerBotJson.parseToJsonElement(text).jsonObject
        } catch (e: Exception) {
            Log.w(TAG, "Ignoring a message that isn't a JSON object")
            return
        }
        val id = (message["id"] as? JsonPrimitive)?.content
        val waiting = id?.let { pending.remove(it) }
        when {
            waiting != null -> waiting.complete(message)
            (message["request"] as? JsonPrimitive)?.content == "Hello" ->
                onHello(StreamerBotJson.decodeFromJsonElement(Hello.serializer(), message))
            message["event"] != null -> runCatching {
                StreamerBotJson.decodeFromJsonElement(EventEnvelope.serializer(), message)
            }.onSuccess { _events.tryEmit(it) }
        }
    }

    private fun onHello(hello: Hello) {
        val challenge = hello.authentication
        if (challenge == null) {
            onConnected(hello.info)
            return
        }
        val password = target?.password.orEmpty()
        if (password.isEmpty()) {
            _state.value = ConnectionState.Failed(PASSWORD_NEEDED, passwordNeeded = true)
            close()
            return
        }
        _state.value = ConnectionState.Authenticating
        val gen = generation
        // Launched so this coroutine doesn't block the messages (the answer) we wait for.
        scope.launch {
            try {
                val auth = StreamerBotAuth.authentication(password, challenge.salt, challenge.challenge)
                send { id -> StreamerBotRequests.authenticate(id, auth) }
                if (gen == generation) onConnected(hello.info)
            } catch (e: StreamerBotException) {
                if (gen != generation) return@launch
                _state.value = ConnectionState.Failed(
                    if (e.rejected) "Wrong password. Check it and try again." else e.message ?: NOT_CONNECTED
                )
                close()
            }
        }
    }

    private fun onConnected(info: ServerInfo?) {
        failedAttempts = 0
        _state.value = ConnectionState.Connected(info)
    }

    // --- Requests -----------------------------------------------------------------

    private suspend fun request(build: (id: String) -> String): JsonObject {
        if (_state.value !is ConnectionState.Connected) throw StreamerBotException(NOT_CONNECTED)
        return send(build)
    }

    /** Sends one request and waits for the response with the same id. */
    private suspend fun send(build: (id: String) -> String): JsonObject = withContext(dispatcher) {
        val ws = socket ?: throw StreamerBotException(NOT_CONNECTED)
        val id = UUID.randomUUID().toString()
        val text = build(id)
        val answer = CompletableDeferred<JsonObject>()
        pending[id] = answer
        try {
            Log.d(TAG, "→ ${maskSecrets(text)}")
            if (!ws.send(text)) throw StreamerBotException(NOT_CONNECTED)
            val message = withTimeoutOrNull(TIMEOUT_MS) { answer.await() }
                ?: throw StreamerBotException("Your PC didn't answer in time.")
            val response = StreamerBotJson.decodeFromJsonElement(Response.serializer(), message)
            if (!response.isOk) {
                throw StreamerBotException(response.errorMessage ?: "Streamer.bot couldn't do that.", rejected = true)
            }
            message
        } finally {
            pending.remove(id)
        }
    }

    private fun failPending(reason: String) {
        pending.values.forEach { it.completeExceptionally(StreamerBotException(reason)) }
        pending.clear()
    }

    private inner class Listener(private val gen: Int) : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "← ${maskSecrets(text)}")
            scope.launch { if (gen == generation) onMessage(text) }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            scope.launch { if (gen == generation) onDropped("closed ($code)") }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: HttpResponse?) {
            scope.launch { if (gen == generation) onDropped(t.toString()) }
        }
    }

    private companion object {
        const val TAG = "StreamerBot"
        const val TIMEOUT_MS = 5_000L
        const val NOT_CONNECTED = "Not connected to your PC"
        const val PASSWORD_NEEDED = "Password needed"
        val RETRY_DELAYS_SECONDS = listOf(1L, 2L, 4L, 10L)
    }
}
