package com.example.pinkstream.ui.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.data.deck.DeckButton
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.deck.SectionStyle
import com.example.pinkstream.data.deck.hasAction
import com.example.pinkstream.ui.components.DeckTile
import com.example.pinkstream.ui.components.MicHero
import com.example.pinkstream.ui.components.SectionLabel
import com.example.pinkstream.ui.components.TileGrid
import com.example.pinkstream.ui.components.TonalButton
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/** What a tile can ask the screen to do. */
class TileActions(
    val tileEvents: Flow<TileEvent>,
    /** Tap on a tile whose action exists. */
    val onRun: (DeckButton) -> Unit,
    /** Long-press, a tap on a tile with no action, or any tap in edit layout mode. */
    val onEdit: (DeckButton) -> Unit,
    /** The remove badge (edit layout mode). */
    val onRemove: (DeckButton) -> Unit,
    /** "+ Add" at the end of a section, by section id. */
    val onAdd: (String) -> Unit,
)

/** True when none of the page's sections has a button. */
fun DeckPage.isEmpty(): Boolean = sections.all { it.buttons.isEmpty() }

/** One page of the deck: its sections top to bottom, scrolling if they don't fit. */
@Composable
fun DeckPageContent(page: DeckPage, state: DeckUiState, actions: TileActions, onEditLayout: () -> Unit) {
    if (page.isEmpty() && !state.editing) {
        EmptyPage(onEditLayout)
        return
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            // Inside the scroll, so shadows and the success ring at the edges aren't cut off.
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
    ) {
        // Sections without buttons only show while editing, so they can get some.
        page.sections.filter { state.editing || it.buttons.isNotEmpty() }.forEachIndexed { index, section ->
            // 16 dp under the page tabs for the first section, as in the mockup; 22 dp between sections.
            val top: Dp = if (index == 0) 16.dp else 22.dp
            if (section.title != null) SectionLabel(section.title, top = top) else Spacer(Modifier.height(top))
            when (section.style) {
                SectionStyle.HERO -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    section.buttons.forEach { EditableTile(it, state, actions) { HeroButton(it, state, actions) } }
                    // The hero holds one button; offer "+ Add" only when it's empty.
                    if (state.editing && section.buttons.isEmpty()) {
                        AddTile(onClick = { actions.onAdd(section.id) }, height = 82.dp, cornerRadius = 26.dp)
                    }
                }
                SectionStyle.GRID -> {
                    // null stands for the "+ Add" tile after the last button.
                    val items: List<DeckButton?> = if (state.editing) section.buttons + null else section.buttons
                    TileGrid(items, columns = state.columns) { button ->
                        if (button == null) {
                            AddTile(onClick = { actions.onAdd(section.id) })
                        } else {
                            EditableTile(button, state, actions) { GridButton(button, state, actions) }
                        }
                    }
                }
            }
        }
    }
}

/** In edit layout mode, puts the remove badge on the tile's top-right corner. */
@Composable
private fun EditableTile(button: DeckButton, state: DeckUiState, actions: TileActions, tile: @Composable () -> Unit) {
    Box {
        tile()
        if (state.editing) {
            RemoveBadge(
                label = button.label,
                onClick = { actions.onRemove(button) },
                // The 22 dp circle pokes 6 dp out of the corner; its 48 dp touch area is centred on it.
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 19.dp, y = (-19).dp),
            )
        }
    }
}

/** Tap: run the action. Without an action, or while editing the layout: open the editor. */
private fun onTileTap(button: DeckButton, hasAction: Boolean, state: DeckUiState, actions: TileActions) {
    if (tapRuns(hasAction, state)) actions.onRun(button) else actions.onEdit(button)
}

private fun tapRuns(hasAction: Boolean, state: DeckUiState) = hasAction && !state.editing

@Composable
private fun GridButton(button: DeckButton, state: DeckUiState, actions: TileActions) {
    val hasAction = button.hasAction(state.actions)
    val look = button.tileLook(state.live, hasAction)
    // Nothing runs while editing, so there's no reason to fade.
    val offline = !state.connected && !state.editing
    PressableTile(
        cornerRadius = 22.dp,
        description = spokenTile(button.label, look.spoken(), offline = offline),
        clickLabel = if (tapRuns(hasAction, state)) "Run" else "Edit",
        vibrate = state.vibrate,
        feedback = rememberFeedback(actions.tileEvents, button.id),
        onClick = { onTileTap(button, hasAction, state, actions) },
        onLongClick = { actions.onEdit(button) },
    ) {
        DeckTile(
            label = button.label,
            icon = button.icon,
            color = button.color,
            look = look,
            offline = offline,
        )
    }
}

@Composable
private fun HeroButton(button: DeckButton, state: DeckUiState, actions: TileActions) {
    val hasAction = button.hasAction(state.actions)
    val micState = button.micState(state.live)
    val offline = !state.connected && !state.editing
    PressableTile(
        cornerRadius = 26.dp,
        description = spokenTile(button.label, micState.spoken(), "no action".takeUnless { hasAction }, offline = offline),
        clickLabel = if (tapRuns(hasAction, state)) "Run" else "Edit",
        vibrate = state.vibrate,
        feedback = rememberFeedback(actions.tileEvents, button.id),
        onClick = { onTileTap(button, hasAction, state, actions) },
        onLongClick = { actions.onEdit(button) },
    ) {
        MicHero(
            label = button.label,
            icon = button.icon,
            state = micState,
            offline = offline,
            subtitle = if (hasAction) null else "No action",
        )
    }
}

/** This tile's share of the screen-wide tile events. */
@Composable
private fun rememberFeedback(events: Flow<TileEvent>, buttonId: String): Flow<TileFeedback> =
    remember(events, buttonId) { events.filter { it.buttonId == buttonId }.map { it.feedback } }

@Composable
private fun EmptyPage(onEditLayout: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Add buttons to this page", style = DeckType.rowTitle, color = DeckTheme.colors.ink)
            Spacer(Modifier.height(16.dp))
            TonalButton(text = "Edit layout", icon = R.drawable.ic_deck_pencil, onClick = onEditLayout)
        }
    }
}
