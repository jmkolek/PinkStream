package com.example.pinkstream.ui.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinkstream.StreamDeckApp
import com.example.pinkstream.data.deck.ActionCatalog
import com.example.pinkstream.data.deck.DeckKey
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.deck.DeckRepository
import com.example.pinkstream.data.deck.addPage
import com.example.pinkstream.data.deck.KeyLocation
import com.example.pinkstream.data.deck.MAX_PAGES
import com.example.pinkstream.data.deck.insertKey
import com.example.pinkstream.data.deck.locate
import com.example.pinkstream.data.deck.newPage
import com.example.pinkstream.data.deck.removeKey
import com.example.pinkstream.data.deck.moveKey
import com.example.pinkstream.data.deck.movePage
import com.example.pinkstream.data.deck.removePage
import com.example.pinkstream.data.deck.updatePage
import com.example.pinkstream.data.live.LiveState
import com.example.pinkstream.data.live.LiveStateRepository
import com.example.pinkstream.data.settings.SettingsRepository
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.data.streamerbot.StreamerBotException
import com.example.pinkstream.ui.components.ChipStatus
import com.example.pinkstream.ui.icons.PageIcon
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
    /** null = not fetched yet; keys then don't claim their action is missing. */
    val actions: List<ActionInfo>?,
    val live: LiveState,
    /** Edit layout mode: remove badges, empty slots, page renaming. */
    val editing: Boolean = false,
)

/** The button a Snackbar can offer. */
enum class MessageAction(val label: String) { OPEN_SETTINGS("Settings"), UNDO_REMOVE("Undo") }

/** A Snackbar, optionally with one [action] button. */
data class DeckMessage(val text: String, val action: MessageAction? = null)

enum class KeyFeedback { SUCCESS, ERROR }

/** Tells one key to play its success flash or error shake. */
data class KeyEvent(val keyId: String, val feedback: KeyFeedback)

