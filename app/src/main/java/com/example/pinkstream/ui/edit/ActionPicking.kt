package com.example.pinkstream.ui.edit

import com.example.pinkstream.data.deck.DeckLayout
import com.example.pinkstream.data.deck.keyUsing
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.IconTint

/** The Streamer.bot group the app's actions are meant to live in (docs/PLAN.md). */
const val STREAM_DECK_GROUP = "Stream Deck"

/** Icon and colour for a picker row, guessed from the action's name. */
data class ActionStyle(val icon: DeckIcon, val tint: IconTint)

fun guessActionStyle(name: String): ActionStyle = when {
    name.contains("Scene", ignoreCase = true) -> ActionStyle(DeckIcon.MONITOR, IconTint.ROSE)
    name.contains("Mic", ignoreCase = true) || name.contains("Toggle", ignoreCase = true) ->
        ActionStyle(DeckIcon.VOLUME, IconTint.PEACH)
    name.contains("VTS", ignoreCase = true) -> ActionStyle(DeckIcon.SMILE, IconTint.LILAC)
    else -> ActionStyle(DeckIcon.BOLT, IconTint.MIST)
}

/**
 * A key name from an action name, ready to retype: without the "Deck - " prefix and a
 * leading category word, so "Deck - Scene Gaming" → "Gaming", "Deck - VTS Happy" → "Happy".
 */
fun keyNameFor(actionName: String): String {
    val name = actionName.trim()
        .replace(Regex("^Deck\\s*[-–—:]\\s*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("^(Scene|VTS)\\s+", RegexOption.IGNORE_CASE), "")
        .trim()
    return name.ifEmpty { actionName.trim() }
}

/** The chip selected when the picker opens: "Stream Deck" if that group exists, else all groups (null). */
fun defaultGroup(actions: List<ActionInfo>): String? =
    STREAM_DECK_GROUP.takeIf { actions.any { it.group == STREAM_DECK_GROUP } }

/** Actions in [group] (null = all) whose name contains [query], ignoring case, sorted by name. */
fun filterActions(actions: List<ActionInfo>, query: String, group: String?): List<ActionInfo> {
    val q = query.trim()
    return actions
        .filter { group == null || it.group == group }
        .filter { q.isEmpty() || it.name.contains(q, ignoreCase = true) }
        .sortedBy { it.name.lowercase() }
}

/** Where [query] appears in [text] (every match, ignoring case), for highlighting. */
fun matchRanges(text: String, query: String): List<IntRange> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val ranges = mutableListOf<IntRange>()
    var start = text.indexOf(q, ignoreCase = true)
    while (start >= 0) {
        ranges += start until start + q.length
        start = text.indexOf(q, startIndex = start + q.length, ignoreCase = true)
    }
    return ranges
}

/**
 * The picker row's second line (the mockup's wording says "button"). [isThisKeysAction]
 * = the key being edited currently uses this action, which wins over any other key
 * that also uses it.
 */
fun actionUsage(layout: DeckLayout, action: ActionInfo, isThisKeysAction: Boolean, thisKeyId: String?): String {
    if (isThisKeysAction) return "On this button"
    val other = layout.keyUsing(action)?.takeIf { it.id != thisKeyId }
    return if (other != null) "On the ${other.label} button" else "Not on a button yet"
}
