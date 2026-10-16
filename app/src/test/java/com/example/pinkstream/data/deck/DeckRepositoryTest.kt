package com.example.pinkstream.data.deck

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.pinkstream.data.streamerbot.ActionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DeckRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scopes = mutableListOf<CoroutineScope>()

    private val file: File by lazy { File(folder.root, "deck.preferences_pb") }

    private fun openStore() = CoroutineScope(SupervisorJob() + Dispatchers.IO).let { scope ->
        scopes += scope
        PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
    }

    /** Closes the open DataStore and opens a new one on the same file, like a restart. */
    private suspend fun restart(): DeckRepository {
        // Wait until the old one has really shut down: only one DataStore may have the file open.
        scopes.removeAt(0).coroutineContext.job.cancelAndJoin()
        return DeckRepository(openStore())
    }

    @After
    fun tearDown() = scopes.forEach { it.cancel() }

    @Test
    fun firstReadSeedsTheDefaultAndKeepsItsIds() = runBlocking {
        val first = DeckRepository(openStore()).layout.first()
        assertEquals(listOf("Main", "Model", "Extras"), first.pages.map { it.name })

        // Same ids after a restart: the seed was saved, not made up again.
        assertEquals(first, restart().layout.first())
    }

    @Test
    fun fillActionIdsIsSaved() = runBlocking {
        val repository = DeckRepository(openStore())
        repository.layout.first()
        repository.fillActionIds(listOf(ActionInfo(id = "g-1", name = "Deck - Scene Gaming")))

        val gaming = restart().layout.first().pages.first().sections.flatMap { it.buttons }.single { it.label == "Gaming" }
        assertEquals("g-1", gaming.actionId)
    }

    @Test
    fun unreadableLayoutIsReplacedByTheDefault() = runBlocking {
        val store = openStore()
        store.edit { it[stringPreferencesKey("layout")] = "{ not json" }
        val layout = DeckRepository(store).layout.first()
        assertEquals("Main", layout.pages.first().name)
        assertEquals("{ not json", store.data.first()[stringPreferencesKey("layout_unreadable")])
    }
}
