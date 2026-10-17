package com.example.pinkstream.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pinkstream.R
import com.example.pinkstream.data.deck.DeckLayout
import com.example.pinkstream.ui.components.SettingsDivider
import com.example.pinkstream.ui.components.SettingsRow
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.TileColor

/**
 * Settings → Backup: Export layout and Import layout rows. Not in the mockups; it
 * copies the Deck card's rows, with a chevron like the Edit screen's action row.
 */
@Composable
fun BackupCard(viewModel: BackupViewModel) {
    val colors = DeckTheme.colors
    val pending by viewModel.pendingImport.collectAsStateWithLifecycle()

    // The system file pickers. Each returns a content:// Uri, or null if the user backed out.
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(viewModel::export)
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::onImportFilePicked)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, DeckShapes.settingsCard)
            .border(1.dp, colors.hairline, DeckShapes.settingsCard)
            .padding(horizontal = 16.dp),
    ) {
        SettingsRow(
            icon = R.drawable.ic_deck_upload,
            iconColor = TileColor.LILAC,
            title = "Export layout",
            modifier = Modifier.clickable(role = Role.Button) { exportPicker.launch(viewModel.exportFileName()) },
        ) { Chevron() }
        SettingsDivider()
        SettingsRow(
            icon = R.drawable.ic_deck_download,
            iconColor = TileColor.PEACH,
            title = "Import layout",
            // Some file managers label .json files as plain text or "any file".
            modifier = Modifier.clickable(role = Role.Button) {
                importPicker.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
            },
        ) { Chevron() }
    }

    pending?.let { layout ->
        ConfirmImportDialog(layout, onImport = viewModel::confirmImport, onCancel = viewModel::cancelImport)
    }
}

@Composable
private fun Chevron() {
    Icon(
        painterResource(R.drawable.ic_deck_chevron_right),
        contentDescription = null,
        tint = DeckTheme.colors.inkSecondary,
        modifier = Modifier.size(20.dp),
    )
}

@Composable
private fun ConfirmImportDialog(layout: DeckLayout, onImport: () -> Unit, onCancel: () -> Unit) {
    val pages = layout.pages.size
    val buttons = layout.pages.sumOf { page -> page.sections.sumOf { it.buttons.size } }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Replace your layout?") },
        text = {
            Text(
                "The file has $pages ${if (pages == 1) "page" else "pages"} and $buttons " +
                    "${if (buttons == 1) "button" else "buttons"}. They replace everything on the deck now.",
            )
        },
        confirmButton = { Button(onClick = onImport) { Text("Replace") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
    )
}
