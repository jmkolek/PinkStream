package com.example.pinkstream.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinkstream.StreamDeckApp
import com.example.pinkstream.data.obs.ObsClient
import com.example.pinkstream.data.settings.AppSettings
import com.example.pinkstream.data.settings.ConnectionTarget
import com.example.pinkstream.data.settings.ConnectionValidation
import com.example.pinkstream.data.settings.SettingsRepository
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.data.streamerbot.StreamerBotException
import com.example.pinkstream.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException

/** What's typed in the connection fields, plus any problem to show under them. */
data class ConnectionForm(
    val host: String,
    val port: String,
    val password: String,
    val hostError: String? = null,
    val portError: String? = null,
    val passwordVisible: Boolean = false,
    val obsPort: String,
    val obsPassword: String,
    val obsPortError: String? = null,
    val obsPasswordVisible: Boolean = false,
)

/**
 * Drives the Settings screen.
 *
 * Connection fields are saved when they lose focus (or on Test connection), not per
 * keystroke. Saving doesn't reconnect by itself: Test connection does, and so does
 * leaving the screen with an address the client isn't using yet.
 */
class SettingsViewModel(
    private val repository: SettingsRepository,
    private val client: StreamerBotClient,
    private val obs: ObsClient,
    private val appScope: CoroutineScope,
) : ViewModel() {

    /** null until the saved values have loaded. */
    private val _form = MutableStateFlow<ConnectionForm?>(null)
    val form: StateFlow<ConnectionForm?> = _form.asStateFlow()

    /** The saved settings; the Deck card shows these directly. */
    val settings: StateFlow<AppSettings?> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val lookup = MutableStateFlow<ActionsLookup>(ActionsLookup.Unknown)
    private val lastFailure = MutableStateFlow<String?>(null)

    val status: StateFlow<ConnectionStatus> =
        combine(client.state, lookup, lastFailure, ::connectionStatus)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                connectionStatus(client.state.value, ActionsLookup.Unknown, null),
            )

    val obsStatus: StateFlow<ConnectionStatus> =
        obs.state.map(::obsStatus)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), obsStatus(obs.state.value))

    /** Where the clients were last told to connect. Start as the saved values: the app connects with them at launch. */
    private var applied: ConnectionTarget? = null
    private var appliedObs: ConnectionTarget? = null
    private var testJob: Job? = null
    private var lookupJob: Job? = null

    init {
        viewModelScope.launch {
            val saved = repository.settings.first()
            applied = saved.connection
            appliedObs = saved.obsConnection
            _form.value = ConnectionForm(
                saved.host, saved.port.toString(), saved.password,
                obsPort = saved.obsPort.toString(), obsPassword = saved.obsPassword,
            )
        }
        viewModelScope.launch {
            client.state.collect { state ->
                when (state) {
                    is ConnectionState.Connected -> {
                        lastFailure.value = null
                        // During a test, testConnection() asks for the actions itself.
                        if (testJob?.isActive != true) lookUpActions()
                    }
                    is ConnectionState.Failed -> {
                        if (!state.passwordNeeded) lastFailure.value = state.reason
                        clearLookup()
                    }
                    ConnectionState.Connecting, ConnectionState.Authenticating -> clearLookup()
                    ConnectionState.Disconnected -> {
                        lastFailure.value = null
                        clearLookup()
                    }
                }
            }
        }
    }

    // --- Connection fields ----------------------------------------------------------

    fun onHostChange(value: String) = editForm { it.copy(host = value, hostError = null) }

    fun onPortChange(value: String) = editForm { it.copy(port = value.filter(Char::isDigit).take(5), portError = null) }

    fun onPasswordChange(value: String) = editForm { it.copy(password = value) }

    fun togglePasswordVisible() = editForm { it.copy(passwordVisible = !it.passwordVisible) }

    fun onHostFocusLost() {
        val f = _form.value ?: return
        val error = ConnectionValidation.hostError(f.host)
        editForm { it.copy(hostError = error) }
        if (error == null) save { it.setHost(f.host.trim()) }
    }

    fun onPortFocusLost() {
        val f = _form.value ?: return
        val error = ConnectionValidation.portError(f.port)
        editForm { it.copy(portError = error) }
        val port = ConnectionValidation.parsePort(f.port)
        if (port != null) save { it.setPort(port) }
    }

    fun onPasswordFocusLost() {
        val f = _form.value ?: return
        save { it.setPassword(f.password) }
    }

    fun onObsPortChange(value: String) = editForm { it.copy(obsPort = value.filter(Char::isDigit).take(5), obsPortError = null) }

    fun onObsPasswordChange(value: String) = editForm { it.copy(obsPassword = value) }

    fun toggleObsPasswordVisible() = editForm { it.copy(obsPasswordVisible = !it.obsPasswordVisible) }

    fun onObsPortFocusLost() {
        val f = _form.value ?: return
        val error = ConnectionValidation.portError(f.obsPort)
        editForm { it.copy(obsPortError = error) }
        val port = ConnectionValidation.parsePort(f.obsPort)
        if (port != null) save { it.setObsPort(port) }
    }

    fun onObsPasswordFocusLost() {
        val f = _form.value ?: return
        save { it.setObsPassword(f.obsPassword) }
    }

    /** Saves the fields, reconnects to Streamer.bot and OBS with them, then counts the actions. */
    fun testConnection() {
        val f = _form.value ?: return
        val hostError = ConnectionValidation.hostError(f.host)
        val portError = ConnectionValidation.portError(f.port)
        val obsPortError = ConnectionValidation.portError(f.obsPort)
        editForm { it.copy(hostError = hostError, portError = portError, obsPortError = obsPortError) }
        val port = ConnectionValidation.parsePort(f.port)
        val obsPort = ConnectionValidation.parsePort(f.obsPort)
        if (hostError != null || port == null || obsPort == null) return

        val target = ConnectionTarget(f.host.trim(), port, f.password)
        val obsTarget = ConnectionTarget(target.host, obsPort, f.obsPassword)
        save {
            it.setConnection(target)
            it.setObsConnection(obsPort, f.obsPassword)
        }
        applied = target
        appliedObs = obsTarget
        obs.connect(obsTarget.host, obsTarget.port, obsTarget.password)
        lastFailure.value = null
        clearLookup()

        testJob?.cancel()
        testJob = viewModelScope.launch {
            // Wait until the client has started the new attempt, so we don't
            // mistake the old connection's "Connected" for the answer.
            client.connect(target.host, target.port, target.password).join()
            val result = withTimeoutOrNull(TEST_TIMEOUT_MS) {
                client.state.first { it is ConnectionState.Connected || it is ConnectionState.Failed }
            }
            if (result is ConnectionState.Connected) lookUpActions()
        }
    }

    // --- Deck card -------------------------------------------------------------------

    fun setKeepScreenOn(on: Boolean) = save { it.setKeepScreenOn(on) }
    fun setVibrate(on: Boolean) = save { it.setVibrate(on) }
    fun setThemeMode(mode: ThemeMode) = save { it.setThemeMode(mode) }
    fun setColumns(columns: Int) = save { it.setColumns(columns) }

    // --- Leaving the screen ----------------------------------------------------------

    /**
     * The screen is gone for good (the user went back). A field may still have been
     * focused, so save what's valid, and reconnect whichever address changed.
     */
    override fun onCleared() {
        val f = _form.value ?: return
        val saved = settings.value ?: return
        val target = ConnectionTarget(
            host = f.host.trim().takeIf { ConnectionValidation.hostError(it) == null } ?: saved.host,
            port = ConnectionValidation.parsePort(f.port) ?: saved.port,
            password = f.password,
        )
        if (target != saved.connection) save { it.setConnection(target) }
        if (target != applied) client.connect(target.host, target.port, target.password)

        val obsTarget = ConnectionTarget(
            host = target.host,
            port = ConnectionValidation.parsePort(f.obsPort) ?: saved.obsPort,
            password = f.obsPassword,
        )
        if (obsTarget != saved.obsConnection) save { it.setObsConnection(obsTarget.port, obsTarget.password) }
        if (obsTarget != appliedObs) obs.connect(obsTarget.host, obsTarget.port, obsTarget.password)
    }

    // --- Helpers ---------------------------------------------------------------------

    private fun lookUpActions() {
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            lookup.value = ActionsLookup.Loading
            lookup.value = try {
                ActionsLookup.Found(client.getActions().size)
            } catch (e: StreamerBotException) {
                ActionsLookup.Failed
            } catch (e: SerializationException) {
                ActionsLookup.Failed
            }
        }
    }

    private fun clearLookup() {
        lookupJob?.cancel()
        lookup.value = ActionsLookup.Unknown
    }

    private fun editForm(change: (ConnectionForm) -> ConnectionForm) = _form.update { it?.let(change) }

    // appScope, not viewModelScope: a save started just before going back must still finish.
    private fun save(write: suspend (SettingsRepository) -> Unit) {
        appScope.launch { write(repository) }
    }

    companion object {
        private const val TEST_TIMEOUT_MS = 15_000L

        /** Tells Compose's viewModel() how to build this ViewModel with the shared objects. */
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as StreamDeckApp).container
                SettingsViewModel(container.settings, container.streamerBot, container.obs, container.appScope)
            }
        }
    }
}
