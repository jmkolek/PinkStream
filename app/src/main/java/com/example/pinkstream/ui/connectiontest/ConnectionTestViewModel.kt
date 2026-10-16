package com.example.pinkstream.ui.connectiontest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinkstream.StreamDeckApp
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.data.streamerbot.StreamerBotException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** TEMPORARY (milestones 1–2): drives the connection test screen. Replaced in milestone 3. */
class ConnectionTestViewModel(private val client: StreamerBotClient) : ViewModel() {

    val connection: StateFlow<ConnectionState> = client.state

    // A Channel delivers each message once, even if the screen isn't listening yet
    // (e.g. during rotation). Good for one-off things like snackbars.
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun runTestAction() {
        viewModelScope.launch {
            val message = try {
                client.doAction(id = null, name = TEST_ACTION)
                "$TEST_ACTION: ok"
            } catch (e: StreamerBotException) {
                "Couldn't run $TEST_ACTION. ${e.message}"
            }
            _messages.send(message)
        }
    }

    companion object {
        const val TEST_ACTION = "Deck - Test"

        /** Tells Compose's viewModel() how to build this ViewModel with the shared client. */
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StreamDeckApp
                ConnectionTestViewModel(app.container.streamerBot)
            }
        }
    }
}
