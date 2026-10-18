package com.example.pinkstream.data.deck

import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.ui.icons.PageIcon
import java.util.UUID

// Small, pure changes to a layout. Each returns a new DeckLayout; the editor and the
// deck combine them inside DeckRepository.update { }.

/** A fresh id for a page or key. */
fun newDeckId(): String = UUID.randomUUID().toString()

/** The dock has room for this many tabs. */
const val MAX_PAGES = 5

/** Where a key sits in the layout. */
data class KeyLocation(val page: DeckPage, val index: Int, val key: DeckKey)

fun DeckLayout.locate(keyId: String): KeyLocation? {
    for (page in pages) {
        val index = page.keys.indexOfFirst { it.id == keyId }
        if (index >= 0) return KeyLocation(page, index, page.keys[index])
    }
    return null
}

fun DeckLayout.findPage(pageId: String): DeckPage? = pages.firstOrNull { it.id == pageId }

/** Swaps in [key] where the key with the same id is. */
fun DeckLayout.replaceKey(key: DeckKey): DeckLayout =
    mapPages { page -> page.copy(keys = page.keys.map { if (it.id == key.id) key else it }) }

fun DeckLayout.removeKey(keyId: String): DeckLayout =
    mapPages { page -> page.copy(keys = page.keys.filterNot { it.id == keyId }) }

/** Puts [key] into the page at [index] (clamped, so a too-large index appends). */
fun DeckLayout.insertKey(pageId: String, index: Int, key: DeckKey): DeckLayout =
    mapPages { page ->
        if (page.id != pageId) {
            page
        } else {
            val keys = page.keys.toMutableList()
            keys.add(index.coerceIn(0, keys.size), key)
            page.copy(keys = keys)
        }
    }

/**
 * Moves the key at [from] to [to] on a page (drag and drop in edit layout mode); the
 * keys in between shift over. Out-of-range positions leave the layout as it is.
 */
fun DeckLayout.moveKey(pageId: String, from: Int, to: Int): DeckLayout {
    val page = findPage(pageId) ?: return this
    if (from == to || from !in page.keys.indices || to !in page.keys.indices) return this
    return mapPages { if (it.id == pageId) it.copy(keys = it.keys.moved(from, to)) else it }
}

/** An empty page. */
fun newPage(name: String, icon: PageIcon): DeckPage = DeckPage(id = newDeckId(), name = name, icon = icon, keys = emptyList())

/** Adds a page at the end, unless the dock is full ([MAX_PAGES]). */
fun DeckLayout.addPage(page: DeckPage): DeckLayout = if (pages.size >= MAX_PAGES) this else copy(pages = pages + page)

/** The page sheet's Save: a page's name and dock icon. */
fun DeckLayout.updatePage(pageId: String, name: String, icon: PageIcon): DeckLayout =
    copy(pages = pages.map { if (it.id == pageId) it.copy(name = name, icon = icon) else it })

/** Moves a page [by] places in the dock (−1 = left, 1 = right), staying inside the dock. */
fun DeckLayout.movePage(pageId: String, by: Int): DeckLayout {
    val from = pages.indexOfFirst { it.id == pageId }
    if (from < 0) return this
    val to = (from + by).coerceIn(0, pages.lastIndex)
    if (to == from) return this
    return copy(pages = pages.moved(from, to))
}

/** Removes a page, except the last one: the deck always has at least one. */
fun DeckLayout.removePage(pageId: String): DeckLayout =
    if (pages.size <= 1) this else copy(pages = pages.filterNot { it.id == pageId })

/** The key that runs [action]: same id, or (for keys without an id yet) the same name. */
fun DeckLayout.keyUsing(action: ActionInfo): DeckKey? =
    pages.asSequence()
        .flatMap { it.keys }
        .firstOrNull { it.runs(action) }

/** Whether this key is set to run [action]. */
fun DeckKey.runs(action: ActionInfo): Boolean =
    if (actionId != null) actionId == action.id else actionName == action.name

/** Every key on every page, in order. */
val DeckLayout.allKeys: List<DeckKey> get() = pages.flatMap { it.keys }

/** This list with the item at [from] taken out and put back in at [to]. */
fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    val list = toMutableList()
    list.add(to, list.removeAt(from))
    return list
}

private fun DeckLayout.mapPages(change: (DeckPage) -> DeckPage): DeckLayout = copy(pages = pages.map(change))
