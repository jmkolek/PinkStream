package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.DeckKeyArt

// Picks key art from words in a name ("Deck - Scene Gaming" → Gaming). Used when an
// old layout is moved over and when a new key is made from a Streamer.bot action.

/** Extra words that point at an art, on top of each art's own name. */
private val SYNONYMS: List<Pair<String, DeckKeyArt>> = listOf(
    "starting" to DeckKeyArt.STARTING_SOON,
    "start scene" to DeckKeyArt.STARTING_SOON,
    "game" to DeckKeyArt.GAMING,
    "ingame" to DeckKeyArt.GAMING,
    "chatting" to DeckKeyArt.JUST_CHATTING,
    "talk" to DeckKeyArt.JUST_CHATTING,
    "be right back" to DeckKeyArt.BRB,
    "break" to DeckKeyArt.BRB,
    "stream end" to DeckKeyArt.ENDING,
    "outro" to DeckKeyArt.ENDING,
    "mic" to DeckKeyArt.MIC,
    "desktop" to DeckKeyArt.DESKTOP_AUDIO,
    "sound" to DeckKeyArt.APPLAUSE,
    "sfx" to DeckKeyArt.APPLAUSE,
    "reset" to DeckKeyArt.RESET_MODEL,
    "clip" to DeckKeyArt.CREATE_CLIP,
    "marker" to DeckKeyArt.STREAM_MARKER,
    "ads" to DeckKeyArt.AD_BREAK,
    "commercial" to DeckKeyArt.AD_BREAK,
    "screenshot" to DeckKeyArt.CAMERA,
    "start stream" to DeckKeyArt.GO_LIVE,
    "stop stream" to DeckKeyArt.END_STREAM,
    "gift" to DeckKeyArt.GIFT_SUBS,
    "bits" to DeckKeyArt.CHEER,
    "slow" to DeckKeyArt.SLOW_MODE,
    "water" to DeckKeyArt.HYDRATE,
    "headphones" to DeckKeyArt.HEADSET,
    "cam" to DeckKeyArt.WEBCAM,
    "screen" to DeckKeyArt.SCREEN_SHARE,
    "replay" to DeckKeyArt.INSTANT_REPLAY,
    "hype" to DeckKeyArt.HYPE_TRAIN,
    "random" to DeckKeyArt.RANDOM_PICK,
    "ban" to DeckKeyArt.BAN_HAMMER,
    "snack" to DeckKeyArt.SNACK_BREAK,
    "food" to DeckKeyArt.SNACK_BREAK,
    "light" to DeckKeyArt.LIGHTS,
)

/**
 * Every word list, longest first, so "end stream" wins over "end" and
 * "ring light" over "light".
 */
private val KEYWORDS: List<Pair<Regex, DeckKeyArt>> =
    (DeckKeyArt.entries.map { it.label.lowercase() to it } + SYNONYMS)
        .sortedByDescending { it.first.length }
        .map { (word, art) -> Regex("\\b" + Regex.escape(word) + "\\b") to art }

/** The art whose name (or a synonym) appears as a whole word in [text], or null. */
fun guessKeyArt(text: String): DeckKeyArt? {
    val lower = text.lowercase()
    return KEYWORDS.firstOrNull { (regex, _) -> regex.containsMatchIn(lower) }?.second
}
