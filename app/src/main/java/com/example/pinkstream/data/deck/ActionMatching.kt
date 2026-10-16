package com.example.pinkstream.data.deck

import com.example.pinkstream.data.streamerbot.ActionInfo

/**
 * Fills in each button's action id from its name. A button gets the id of the action
 * with exactly its [DeckButton.actionName] when it has no id yet, or when its id is no
 * longer in Streamer.bot (the action was deleted and made again). Returns the same
 * layout object when nothing changed, so callers can skip saving.
 */
fun DeckLayout.withActionIds(actions: List<ActionInfo>): DeckLayout {
    val ids = actions.mapTo(HashSet()) { it.id }
    val idByName = actions.associate { it.name to it.id }
    var changed = false
    val updated = DeckLayout(
        pages.map { page ->
            page.copy(sections = page.sections.map { section ->
                section.copy(buttons = section.buttons.map { button ->
                    val match = button.actionName?.let { idByName[it] }
                    if (match != null && button.actionId !in ids) {
                        changed = true
                        button.copy(actionId = match)
                    } else {
                        button
                    }
                })
            })
        },
    )
    return if (changed) updated else this
}

/**
 * Whether the button's action can be run. [actions] null = we haven't seen the list
 * yet (never connected), so give the button the benefit of the doubt.
 */
fun DeckButton.hasAction(actions: List<ActionInfo>?): Boolean {
    if (actionId == null && actionName == null) return false
    if (actions == null) return true
    return actions.any { it.id == actionId || it.name == actionName }
}
