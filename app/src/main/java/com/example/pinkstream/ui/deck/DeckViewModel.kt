package com.example.pinkstream.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinkstream.StreamDeckApp
import com.example.pinkstream.data.deck.ActionCatalog
import com.example.pinkstream.data.deck.ButtonLocation
import com.example.pinkstream.data.deck.DeckButton
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.deck.DeckRepository
import com.example.pinkstream.data.deck.addPage
import com.example.pinkstream.data.deck.insertButton
import com.example.pinkstream.data.deck.locate
import com.example.pinkstream.data.deck.newPage
import com.example.pinkstream.data.deck.removeButton
import com.example.pinkstream.data.deck.removePage
import com.example.pinkstream.data.deck.renamePage
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
import kotlinx.coroutines.flow.first
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
    /** Edit layout mode: remove badges, "+ Add" tiles, page renaming. */
    val editing: Boolean = false,
)

/** The button a Snackbar can offer. */
enum class MessageAction(val label: String) { OPEN_SETTINGS("Settings"), UNDO_REMOVE("Undo") }

/** A Snackbar, optionally with one [action] button. */
data class DeckMessage(val text: String, val action: MessageAction? = null)

enum class TileFeedback { SUCCESS, ERROR }

/** Tells one tile to play its success pulse or error shake. */
data class TileEvent(val buttonId: String, val feedback: TileFeedback)

class DeckViewModel(
    private val deck: DeckRepository,
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

    private val editing = MutableStateFlow(false)

    private val baseState: Flow<DeckUiState> =
        combine(deck.layout, settings.settings, connection, actions.actions, liveState.state) { layout, s, conn, list, live ->
            DeckUiState(layout.pages, s.columns, s.vibrate, s.keepScreenOn, conn.chip, conn.connected, list, live)
        }

    /** null until the layout and settings have loaded (a few milliseconds). */
    val uiState: StateFlow<DeckUiState?> =
        combine(baseState, editing) { state, isEditing -> state.copy(editing = isEditing) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _addedPages = Channel<String>(Channel.BUFFERED)
    /** Ids of pages just added with "+": the screen moves to them and asks for a name. */
    val addedPages: Flow<String> = _addedPages.receiveAsFlow()

    /** The last removed button, for the Snackbar's Undo. */
    private var lastRemoved: ButtonLocation? = null

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
                _messages.trySend(DeckMessage("Not connected to your PC", MessageAction.OPEN_SETTINGS))
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

    // --- Edit layout mode. Every change is saved straight away; Done just leaves the mode.

    fun startEditing() {
        editing.value = true
    }

    fun finishEditing() {
        editing.value = false
    }

    fun removeButton(button: DeckButton) {
        viewModelScope.launch {
            val location = deck.layout.first().locate(button.id) ?: return@launch
            deck.update { it.removeButton(button.id) }
            lastRemoved = location
            _messages.send(DeckMessage("Removed ${button.label}", MessageAction.UNDO_REMOVE))
        }
    }

    fun undoRemove() {
        val location = lastRemoved ?: return
        lastRemoved = null
        viewModelScope.launch {
            deck.update { it.insertButton(location.section.id, location.index, location.button) }
        }
    }

    fun addPage() {
        val page = newPage(NEW_PAGE_NAME)
        viewModelScope.launch {
            deck.update { it.addPage(page) }
            _addedPages.send(page.id)
        }
    }

    fun renamePage(pageId: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { deck.update { it.renamePage(pageId, trimmed) } }
    }

    fun deletePage(pageId: String) {
        viewModelScope.launch { deck.update { it.removePage(pageId) } }
    }

    fun setPreview(preview: LivePreview) = liveState.setPreview(preview.state)

    private fun run(button: DeckButton) {
        viewModelScope.launch {
            try {
                val sceneVersion = liveState.sceneVersion()
                client.doAction(id = button.actionId, name = button.actionName)
                button.onAirScene?.let { liveState.markOnAir(it, sceneVersion) }
                _tileEvents.emit(TileEvent(button.id, TileFeedback.SUCCESS))
            } catch (e: StreamerBotException) {
                _tileEvents.emit(TileEvent(button.id, TileFeedback.ERROR))
                _messages.send(DeckMessage("Couldn't run ${button.label}. ${e.message}"))
            }
        }
    }

    companion object {
        const val NEW_PAGE_NAME = "New page"

        /** Tells Compose's viewModel() how to build this ViewModel with the shared objects. */
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as StreamDeckApp).container
                DeckViewModel(c.deck, c.settings, c.streamerBot, c.actions, c.liveState)
            }
        }
    }
}
