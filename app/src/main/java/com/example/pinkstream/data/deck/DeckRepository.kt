package com.example.pinkstream.data.deck

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.pinkstream.data.streamerbot.ActionInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * The deck layout, saved as one JSON string in its own DataStore file.
 * The first read on a fresh install saves [DefaultLayout] and returns it.
 */
class DeckRepository(private val dataStore: DataStore<Preferences>) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    val layout: Flow<DeckLayout> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[LAYOUT]?.let(::decode) }
        // Nothing saved yet (or unreadable): save the default. That write makes
        // dataStore.data emit again, this time with a layout.
        .onEach { if (it == null) seedDefault() }
        .filterNotNull()
        .distinctUntilChanged()

    suspend fun save(layout: DeckLayout) {
        dataStore.edit { it[LAYOUT] = json.encodeToString(DeckLayout.serializer(), layout) }
    }

    /** After GetActions: store the ids of actions that were only known by name. */
    suspend fun fillActionIds(actions: List<ActionInfo>) {
        // Read and write inside one edit() so a save from the editor can't slip in between.
        dataStore.edit { prefs ->
            val current = prefs[LAYOUT]?.let(::decode) ?: return@edit
            val updated = current.withActionIds(actions)
            if (updated !== current) prefs[LAYOUT] = json.encodeToString(DeckLayout.serializer(), updated)
        }
    }

    private suspend fun seedDefault() {
        dataStore.edit { prefs ->
            // Checked again inside edit(), which runs one at a time, so two readers can't both seed.
            val saved = prefs[LAYOUT]
            if (saved == null || decode(saved) == null) {
                // Keep an unreadable layout around instead of losing it silently.
                if (saved != null) prefs[BROKEN_LAYOUT] = saved
                prefs[LAYOUT] = json.encodeToString(DeckLayout.serializer(), DefaultLayout.create())
            }
        }
    }

    private fun decode(text: String): DeckLayout? = try {
        json.decodeFromString(DeckLayout.serializer(), text)
    } catch (e: IllegalArgumentException) {   // SerializationException is one of these
        null
    }

    private companion object {
        val LAYOUT = stringPreferencesKey("layout")
        val BROKEN_LAYOUT = stringPreferencesKey("layout_unreadable")
    }
}
