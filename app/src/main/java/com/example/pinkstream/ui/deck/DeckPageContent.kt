package com.example.pinkstream.ui.deck

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import com.example.pinkstream.R
import com.example.pinkstream.data.deck.DeckKey
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.deck.hasAction
import com.example.pinkstream.ui.components.KeyArtImage
import com.example.pinkstream.ui.components.TonalButton
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/** What a key can ask the screen to do. */
class KeyActions(
    val keyEvents: Flow<KeyEvent>,
    /** Tap on a key whose action exists. */
    val onRun: (DeckKey) -> Unit,
    /** Long-press, a tap on a key with no action, or any tap in edit layout mode. */
    val onEdit: (DeckKey) -> Unit,
    /** The remove badge (edit layout mode). */
    val onRemove: (DeckKey) -> Unit,
    /** An empty slot (edit layout mode), by page id. */
    val onAdd: (String) -> Unit,
    /** A key dropped somewhere new on its page (edit layout mode): page id, from, to. */
    val onMove: (String, Int, Int) -> Unit,
    /** True while a key is being dragged, so the pages don't swipe meanwhile. */
    val onDragging: (Boolean) -> Unit,
)

private const val SETTLE_TIMEOUT_MS = 2_000L

/** Space between keys, both ways (mockup 01). */
val KEY_GAP = 12.dp

/** The width of one key: the screen minus the 20 dp sides, shared by [columns] keys and their gaps. */
fun keySize(width: Dp, columns: Int): Dp = (width - 40.dp - KEY_GAP * (columns - 1)) / columns

/**
 * One page of the deck: its keys in a grid, filling rows left to right, scrolling if
 * they don't fit. In edit layout mode empty slots follow the keys, and a key can be
 * held and dragged to a new place (mockup 02).
 */
@Composable
fun DeckPageContent(page: DeckPage, state: DeckUiState, actions: KeyActions, onEditLayout: () -> Unit) {
    if (page.keys.isEmpty() && !state.editing) {
        EmptyPage(onEditLayout)
        return
    }
    // BoxWithConstraints tells us how big the page is, so keys can be sized to fit.
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val columns = state.columns
        val size = keySize(maxWidth, columns)
        val density = LocalDensity.current
        val sizePx = with(density) { size.toPx() }
        val step = with(density) { (size + KEY_GAP).toPx() }
        val topPadding = if (state.editing) 16.dp else 18.dp
        val keyCount = page.keys.size
        val slotCount = keyCount + if (state.editing) emptySlotCount(keyCount, columns) else 0
        val rows = (slotCount + columns - 1) / columns

        val scroll = rememberScrollState()
        val drag = remember(page.id) { KeyDrag() }
        // The saved order changed (the drop has landed): stop showing the preview order.
        LaunchedEffect(page.keys) { if (drag.settling) drag.reset() }
        // In case the save never shows up (it always should): don't keep the preview order forever.
        LaunchedEffect(drag.settling) {
            if (drag.settling) {
                delay(SETTLE_TIMEOUT_MS)
                drag.reset()
            }
        }
        LaunchedEffect(state.editing) { if (!state.editing) drag.reset() }
        val onDragging by rememberUpdatedState(actions.onDragging)
        LaunchedEffect(drag.keyId != null) { onDragging(drag.keyId != null) }
        EdgeScroll(drag, scroll, viewportHeight = constraints.maxHeight.toFloat(), topPadding = with(density) { topPadding.toPx() }, sizePx, step, columns, keyCount)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                // Inside the scroll, so shadows, badges and the success ring at the edges aren't cut off.
                .padding(start = 20.dp, top = topPadding, end = 20.dp, bottom = 16.dp),
        ) {
            // One box for every key, each placed by offset: a dragged key can then be
            // drawn above all the others (zIndex only works between siblings).
            Box(Modifier.fillMaxWidth().height(size * rows + KEY_GAP * (rows - 1).coerceAtLeast(0))) {
                page.keys.forEachIndexed { index, key ->
                    // key(): each key keeps its own animation state when the order changes.
                    androidx.compose.runtime.key(key.id) {
                        KeySlot(key, index, page, state, actions, drag, size, sizePx, step)
                    }
                }
                for (index in keyCount until slotCount) {
                    EmptySlot(
                        onClick = { actions.onAdd(page.id) },
                        modifier = Modifier
                            .offset { slotOffset(index, columns, step).round() }
                            .size(size),
                    )
                }
            }
        }
    }
}

