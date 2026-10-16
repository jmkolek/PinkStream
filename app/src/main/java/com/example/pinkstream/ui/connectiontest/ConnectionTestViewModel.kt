package com.example.pinkstream.ui.connectiontest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinkstream.StreamDeckApp
import com.example.pinkstream.data.settings.ConnectionDefaults
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.data.streamerbot.StreamerBotException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What's typed in the fields, plus any problem to show under them. */
data class ConnectionForm(
    val host: String = ConnectionDefaults.HOST,
    val port: String = ConnectionDefaults.PORT.toString(),
    val password: String = "",
    val hostError: String? = null,
    val portError: String? = null,
)

/** TEMPORARY (milestone 1): drives the connection test screen. Replaced in milestones 2–3. */
class ConnectionTestViewModel(private val client: StreamerBotClient) : ViewModel() {

    private val _form = MutableStateFlow(ConnectionForm())
    val form: StateFlow<ConnectionForm> = _form.asStateFlow()

    val connection: StateFlow<ConnectionState> = client.state

    // A Channel delivers each message once, even if the screen isn't listening yet
    // (e.g. during rotation). Good for one-off things like snackbars.
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun onHostChange(value: String) = _form.update { it.copy(host = value, hostError = null) }
    fun onPortChange(value: String) = _form.update { it.copy(port = value.filter(Char::isDigit), portError = null) }
    fun onPasswordChange(value: String) = _form.update { it.copy(password = value) }

    fun connect() {
        val f = _form.value
        val host = f.host.trim()
        val port = f.port.toIntOrNull()?.takeIf { it in 1..65535 }
        _form.update {
            it.copy(
                hostError = if (host.isEmpty()) "Enter your PC's address." else null,
                portError = if (port == null) "Use a port from 1 to 65535." else null,
            )
        }
        if (host.isEmpty() || port == null) return
        client.connect(host, port, f.password)
    }

    fun disconnect() = client.disconnect()

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
