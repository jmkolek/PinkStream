package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.icons.PageIcon
import kotlinx.serialization.json.Json

/** A layout read from text, and whether it was in the old (pre key art) shape. */
data class DecodedLayout(val layout: DeckLayout, val migrated: Boolean)

/**
 * Reads saved layouts and backup files in either shape. The current shape is tried
 * first; it needs `icon` and `keys` on every page, which old layouts don't have, so
 * an old layout can't be mistaken for a new one (and the other way round).
 */
object LayoutMigration {

    /** null when [text] is neither shape. */
    fun decode(json: Json, text: String): DecodedLayout? {
        decodeOrNull(json, text, DeckLayout.serializer())?.let { return DecodedLayout(it, migrated = false) }
        val legacy = decodeOrNull(json, text, LegacyLayout.serializer()) ?: return null
        return DecodedLayout(legacy.toCurrent(), migrated = true)
    }

    private fun <T> decodeOrNull(json: Json, text: String, serializer: kotlinx.serialization.KSerializer<T>): T? = try {
        json.decodeFromString(serializer, text)
    } catch (e: IllegalArgumentException) {   // SerializationException is one of these
        null
    }

    /** Sections are flattened in order (the mic hero first); ids, actions and OBS names are kept. */
    internal fun LegacyLayout.toCurrent(): DeckLayout = DeckLayout(
        pages.map { page ->
            DeckPage(
                id = page.id,
                name = page.name,
                icon = pageIconFor(page.name),
                keys = page.sections.flatMap { it.buttons }.map { it.toKey() },
            )
        },
    )

    private fun LegacyButton.toKey() = DeckKey(
        id = id,
        label = label,
        art = artFor(label, actionName, icon),
        actionId = actionId,
        actionName = actionName,
        onAirScene = onAirScene,
        stateInput = stateInput,
        confirm = confirm,
    )

    /** The label's words first, then the action name's, then the old line icon. */
    internal fun artFor(label: String, actionName: String?, icon: String): DeckKeyArt =
        guessKeyArt(label)
            ?: actionName?.let(::guessKeyArt)
            ?: ART_BY_OLD_ICON[icon]
            ?: DeckKeyArt.HIGHLIGHT

    internal fun pageIconFor(name: String): PageIcon = when (name.trim().lowercase()) {
        "main" -> PageIcon.GRID
        "model" -> PageIcon.SMILE
        "extras" -> PageIcon.SPARKLES
        else -> PageIcon.STAR
    }

    private val ART_BY_OLD_ICON = mapOf(
        "CLOCK" to DeckKeyArt.STARTING_SOON,
        "GAMEPAD" to DeckKeyArt.GAMING,
        "CHAT" to DeckKeyArt.JUST_CHATTING,
        "COFFEE" to DeckKeyArt.BRB,
        "MOON" to DeckKeyArt.ENDING,
        "EYE_OFF" to DeckKeyArt.PRIVACY,
        "MIC" to DeckKeyArt.MIC,
        "VOLUME" to DeckKeyArt.DESKTOP_AUDIO,
        "MUSIC" to DeckKeyArt.MUSIC,
        "SPARKLES" to DeckKeyArt.APPLAUSE,
        "SMILE" to DeckKeyArt.HAPPY,
        "HEART" to DeckKeyArt.BLUSH,
        "RESET" to DeckKeyArt.RESET_MODEL,
        "STAR" to DeckKeyArt.HIGHLIGHT,
        "CAMERA" to DeckKeyArt.CAMERA,
        "SCISSORS" to DeckKeyArt.CREATE_CLIP,
        "FLAG" to DeckKeyArt.STREAM_MARKER,
        "PLAY" to DeckKeyArt.GO_LIVE,
        "MONITOR" to DeckKeyArt.SCREEN_SHARE,
        "BOLT" to DeckKeyArt.HIGHLIGHT,
    )
}