class DeckViewModel(
    private val savedState: SavedStateHandle,
    private val deck: DeckRepository,
    settings: SettingsRepository,
    private val client: StreamerBotClient,
    private val catalog: ActionCatalog,
    private val liveState: LiveStateRepository,
) : ViewModel() {

    private data class ConnectionView(val chip: ChipStatus, val connected: Boolean, val failed: Boolean)

    // scan() carries the "failed since connected" flag from one state to the next.
    private val connection: Flow<ConnectionView> =
        client.state.scan(ConnectionView(ChipStatus.CONNECTING, connected = false, failed = false)) { previous, state ->
            val failed = failedSinceConnected(state, previous.failed)
            ConnectionView(chipStatus(state, failed), state is ConnectionState.Connected, failed)
        }

    // In the SavedStateHandle so edit layout mode survives Android stopping the app in the background.
    private val editing: StateFlow<Boolean> = savedState.getStateFlow(EDITING_KEY, false)

    private val baseState: Flow<DeckUiState> =
        combine(deck.layout, settings.settings, connection, catalog.actions, liveState.state) { layout, s, conn, list, live ->
            DeckUiState(layout.pages, s.columns, s.vibrate, s.keepScreenOn, conn.chip, conn.connected, list, live)
        }

    /** null until the layout and settings have loaded (a few milliseconds). */
    val uiState: StateFlow<DeckUiState?> =
        combine(baseState, editing) { state, isEditing -> state.copy(editing = isEditing) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _addedPages = Channel<String>(Channel.BUFFERED)
    /** Ids of pages just added from the dock: the screen moves to them. */
    val addedPages: Flow<String> = _addedPages.receiveAsFlow()

    /** The last removed key, for the Snackbar's Undo. */
    private var lastRemoved: KeyLocation? = null

    private val _confirming = MutableStateFlow<DeckKey?>(null)
    /** The key whose "Run …?" dialog is showing. */
    val confirming: StateFlow<DeckKey?> = _confirming.asStateFlow()

    private val _messages = Channel<DeckMessage>(Channel.BUFFERED)
    val messages: Flow<DeckMessage> = _messages.receiveAsFlow()

    // A SharedFlow, not a Channel: every key listens and picks out its own events.
    private val _keyEvents = MutableSharedFlow<KeyEvent>(extraBufferCapacity = 16)
    val keyEvents: SharedFlow<KeyEvent> = _keyEvents.asSharedFlow()

    /** True while the action picker's refresh is running. */
    val refreshing: StateFlow<Boolean> = catalog.refreshing

    val preview: StateFlow<LivePreview> =
        liveState.preview
            .map { p -> LivePreview.entries.firstOrNull { it.state == p } ?: LivePreview.REAL }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LivePreview.REAL)

    /** A tap on a key whose action exists. (Keys without one open the editor instead.) */
    fun onTap(key: DeckKey) {
        when {
            uiState.value?.connected != true ->
                _messages.trySend(DeckMessage("Not connected to your PC", MessageAction.OPEN_SETTINGS))
            key.confirm -> _confirming.value = key
            else -> run(key)
        }
    }

    fun confirmRun() {
        val key = _confirming.value ?: return
        _confirming.value = null
        run(key)
    }

    fun dismissConfirm() {
        _confirming.value = null
    }

    // --- Edit layout mode. Every change is saved straight away; Done just leaves the mode.

    fun startEditing() {
        savedState[EDITING_KEY] = true
    }

    fun finishEditing() {
        savedState[EDITING_KEY] = false
    }

    fun removeKey(key: DeckKey) {
        viewModelScope.launch {
            val location = deck.layout.first().locate(key.id) ?: return@launch
            deck.update { it.removeKey(key.id) }
            lastRemoved = location
            _messages.send(DeckMessage("Removed ${key.label}", MessageAction.UNDO_REMOVE))
        }
    }

    /** Drag and drop (or TalkBack's Move earlier / later) on one page. */
    fun moveKey(pageId: String, from: Int, to: Int) {
        viewModelScope.launch { deck.update { it.moveKey(pageId, from, to) } }
    }

    fun undoRemove() {
        val location = lastRemoved ?: return
        lastRemoved = null
        viewModelScope.launch {
            deck.update { it.insertKey(location.page.id, location.index, location.key) }
        }
    }

    fun addPage(name: String, icon: PageIcon) {
        val page = newPage(name, icon)
        viewModelScope.launch {
            if (deck.layout.first().pages.size >= MAX_PAGES) return@launch
            deck.update { it.addPage(page) }
            _addedPages.send(page.id)
        }
    }

    fun updatePage(pageId: String, name: String, icon: PageIcon) {
        viewModelScope.launch { deck.update { it.updatePage(pageId, name, icon) } }
    }

    /** Moves a page one place left (−1) or right (1) in the dock. */
    fun movePage(pageId: String, by: Int) {
        viewModelScope.launch { deck.update { it.movePage(pageId, by) } }
    }

    fun deletePage(pageId: String) {
        viewModelScope.launch { deck.update { it.removePage(pageId) } }
    }

    fun setPreview(preview: LivePreview) = liveState.setPreview(preview.state)

    /** The picker's refresh button: GetActions again. */
    fun refreshActions() = catalog.refresh()

    private fun run(key: DeckKey) {
        viewModelScope.launch {
            try {
                val sceneVersion = liveState.sceneVersion()
                client.doAction(id = key.actionId, name = key.actionName)
                key.onAirScene?.let { liveState.markOnAir(it, sceneVersion) }
                _keyEvents.emit(KeyEvent(key.id, KeyFeedback.SUCCESS))
            } catch (e: StreamerBotException) {
                _keyEvents.emit(KeyEvent(key.id, KeyFeedback.ERROR))
                _messages.send(DeckMessage("Couldn't run ${key.label}. ${e.message}"))
            }
        }
    }

    companion object {
        private const val EDITING_KEY = "editing"

        /** Tells Compose's viewModel() how to build this ViewModel with the shared objects. */
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as StreamDeckApp).container
                DeckViewModel(createSavedStateHandle(), c.deck, c.settings, c.streamerBot, c.actions, c.liveState)
            }
        }
    }
}
