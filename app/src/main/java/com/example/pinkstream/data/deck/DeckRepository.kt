package com.example.pinkstream.data.deck

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.pinkstream.data.streamerbot.ActionInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * The deck layout, saved as one JSON string in its own DataStore file.
 * The first read on a fresh install saves [DefaultLayout] and returns it. A layout
 * saved by the app before the key art redesign is moved over to the new shape once
 * (the old text is kept under "layout_v1").
 */
class DeckRepository(private val dataStore: DataStore<Preferences>) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    val layout: Flow<DeckLayout> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[LAYOUT]?.let(::decode) }
        // Nothing saved yet, unreadable or in the old shape: save a usable layout. That
        // write makes dataStore.data emit again, this time with the saved layout.
        .onEach { if (it == null || it.migrated) repair() }
        .filter { it != null && !it.migrated }
        .map { it!!.layout }
        .distinctUntilChanged()

    suspend fun save(layout: DeckLayout) {
        dataStore.edit { it[LAYOUT] = encode(layout) }
    }

    /**
     * Changes the saved layout in one step: reads the latest saved version, applies
     * [change] and writes the result, so two quick edits can't undo each other.
     */
    suspend fun update(change: (DeckLayout) -> DeckLayout) {
        dataStore.edit { prefs ->
            val current = prefs.current() ?: return@edit
            prefs[LAYOUT] = encode(change(current))
        }
    }

    /** After GetActions: store the ids of actions that were only known by name. */
    suspend fun fillActionIds(actions: List<ActionInfo>) {
        // Read and write inside one edit() so a save from the editor can't slip in between.
        dataStore.edit { prefs ->
            val current = prefs.current() ?: return@edit
            val updated = current.withActionIds(actions)
            if (updated !== current) prefs[LAYOUT] = encode(updated)
        }
    }

    private suspend fun repair() {
        dataStore.edit { prefs ->
            // Checked again inside edit(), which runs one at a time, so two readers can't both repair.
            val saved = prefs[LAYOUT]
            val decoded = saved?.let(::decode)
            when {
                decoded == null -> {
                    // Keep an unreadable layout around instead of losing it silently.
                    if (saved != null) prefs[BROKEN_LAYOUT] = saved
                    prefs[LAYOUT] = encode(DefaultLayout.create())
                }
                decoded.migrated -> {
                    saved?.let { prefs[LEGACY_LAYOUT] = it }
                    prefs[LAYOUT] = encode(decoded.layout)
                }
            }
        }
    }

    private fun MutablePreferences.current(): DeckLayout? = this[LAYOUT]?.let(::decode)?.layout

    private fun decode(text: String): DecodedLayout? = LayoutMigration.decode(json, text)

    private fun encode(layout: DeckLayout): String = json.encodeToString(DeckLayout.serializer(), layout)

    private companion object {
        val LAYOUT = stringPreferencesKey("layout")
        val BROKEN_LAYOUT = stringPreferencesKey("layout_unreadable")
        val LEGACY_LAYOUT = stringPreferencesKey("layout_v1")
    }
}
