package com.example.pinkstream.data.deck

import com.example.pinkstream.data.streamerbot.ActionInfo
import java.util.UUID

// Small, pure changes to a layout. Each returns a new DeckLayout; the editor and the
// deck combine them inside DeckRepository.update { }.

/** A fresh id for a page, section or button. */
fun newDeckId(): String = UUID.randomUUID().toString()

/** Where a button sits in the layout. */
data class ButtonLocation(val page: DeckPage, val section: DeckSection, val index: Int, val button: DeckButton)

fun DeckLayout.locate(buttonId: String): ButtonLocation? {
    for (page in pages) {
        for (section in page.sections) {
            val index = section.buttons.indexOfFirst { it.id == buttonId }
            if (index >= 0) return ButtonLocation(page, section, index, section.buttons[index])
        }
    }
    return null
}

fun DeckLayout.findSection(sectionId: String): DeckSection? =
    pages.firstNotNullOfOrNull { page -> page.sections.firstOrNull { it.id == sectionId } }

/** Swaps in [button] where the button with the same id is. */
fun DeckLayout.replaceButton(button: DeckButton): DeckLayout =
    mapSections { section -> section.copy(buttons = section.buttons.map { if (it.id == button.id) button else it }) }

fun DeckLayout.removeButton(buttonId: String): DeckLayout =
    mapSections { section -> section.copy(buttons = section.buttons.filterNot { it.id == buttonId }) }

/** Puts [button] into the section at [index] (clamped, so a too-large index appends). */
fun DeckLayout.insertButton(sectionId: String, index: Int, button: DeckButton): DeckLayout =
    mapSections { section ->
        if (section.id != sectionId) {
            section
        } else {
            val buttons = section.buttons.toMutableList()
            buttons.add(index.coerceIn(0, buttons.size), button)
            section.copy(buttons = buttons)
        }
    }

/** A new page with one untitled tile section, ready for "+ Add". */
fun newPage(name: String): DeckPage =
    DeckPage(id = newDeckId(), name = name, sections = listOf(DeckSection(newDeckId(), null, SectionStyle.GRID, emptyList())))

fun DeckLayout.addPage(page: DeckPage): DeckLayout = copy(pages = pages + page)

fun DeckLayout.renamePage(pageId: String, name: String): DeckLayout =
    copy(pages = pages.map { if (it.id == pageId) it.copy(name = name) else it })

/** Removes a page, except the last one: the deck always has at least one. */
fun DeckLayout.removePage(pageId: String): DeckLayout =
    if (pages.size <= 1) this else copy(pages = pages.filterNot { it.id == pageId })

/** The button that runs [action]: same id, or (for buttons without an id yet) the same name. */
fun DeckLayout.buttonUsing(action: ActionInfo): DeckButton? =
    pages.asSequence()
        .flatMap { it.sections }
        .flatMap { it.buttons }
        .firstOrNull { it.runs(action) }

/** Whether this button is set to run [action]. */
fun DeckButton.runs(action: ActionInfo): Boolean =
    if (actionId != null) actionId == action.id else actionName == action.name

private fun DeckLayout.mapSections(change: (DeckSection) -> DeckSection): DeckLayout =
    copy(pages = pages.map { page -> page.copy(sections = page.sections.map(change)) })