/** A key in its grid slot, gliding to wherever the current drag shows it. */
@Composable
private fun KeySlot(
    key: DeckKey,
    index: Int,
    page: DeckPage,
    state: DeckUiState,
    actions: KeyActions,
    drag: KeyDrag,
    size: Dp,
    sizePx: Float,
    step: Float,
) {
    val columns = state.columns
    val held = drag.keyId == key.id
    val target = slotOffset(drag.shown(index), columns, step).round()
    // Animatable: the key's position, animated by hand. A held key follows the finger
    // exactly; the others (and the held key after the drop) spring to their slots.
    val position = remember { Animatable(target, IntOffset.VectorConverter) }
    LaunchedEffect(target, held) {
        if (held) {
            snapshotFlow { (slotOffset(drag.from, columns, step) + drag.offset).round() }.collect { position.snapTo(it) }
        } else {
            position.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }
    val modifier = Modifier
        .offset { position.value }
        .size(size)
        .zIndex(if (held || drag.from == index && drag.settling) 1f else 0f)
    if (state.editing) {
        EditableKey(key, index, page, state, actions, drag, sizePx, step, modifier)
    } else {
        Box(modifier) { KeyCell(key, state, actions) }
    }
}

/**
 * Edit layout mode: tap to edit, hold and drag to move, the remove badge in the corner.
 * TalkBack can't drag, so it gets "Move earlier" and "Move later" actions instead.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditableKey(
    key: DeckKey,
    index: Int,
    page: DeckPage,
    state: DeckUiState,
    actions: KeyActions,
    drag: KeyDrag,
    sizePx: Float,
    step: Float,
    modifier: Modifier,
) {
    val view = LocalView.current
    val held = drag.keyId == key.id
    val lift by animateFloatAsState(if (held) 1f else 0f, label = "lift")
    // The gesture below lives as long as the key; these keep it reading current values.
    val currentIndex by rememberUpdatedState(index)
    val currentCount by rememberUpdatedState(page.keys.size)
    val currentColumns by rememberUpdatedState(state.columns)
    val currentStep by rememberUpdatedState(step)
    val vibrate by rememberUpdatedState(state.vibrate)
    val tick = { if (vibrate) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }

    val moveBy = { by: Int -> actions.onMove(page.id, index, index + by) }
    val description = spokenKey(key.label, key.state(state.live).spoken(key.art), "no action".takeUnless { key.hasAction(state.actions) })

    Box(modifier) {
        Box(
            Modifier
                .fillMaxSize()
                // Before the graphicsLayer: drag distances are measured without the tilt and lift.
                .pointerInput(key.id, sizePx) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            drag.start(key.id, currentIndex)
                            tick()
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            drag.dragBy(amount, currentStep, sizePx, currentColumns, currentCount)
                        },
                        onDragEnd = {
                            drag.drop()?.let { (from, to) -> actions.onMove(page.id, from, to) }
                            tick()
                        },
                        onDragCancel = { drag.reset() },
                    )
                }
                .graphicsLayer {
                    // Mockup 02: the held key grows 6 % and tilts 3°.
                    val scale = 1f + 0.06f * lift
                    scaleX = scale
                    scaleY = scale
                    rotationZ = 3f * lift
                }
                .semantics {
                    contentDescription = description
                    customActions = listOfNotNull(
                        CustomAccessibilityAction("Move earlier") { moveBy(-1); true }.takeIf { index > 0 },
                        CustomAccessibilityAction("Move later") { moveBy(1); true }.takeIf { index < page.keys.lastIndex },
                    )
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClickLabel = "Edit",
                    onClick = { actions.onEdit(key) },
                ),
        ) {
            Box(Modifier.clearAndSetSemantics {}) {
                KeyArtImage(key.art, on = key.state(state.live).isOn(), lifted = held, modifier = Modifier.fillMaxSize())
            }
        }
        if (!held) RemoveBadge(key.label, onClick = { actions.onRemove(key) }, modifier = Modifier.align(Alignment.TopEnd))
    }
}

/**
 * While a key is held near the top or bottom of the page, scrolls the page that way
 * (faster the closer it gets) and keeps the key under the finger.
 */
@Composable
private fun EdgeScroll(
    drag: KeyDrag,
    scroll: ScrollState,
    viewportHeight: Float,
    topPadding: Float,
    sizePx: Float,
    step: Float,
    columns: Int,
    count: Int,
) {
    LaunchedEffect(drag.keyId, viewportHeight, step, columns, count) {
        if (drag.keyId == null) return@LaunchedEffect
        val edge = sizePx * 0.6f
        val maxSpeed = sizePx / 8f   // per frame
        while (true) {
            withFrameNanos { }   // once per frame
            val keyTop = topPadding + slotOffset(drag.from, columns, step).y + drag.offset.y - scroll.value
            val centre = keyTop + sizePx / 2
            val speed = when {
                centre < edge -> -maxSpeed * ((edge - centre) / edge).coerceAtMost(1f)
                centre > viewportHeight - edge -> maxSpeed * ((centre - (viewportHeight - edge)) / edge).coerceAtMost(1f)
                else -> 0f
            }
            if (speed != 0f) {
                val scrolled = scroll.scrollBy(speed)
                if (scrolled != 0f) drag.dragBy(Offset(0f, scrolled), step, sizePx, columns, count)
            }
        }
    }
}

/** A key with its "!" badge when its action is missing. Tap runs it (or opens the editor). */
@Composable
private fun KeyCell(key: DeckKey, state: DeckUiState, actions: KeyActions) {
    val hasAction = key.hasAction(state.actions)
    val keyState = key.state(state.live)
    val offline = !state.connected
    val alpha by animateFloatAsState(if (offline) 0.45f else 1f, label = "offline")
    Box(Modifier.fillMaxSize()) {
        PressableKey(
            description = spokenKey(key.label, keyState.spoken(key.art), "no action".takeUnless { hasAction }, offline = offline),
            clickLabel = if (hasAction) "Run" else "Edit",
            vibrate = state.vibrate,
            feedback = rememberFeedback(actions.keyEvents, key.id),
            onClick = { if (hasAction) actions.onRun(key) else actions.onEdit(key) },
            onLongClick = { actions.onEdit(key) },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha },
        ) { flash ->
            KeyArtImage(key.art, on = keyState.isOn() || flash, modifier = Modifier.fillMaxSize())
        }
        if (!hasAction) MissingActionBadge(Modifier.align(Alignment.TopEnd))
    }
}

/** This key's share of the screen-wide key events. */
@Composable
private fun rememberFeedback(events: Flow<KeyEvent>, keyId: String): Flow<KeyFeedback> =
    remember(events, keyId) { events.filter { it.keyId == keyId }.map { it.feedback } }

@Composable
private fun EmptyPage(onEditLayout: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Add keys to this page", style = DeckType.rowTitle, color = DeckTheme.colors.ink)
            Spacer(Modifier.height(16.dp))
            TonalButton(text = "Edit layout", icon = R.drawable.ic_deck_pencil, onClick = onEditLayout)
        }
    }
}
