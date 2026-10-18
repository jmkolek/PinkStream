package com.example.pinkstream.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * The saved settings. DataStore keeps key/value pairs in a small file and hands them
 * out as a Flow, so every screen sees a change the moment it's written.
 * Missing or unreadable values fall back to the [AppSettings] defaults.
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<AppSettings> = dataStore.data
        // A damaged file shouldn't crash the app: start again from the defaults.
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toSettings() }
        .distinctUntilChanged()

    suspend fun setHost(host: String) = set(HOST, host)
    suspend fun setPort(port: Int) = set(PORT, port)
    suspend fun setPassword(password: String) = set(PASSWORD, password)
    suspend fun setKeepScreenOn(on: Boolean) = set(KEEP_SCREEN_ON, on)
    suspend fun setVibrate(on: Boolean) = set(VIBRATE, on)
    suspend fun setObsPort(port: Int) = set(OBS_PORT, port)
    suspend fun setObsPassword(password: String) = set(OBS_PASSWORD, password)

    suspend fun setColumns(columns: Int) {
        require(columns in AppSettings.COLUMN_CHOICES) { "Unsupported column count $columns" }
        set(COLUMNS, columns)
    }

    /** Saves host, port and password in one write. */
    suspend fun setConnection(target: ConnectionTarget) {
        dataStore.edit {
            it[HOST] = target.host
            it[PORT] = target.port
            it[PASSWORD] = target.password
        }
    }

    /** Saves the OBS port and password in one write (the host is shared, see [setConnection]). */
    suspend fun setObsConnection(port: Int, password: String) {
        dataStore.edit {
            it[OBS_PORT] = port
            it[OBS_PASSWORD] = password
        }
    }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    private fun Preferences.toSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            host = this[HOST]?.takeIf { it.isNotBlank() } ?: defaults.host,
            port = this[PORT]?.takeIf { it in 1..65535 } ?: defaults.port,
            password = this[PASSWORD] ?: defaults.password,
            keepScreenOn = this[KEEP_SCREEN_ON] ?: defaults.keepScreenOn,
            vibrate = this[VIBRATE] ?: defaults.vibrate,
            columns = this[COLUMNS]?.takeIf { it in AppSettings.COLUMN_CHOICES } ?: defaults.columns,
            obsPort = this[OBS_PORT]?.takeIf { it in 1..65535 } ?: defaults.obsPort,
            obsPassword = this[OBS_PASSWORD] ?: defaults.obsPassword,
        )
    }

    private companion object {
        val HOST = stringPreferencesKey("host")
        val PORT = intPreferencesKey("port")
        val PASSWORD = stringPreferencesKey("password")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val VIBRATE = booleanPreferencesKey("vibrate")
        val COLUMNS = intPreferencesKey("columns")
        val OBS_PORT = intPreferencesKey("obs_port")
        val OBS_PASSWORD = stringPreferencesKey("obs_password")
    }
}
