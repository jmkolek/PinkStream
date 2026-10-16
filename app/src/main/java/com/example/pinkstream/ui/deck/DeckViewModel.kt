package com.example.pinkstream.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinkstream.StreamDeckApp
import com.example.pinkstream.data.deck.ActionCatalog
import com.example.pinkstream.data.deck.DeckButton
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.deck.DeckRepository
import com.example.pinkstream.data.live.LiveState
import com.example.pinkstream.data.live.LiveStateRepository
import com.example.pinkstream.data.settings.SettingsRepository
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.data.streamerbot.StreamerBotException
import com.example.pinkstream.ui.components.ChipStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything the deck draws. */
data class DeckUiState(
    val pages: List<DeckPage>,
    val columns: Int,
    val vibrate: Boolean,
    val keepScreenOn: Boolean,
    val chip: ChipStatus,
    val connected: Boolean,
    /** null = not fetched yet; tiles then don't claim "No action". */
    val actions: List<ActionInfo>?,
    val live: LiveState,
)

/** A Snackbar. [offerSettings] adds a "Settings" button. */
data class DeckMessage(val text: String, val offerSettings: Boolean = false)

enum class TileFeedback { SUCCESS, ERROR }

/** Tells one tile to play its success pulse or error shake. */
data class TileEvent(val buttonId: String, val feedback: TileFeedback)

class DeckViewModel(
    deck: DeckRepository,
    settings: SettingsRepository,
    private val client: StreamerBotClient,
    actions: ActionCatalog,
    private val liveState: LiveStateRepository,
) : ViewModel() {

    private data class ConnectionView(val chip: ChipStatus, val connected: Boolean, val failed: Boolean)

    // scan() carries the "failed since connected" flag from one state to the next.
    private val connection: Flow<ConnectionView> =
        client.state.scan(ConnectionView(ChipStatus.CONNECTING, connected = false, failed = false)) { previous, state ->
            val failed = failedSinceConnected(state, previous.failed)
            ConnectionView(chipStatus(state, failed), state is ConnectionState.Connected, failed)
        }

    /** null until the layout and settings have loaded (a few milliseconds). */
    val uiState: StateFlow<DeckUiState?> =
        combine(deck.layout, settings.settings, connection, actions.actions, liveState.state) { layout, s, conn, list, live ->
            DeckUiState(layout.pages, s.columns, s.vibrate, s.keepScreenOn, conn.chip, conn.connected, list, live)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _confirming = MutableStateFlow<DeckButton?>(null)
    /** The button whose "Run …?" dialog is showing. */
    val confirming: StateFlow<DeckButton?> = _confirming.asStateFlow()

    private val _messages = Channel<DeckMessage>(Channel.BUFFERED)
    val messages: Flow<DeckMessage> = _messages.receiveAsFlow()

    // A SharedFlow, not a Channel: every tile listens and picks out its own events.
    private val _tileEvents = MutableSharedFlow<TileEvent>(extraBufferCapacity = 16)
    val tileEvents: SharedFlow<TileEvent> = _tileEvents.asSharedFlow()

    val preview: StateFlow<LivePreview> =
        liveState.preview
            .map { p -> LivePreview.entries.firstOrNull { it.state == p } ?: LivePreview.REAL }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LivePreview.REAL)

    /** A tap on a tile whose action exists. (Tiles without one open the editor instead.) */
    fun onTap(button: DeckButton) {
        when {
            uiState.value?.connected != true ->
                _messages.trySend(DeckMessage("Not connected to your PC", offerSettings = true))
            button.confirm -> _confirming.value = button
            else -> run(button)
        }
    }

    fun confirmRun() {
        val button = _confirming.value ?: return
        _confirming.value = null
        run(button)
    }

    fun dismissConfirm() {
        _confirming.value = null
    }

    fun onEditLayout() {
        _messages.trySend(DeckMessage("Editing the layout arrives in milestone 4."))
    }

    fun setPreview(preview: LivePreview) = liveState.setPreview(preview.state)

    private fun run(button: DeckButton) {
        viewModelScope.launch {
            try {
                client.doAction(id = button.actionId, name = button.actionName)
                _tileEvents.emit(TileEvent(button.id, TileFeedback.SUCCESS))
            } catch (e: StreamerBotException) {
                _tileEvents.emit(TileEvent(button.id, TileFeedback.ERROR))
                _messages.send(DeckMessage("Couldn't run ${button.label}. ${e.message}"))
            }
        }
    }

    companion object {
        /** Tells Compose's viewModel() how to build this ViewModel with the shared objects. */
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as StreamDeckApp).container
                DeckViewModel(c.deck, c.settings, c.streamerBot, c.actions, c.liveState)
            }
        }
    }
}
