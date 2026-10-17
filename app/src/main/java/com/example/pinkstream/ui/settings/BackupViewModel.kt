package com.example.pinkstream.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinkstream.StreamDeckApp
import com.example.pinkstream.data.deck.ActionCatalog
import com.example.pinkstream.data.deck.DeckLayout
import com.example.pinkstream.data.deck.DeckRepository
import com.example.pinkstream.data.deck.LayoutBackup
import com.example.pinkstream.data.deck.LayoutFiles
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate

/** Settings → Backup: save the layout to a file, or replace it with one. */
class BackupViewModel(
    private val deck: DeckRepository,
    private val files: LayoutFiles,
    private val actions: ActionCatalog,
) : ViewModel() {

    private val _pendingImport = MutableStateFlow<DeckLayout?>(null)
    /** A layout read from a file, waiting for the user to confirm the replace. */
    val pendingImport: StateFlow<DeckLayout?> = _pendingImport.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** Snackbar texts. */
    val messages: Flow<String> = _messages.receiveAsFlow()

    /** What the file picker suggests, e.g. "stream-deck-layout-2026-10-17.json". */
    fun exportFileName(): String = "stream-deck-layout-${LocalDate.now()}.json"

    fun export(uri: Uri) {
        viewModelScope.launch {
            val message = try {
                files.write(uri, LayoutBackup.encode(deck.layout.first()))
                "Layout saved"
            } catch (e: IOException) {
                "Couldn't save the file. Try another folder."
            } catch (e: SecurityException) {
                "Couldn't save the file. Try another folder."
            }
            _messages.send(message)
        }
    }

    fun onImportFilePicked(uri: Uri) {
        viewModelScope.launch {
            val text = try {
                files.read(uri)
            } catch (e: IOException) {
                null
            } catch (e: SecurityException) {
                null
            }
            val layout = text?.let(LayoutBackup::decode)
            if (layout == null) {
                _messages.send("That file isn't a Stream Deck layout.")
            } else {
                _pendingImport.value = layout
            }
        }
    }

    fun confirmImport() {
        val layout = _pendingImport.value ?: return
        _pendingImport.value = null
        viewModelScope.launch {
            deck.save(layout)
            // Fill in action ids by name, in case the file came from another PC.
            actions.actions.value?.let { deck.fillActionIds(it) }
            _messages.send("Layout imported")
        }
    }

    fun cancelImport() {
        _pendingImport.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as StreamDeckApp).container
                BackupViewModel(c.deck, c.layoutFiles, c.actions)
            }
        }
    }
}
