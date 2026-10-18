package com.example.pinkstream.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinkstream.StreamDeckApp
import com.example.pinkstream.data.deck.ActionCatalog
import com.example.pinkstream.data.deck.DeckLayout
import com.example.pinkstream.data.deck.DeckRepository
import com.example.pinkstream.data.deck.findPage
import com.example.pinkstream.data.deck.guessKeyArt
import com.example.pinkstream.data.deck.insertKey
import com.example.pinkstream.data.deck.locate
import com.example.pinkstream.data.deck.newDeckId
import com.example.pinkstream.data.deck.removeKey
import com.example.pinkstream.data.deck.replaceKey
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.ui.icons.DeckKeyArt
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/** The Edit key form. */
data class EditKeyUiState(
    /** Made from an empty slot: not in the layout until Save. */
    val isNew: Boolean,
    val draft: KeyDraft,
    val errors: DraftErrors = DraftErrors(),
)

/**
 * Edits one key (route `edit/{keyId}`) or makes a new one at the end of a page
 * (route `add/{pageId}`, usually with the action picked for it). Nothing is saved
 * until Save changes; back discards.
 */
class EditKeyViewModel(
    private val savedState: SavedStateHandle,
    private val deck: DeckRepository,
    private val catalog: ActionCatalog,
    client: StreamerBotClient,
) : ViewModel() {

    // Navigation puts the route's {arguments} into the SavedStateHandle.
    private val keyId: String? = savedState["keyId"]
    private val pageId: String? = savedState["pageId"]
    // A new key made from an action in the picker (empty slot → Choose an action).
    private val newActionId: String? = savedState["actionId"]
    private val newActionName: String? = savedState["actionName"]

    /** The id of the key being edited; null for a new one. */
    val editedKeyId: String? get() = keyId

    private val _state = MutableStateFlow<EditKeyUiState?>(null)
    /** null while loading. */
    val state: StateFlow<EditKeyUiState?> = _state.asStateFlow()

    /** The saved layout, for the picker's "On the X button" lines. */
    val layout: StateFlow<DeckLayout?> = deck.layout.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val actions: StateFlow<List<ActionInfo>?> = catalog.actions
    val refreshing: StateFlow<Boolean> = catalog.refreshing
    val connected: StateFlow<Boolean> =
        client.state.map { it is ConnectionState.Connected }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), client.state.value is ConnectionState.Connected)

    private val _closed = Channel<Unit>(Channel.CONFLATED)
    /** Fires when the screen should go back: saved, deleted, or the key is gone. */
    val closed: Flow<Unit> = _closed.receiveAsFlow()

    init {
        viewModelScope.launch { load(deck.layout.first()) }
    }

    private fun load(layout: DeckLayout) {
        // After Android stopped the app in the background, carry on with the unsaved form.
        val restored = restoredDraft()
        val existing = keyId?.let { layout.locate(it) }
        if (existing != null) {
            _state.value = EditKeyUiState(isNew = false, draft = restored ?: existing.key.toDraft())
            return
        }
        if (pageId == null || layout.findPage(pageId) == null) {
            // Deleted meanwhile (or a bad link): nothing to edit.
            _closed.trySend(Unit)
            return
        }
        val fresh = newActionName?.let { newKeyDraftFor(newActionId, it) } ?: newKeyDraft()
        _state.value = EditKeyUiState(isNew = true, draft = restored ?: fresh)
    }

    // --- Form changes ---------------------------------------------------------------

    fun onLabelChange(value: String) = editDraft { it.copy(label = value) }
    fun onArtChange(art: DeckKeyArt) = editDraft { it.copy(art = art) }
    fun onOnAirChange(on: Boolean) = editDraft { it.withOnAir(on) }
    fun onSceneChange(value: String) = editDraft { it.copy(onAirScene = value) }
    fun onShowStateChange(on: Boolean) = editDraft { it.copy(showState = on) }
    fun onInputChange(value: String) = editDraft { it.copy(stateInput = value) }
    fun onConfirmChange(on: Boolean) = editDraft { it.copy(confirm = on) }

    /** From the action picker. A new key with no name yet borrows the action's, and a guessed art. */
    fun onActionChosen(action: ActionInfo) {
        val isNew = _state.value?.isNew == true
        editDraft { draft ->
            val named = draft.label.isNotBlank()
            draft.copy(
                actionId = action.id,
                actionName = action.name,
                label = if (isNew && !named) keyNameFor(action.name) else draft.label,
                art = if (isNew && !named) guessKeyArt(action.name) ?: draft.art else draft.art,
            )
        }
    }

    fun refreshActions() = catalog.refresh()

    // --- Save and delete --------------------------------------------------------------

    fun save() {
        val current = _state.value ?: return
        val errors = current.draft.validate()
        if (!errors.ok) {
            _state.value = current.copy(errors = errors)
            return
        }
        val key = current.draft.toKey(keyId ?: newDeckId())
        viewModelScope.launch {
            deck.update { layout ->
                if (current.isNew) {
                    // Int.MAX_VALUE: insertKey clamps it, so the new key goes last.
                    layout.insertKey(pageId ?: return@update layout, Int.MAX_VALUE, key)
                } else {
                    layout.replaceKey(key)
                }
            }
            _closed.send(Unit)
        }
    }

    fun delete() {
        val id = keyId ?: return
        viewModelScope.launch {
            deck.update { it.removeKey(id) }
            _closed.send(Unit)
        }
    }

    /** Changes the draft. Once errors are showing, they update as the user fixes them. */
    private fun editDraft(change: (KeyDraft) -> KeyDraft) {
        _state.update { state ->
            state ?: return@update null
            val draft = change(state.draft)
            state.copy(draft = draft, errors = if (state.errors.ok) state.errors else draft.validate())
        }
        _state.value?.draft?.let { savedState[DRAFT_KEY] = Json.encodeToString(KeyDraft.serializer(), it) }
    }

    private fun restoredDraft(): KeyDraft? {
        val text = savedState.get<String>(DRAFT_KEY) ?: return null
        return try {
            Json.decodeFromString(KeyDraft.serializer(), text)
        } catch (e: IllegalArgumentException) {   // from an older app version: start fresh
            null
        }
    }

    companion object {
        /**
         * The SavedStateHandle survives the process being killed in the background (it's
         * stored in the activity's saved instance state), unlike the ViewModel itself.
         */
        private const val DRAFT_KEY = "draft"

        /** Tells Compose's viewModel() how to build this ViewModel, with the route's arguments. */
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as StreamDeckApp).container
                EditKeyViewModel(createSavedStateHandle(), c.deck, c.actions, c.streamerBot)
            }
        }
    }
}
