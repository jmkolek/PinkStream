package com.example.pinkstream.ui.edit

import com.example.pinkstream.data.deck.DeckButton
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.TileColor
import kotlinx.serialization.Serializable

/**
 * The Edit button form while it's being changed. The switches and their text fields
 * are kept separately, so turning a switch off and on again doesn't lose the name.
 * Serializable so the unsaved form survives Android stopping the app in the background.
 */
@Serializable
data class ButtonDraft(
    val label: String,
    val icon: DeckIcon,
    val color: TileColor,
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

fun DeckButton.toDraft() = ButtonDraft(
    label = label,
    icon = icon,
    color = color,
    actionId = actionId,
    actionName = actionName,
    onAir = onAirScene != null,
    onAirScene = onAirScene.orEmpty(),
    showState = stateInput != null,
    stateInput = stateInput.orEmpty(),
    confirm = confirm,
)

/** What "+ Add" starts with. */
fun newButtonDraft(color: TileColor) = ButtonDraft(
    label = "",
    icon = DeckIcon.BOLT,
    color = color,
    actionId = null,
    actionName = null,
    onAir = false,
    onAirScene = "",
    showState = false,
    stateInput = "",
    confirm = false,
)

fun ButtonDraft.validate() = DraftErrors(
    label = if (label.isBlank()) "Give the button a name." else null,
    onAirScene = if (onAir && onAirScene.isBlank()) "Enter the scene name exactly as OBS shows it." else null,
    stateInput = if (showState && stateInput.isBlank()) "Enter the source name exactly as OBS shows it." else null,
)

/** The button to save. Text is trimmed; a switch that's off clears its name. */
fun ButtonDraft.toButton(id: String) = DeckButton(
    id = id,
    label = label.trim(),
    icon = icon,
    color = color,
    actionId = actionId,
    actionName = actionName,
    onAirScene = if (onAir) onAirScene.trim() else null,
    stateInput = if (showState) stateInput.trim() else null,
    confirm = confirm,
)

/** Turning "Light up when on air" on with no scene name yet fills in the label. */
fun ButtonDraft.withOnAir(on: Boolean) =
    copy(onAir = on, onAirScene = if (on && onAirScene.isBlank()) label.trim() else onAirScene)

// The icons offered in the row before "More icons", most useful for a stream first.
private val COMMON_ICONS = listOf(
    DeckIcon.GAMEPAD, DeckIcon.CHAT, DeckIcon.COFFEE, DeckIcon.MOON, DeckIcon.SPARKLES,
    DeckIcon.MIC, DeckIcon.VOLUME, DeckIcon.SMILE,
)
const val ICON_ROW_SIZE = 5

/** The row of quick icon choices, starting with the button's current icon. */
fun iconRow(current: DeckIcon): List<DeckIcon> = (listOf(current) + COMMON_ICONS).distinct().take(ICON_ROW_SIZE)

/** After picking from "More icons": an icon already in the row stays put; a new one goes first. */
fun List<DeckIcon>.withChoice(icon: DeckIcon): List<DeckIcon> =
    if (icon in this) this else (listOf(icon) + this).take(size)
