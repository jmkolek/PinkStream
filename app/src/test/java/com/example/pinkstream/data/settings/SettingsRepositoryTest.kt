package com.example.pinkstream.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scopes = mutableListOf<CoroutineScope>()

    private val file: File by lazy { File(folder.root, "settings.preferences_pb") }

    /** A fresh DataStore on the same file, like the app after a restart. */
    private fun openStore() = CoroutineScope(SupervisorJob() + Dispatchers.IO).let { scope ->
        scopes += scope
        PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
    }

    @After
    fun tearDown() = scopes.forEach { it.cancel() }

    @Test
    fun freshInstallUsesTheDefaults() = runBlocking {
        val settings = SettingsRepository(openStore()).settings.first()
        assertEquals(AppSettings("127.0.0.1", 8080, "", keepScreenOn = true, vibrate = true, columns = 3, obsPort = 4455, obsPassword = ""), settings)
    }

    @Test
    fun valuesSurviveARestart() = runBlocking {
        SettingsRepository(openStore()).apply {
            setConnection(ConnectionTarget("192.168.1.20", 8081, "secret"))
            setKeepScreenOn(false)
            setVibrate(false)
            setColumns(4)
            setObsConnection(4460, "obs-secret")
        }
        scopes.removeAt(0).cancel()   // only one DataStore may have the file open

        val settings = SettingsRepository(openStore()).settings.first()
        assertEquals(AppSettings("192.168.1.20", 8081, "secret", keepScreenOn = false, vibrate = false, columns = 4, obsPort = 4460, obsPassword = "obs-secret"), settings)
    }

    @Test
    fun singleFieldSettersOnlyChangeTheirField() = runBlocking {
        val repository = SettingsRepository(openStore())
        repository.setPort(9000)
        repository.setHost("10.0.2.2")
        repository.setPassword("pw")
        assertEquals(ConnectionTarget("10.0.2.2", 9000, "pw"), repository.settings.first().connection)
    }

    @Test
    fun obsSharesTheHostButHasItsOwnPortAndPassword() = runBlocking {
        val repository = SettingsRepository(openStore())
        repository.setHost("192.168.1.20")
        repository.setObsPort(4456)
        repository.setObsPassword("obs-pw")
        assertEquals(ConnectionTarget("192.168.1.20", 4456, "obs-pw"), repository.settings.first().obsConnection)
        assertEquals(8080, repository.settings.first().port)
    }

    @Test
    fun badStoredValuesFallBackToDefaults() = runBlocking {
        val store = openStore()
        store.edit {
            it[stringPreferencesKey("host")] = "  "
            it[intPreferencesKey("port")] = 70000
            it[stringPreferencesKey("theme_mode")] = "SEPIA"
            it[intPreferencesKey("columns")] = 7
        }
        assertEquals(AppSettings(), SettingsRepository(store).settings.first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedColumnCountIsRejected() = runBlocking {
        SettingsRepository(openStore()).setColumns(5)
    }
}
