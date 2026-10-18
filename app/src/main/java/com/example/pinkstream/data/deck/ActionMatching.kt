package com.example.pinkstream.data.deck

import com.example.pinkstream.data.streamerbot.ActionInfo

/**
 * Fills in each key's action id from its name. A key gets the id of the action
 * with exactly its [DeckKey.actionName] when it has no id yet, or when its id is no
 * longer in Streamer.bot (the action was deleted and made again). Returns the same
 * layout object when nothing changed, so callers can skip saving.
 */
fun DeckLayout.withActionIds(actions: List<ActionInfo>): DeckLayout {
    val ids = actions.mapTo(HashSet()) { it.id }
    val idByName = actions.associate { it.name to it.id }
    var changed = false
    val updated = DeckLayout(
        pages.map { page ->
            page.copy(keys = page.keys.map { key ->
                val match = key.actionName?.let { idByName[it] }
                if (match != null && key.actionId !in ids) {
                    changed = true
                    key.copy(actionId = match)
                } else {
                    key
                }
            })
        },
    )
    return if (changed) updated else this
}

/**
 * Whether the key's action can be run. [actions] null = we haven't seen the list
 * yet (never connected), so give the key the benefit of the doubt.
 */
fun DeckKey.hasAction(actions: List<ActionInfo>?): Boolean {
    if (actionId == null && actionName == null) return false
    if (actions == null) return true
    return actions.any { it.id == actionId || it.name == actionName }
}
