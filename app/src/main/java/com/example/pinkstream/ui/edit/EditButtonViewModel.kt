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
import com.example.pinkstream.data.deck.SectionStyle
import com.example.pinkstream.data.deck.findSection
import com.example.pinkstream.data.deck.insertButton
import com.example.pinkstream.data.deck.locate
import com.example.pinkstream.data.deck.newDeckId
import com.example.pinkstream.data.deck.removeButton
import com.example.pinkstream.data.deck.replaceButton
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.data.streamerbot.StreamerBotClient
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.TileColor
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

/** The Edit button form. */
data class EditButtonUiState(
    /** Made with "+ Add": not in the layout until Save. */
    val isNew: Boolean,
    /** The full-width mic button: no on-air look, Live/Muted instead of On/Off. */
    val isHero: Boolean,
    val draft: ButtonDraft,
    val errors: DraftErrors = DraftErrors(),
    val iconRow: List<DeckIcon>,
)

/**
 * Edits one button (route `edit/{buttonId}`) or makes a new one in a section
 * (route `add/{sectionId}`). Nothing is saved until Save changes; back discards.
 */
class EditButtonViewModel(
    private val savedState: SavedStateHandle,
    private val deck: DeckRepository,
    private val catalog: ActionCatalog,
    client: StreamerBotClient,
) : ViewModel() {

    // Navigation puts the route's {arguments} into the SavedStateHandle.
    private val buttonId: String? = savedState["buttonId"]
    private val sectionId: String? = savedState["sectionId"]

    /** The id of the button being edited; null for a new one. */
    val editedButtonId: String? get() = buttonId

    private val _state = MutableStateFlow<EditButtonUiState?>(null)
    /** null while loading. */
    val state: StateFlow<EditButtonUiState?> = _state.asStateFlow()

    /** The saved layout, for the picker's "On the X button" lines. */
    val layout: StateFlow<DeckLayout?> = deck.layout.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val actions: StateFlow<List<ActionInfo>?> = catalog.actions
    val refreshing: StateFlow<Boolean> = catalog.refreshing
    val connected: StateFlow<Boolean> =
        client.state.map { it is ConnectionState.Connected }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), client.state.value is ConnectionState.Connected)

    private val _closed = Channel<Unit>(Channel.CONFLATED)
    /** Fires when the screen should go back: saved, deleted, or the button is gone. */
    val closed: Flow<Unit> = _closed.receiveAsFlow()

    init {
        viewModelScope.launch { load(deck.layout.first()) }
    }

    private fun load(layout: DeckLayout) {
        // After Android stopped the app in the background, carry on with the unsaved form.
        val restored = restoredDraft()
        val existing = buttonId?.let { layout.locate(it) }
        if (existing != null) {
            val draft = restored ?: existing.button.toDraft()
            _state.value = EditButtonUiState(
                isNew = false,
                isHero = existing.section.style == SectionStyle.HERO,
                draft = draft,
                iconRow = iconRow(draft.icon),
            )
            return
        }
        val section = sectionId?.let { layout.findSection(it) }
        if (section == null) {
            // Deleted meanwhile (or a bad link): nothing to edit.
            _closed.trySend(Unit)
            return
        }
        // A new tile takes the colour of its neighbours, so a section stays one colour.
        val draft = restored ?: newButtonDraft(section.buttons.lastOrNull()?.color ?: TileColor.ROSE)
        _state.value = EditButtonUiState(
            isNew = true,
            isHero = section.style == SectionStyle.HERO,
            draft = draft,
            iconRow = iconRow(draft.icon),
        )
    }

    // --- Form changes ---------------------------------------------------------------

    fun onLabelChange(value: String) = editDraft { it.copy(label = value) }
    fun onColorChange(color: TileColor) = editDraft { it.copy(color = color) }
    fun onOnAirChange(on: Boolean) = editDraft { it.withOnAir(on) }
    fun onSceneChange(value: String) = editDraft { it.copy(onAirScene = value) }
    fun onShowStateChange(on: Boolean) = editDraft { it.copy(showState = on) }
    fun onInputChange(value: String) = editDraft { it.copy(stateInput = value) }
    fun onConfirmChange(on: Boolean) = editDraft { it.copy(confirm = on) }

    fun onIconChange(icon: DeckIcon) {
        _state.update { it?.copy(iconRow = it.iconRow.withChoice(icon)) }
        editDraft { it.copy(icon = icon) }
    }

    /** From the action picker. A new button with no name yet borrows the action's. */
    fun onActionChosen(action: ActionInfo) {
        val isNew = _state.value?.isNew == true
        editDraft { draft ->
            draft.copy(
                actionId = action.id,
                actionName = action.name,
                label = if (isNew && draft.label.isBlank()) action.name.removePrefix("Deck - ").trim() else draft.label,
            )
        }
        if (isNew && _state.value?.draft?.icon == DeckIcon.BOLT) onIconChange(guessActionStyle(action.name).icon)
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
        val button = current.draft.toButton(buttonId ?: newDeckId())
        viewModelScope.launch {
            deck.update { layout ->
                if (current.isNew) {
                    // Int.MAX_VALUE: insertButton clamps it, so the new button goes last.
                    layout.insertButton(sectionId ?: return@update layout, Int.MAX_VALUE, button)
                } else {
                    layout.replaceButton(button)
                }
            }
            _closed.send(Unit)
        }
    }

    fun delete() {
        val id = buttonId ?: return
        viewModelScope.launch {
            deck.update { it.removeButton(id) }
            _closed.send(Unit)
        }
    }

    /** Changes the draft. Once errors are showing, they update as the user fixes them. */
    private fun editDraft(change: (ButtonDraft) -> ButtonDraft) {
        _state.update { state ->
            state ?: return@update null
            val draft = change(state.draft)
            state.copy(draft = draft, errors = if (state.errors.ok) state.errors else draft.validate())
        }
        _state.value?.draft?.let { savedState[DRAFT_KEY] = Json.encodeToString(ButtonDraft.serializer(), it) }
    }

    private fun restoredDraft(): ButtonDraft? {
        val text = savedState.get<String>(DRAFT_KEY) ?: return null
        return try {
            Json.decodeFromString(ButtonDraft.serializer(), text)
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
                EditButtonViewModel(createSavedStateHandle(), c.deck, c.actions, c.streamerBot)
            }
        }
    }
}
