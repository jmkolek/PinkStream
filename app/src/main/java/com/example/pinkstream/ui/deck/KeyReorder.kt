package com.example.pinkstream.ui.deck

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.floor

// Drag and drop in edit layout mode. Positions are in pixels inside the key grid;
// [step] is one key plus the gap after it, the same both ways.

/** The top-left corner of grid slot [index]. */
fun slotOffset(index: Int, columns: Int, step: Float): Offset =
    Offset((index % columns) * step, (index / columns) * step)

/** The slot under the point ([x], [y]), kept within the [count] keys of the page. */
fun slotAt(x: Float, y: Float, step: Float, columns: Int, count: Int): Int {
    val column = floor(x / step).toInt().coerceIn(0, columns - 1)
    val row = floor(y / step).toInt().coerceAtLeast(0)
    return (row * columns + column).coerceIn(0, (count - 1).coerceAtLeast(0))
}

/**
 * Where the key saved at [index] shows while the key from [from] is held over [to]:
 * the held key takes [to] and the keys in between shift one place to make room.
 */
fun shownIndex(index: Int, from: Int, to: Int): Int = when {
    index == from -> to
    from < to && index in (from + 1)..to -> index - 1
    from > to && index in to until from -> index + 1
    else -> index
}

/**
 * One drag on a page. While a key is held ([keyId]) and until the saved order catches
 * up after the drop ([settling]), the grid shows keys in their [shown] places.
 */
@Stable
class KeyDrag {
    var keyId by mutableStateOf<String?>(null)
        private set
    var from by mutableIntStateOf(-1)
        private set
    var to by mutableIntStateOf(-1)
        private set
    /** How far the held key has moved from its own slot. */
    var offset by mutableStateOf(Offset.Zero)
        private set
    /** Dropped somewhere new; waiting for the saved layout to show the new order. */
    var settling by mutableStateOf(false)
        private set

    fun start(id: String, index: Int) {
        keyId = id
        from = index
        to = index
        offset = Offset.Zero
        settling = false
    }

    /** The finger (or an edge scroll) moved the held key by [delta]. Returns true if it's over a new slot. */
    fun dragBy(delta: Offset, step: Float, size: Float, columns: Int, count: Int): Boolean {
        offset += delta
        val start = slotOffset(from, columns, step)
        val over = slotAt(start.x + offset.x + size / 2, start.y + offset.y + size / 2, step, columns, count)
        if (over == to) return false
        to = over
        return true
    }

    /** Lets go. Returns (from, to) when the key moved, or null when it's back where it was. */
    fun drop(): Pair<Int, Int>? {
        val move = (from to to).takeIf { from != to }
        keyId = null
        if (move == null) reset() else settling = true
        return move
    }

    fun reset() {
        keyId = null
        settling = false
        from = -1
        to = -1
        offset = Offset.Zero
    }

    fun shown(index: Int): Int = if (keyId != null || settling) shownIndex(index, from, to) else index
}
