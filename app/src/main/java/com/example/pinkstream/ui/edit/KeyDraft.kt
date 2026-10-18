package com.example.pinkstream.ui.edit

import com.example.pinkstream.data.deck.DeckKey
import com.example.pinkstream.data.deck.guessKeyArt
import com.example.pinkstream.ui.icons.DeckKeyArt
import kotlinx.serialization.Serializable

/**
 * The Edit key form while it's being changed. The switches and their text fields
 * are kept separately, so turning a switch off and on again doesn't lose the name.
 * Serializable so the unsaved form survives Android stopping the app in the background.
 */
@Serializable
data class KeyDraft(
    val label: String,
    val art: DeckKeyArt,
    val actionId: String?,
    val actionName: String?,
    val onAir: Boolean,
    val onAirScene: String,
    val showState: Boolean,
    val stateInput: String,
    val confirm: Boolean,
)

/** Problems to show under the fields; all null = ready to save. */
data class DraftErrors(val label: String? = null, val onAirScene: String? = null, val stateInput: String? = null) {
    val ok: Boolean get() = label == null && onAirScene == null && stateInput == null
}

fun DeckKey.toDraft() = KeyDraft(
    label = label,
    art = art,
    actionId = actionId,
    actionName = actionName,
    onAir = onAirScene != null,
    onAirScene = onAirScene.orEmpty(),
    showState = stateInput != null,
    stateInput = stateInput.orEmpty(),
    confirm = confirm,
)

/** What a new key starts with before anything is picked. */
fun newKeyDraft() = KeyDraft(
    label = "",
    art = DeckKeyArt.entries.first(),
    actionId = null,
    actionName = null,
    onAir = false,
    onAirScene = "",
    showState = false,
    stateInput = "",
    confirm = false,
)

/**
 * A new key made from a Streamer.bot action (empty slot → Choose an action): its name
 * without "Deck - ", art guessed from the name, and the switches its name suggests.
 * Mic and desktop audio get OBS's default input names, which the user can change.
 */
fun newKeyDraftFor(actionId: String?, actionName: String): KeyDraft {
    val label = keyNameFor(actionName)
    val input = when {
        Regex("\\bmic\\b", RegexOption.IGNORE_CASE).containsMatchIn(actionName) -> "Mic/Aux"
        actionName.contains("desktop", ignoreCase = true) -> "Desktop Audio"
        else -> null
    }
    return newKeyDraft().copy(
        label = label,
        art = guessKeyArt(actionName) ?: newKeyDraft().art,
        actionId = actionId,
        actionName = actionName,
        showState = input != null,
        stateInput = input.orEmpty(),
    ).withOnAir(actionName.contains("scene", ignoreCase = true))
}

fun KeyDraft.validate() = DraftErrors(
    label = if (label.isBlank()) "Give the key a name. TalkBack reads it, and the on-air chip shows it." else null,
    onAirScene = if (onAir && onAirScene.isBlank()) "Enter the scene name exactly as OBS shows it." else null,
    stateInput = if (showState && stateInput.isBlank()) "Enter the source name exactly as OBS shows it." else null,
)

/** The key to save. Text is trimmed; a switch that's off clears its name. */
fun KeyDraft.toKey(id: String) = DeckKey(
    id = id,
    label = label.trim(),
    art = art,
    actionId = actionId,
    actionName = actionName,
    onAirScene = if (onAir) onAirScene.trim() else null,
    stateInput = if (showState) stateInput.trim() else null,
    confirm = confirm,
)

/** Turning "Show when on air" on with no scene name yet fills in the name. */
fun KeyDraft.withOnAir(on: Boolean) =
    copy(onAir = on, onAirScene = if (on && onAirScene.isBlank()) label.trim() else onAirScene)

/** What the preview calls the second (on) art. */
fun KeyDraft.onArtLabel(): String = when {
    onAir -> "On air"
    showState -> "On"
    else -> "Pressed"
}
