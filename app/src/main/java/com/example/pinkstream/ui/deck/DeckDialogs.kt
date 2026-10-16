package com.example.pinkstream.ui.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.pinkstream.data.deck.DeckButton
import com.example.pinkstream.ui.theme.DeckType

/** "Run Ad break?" for buttons with "Ask before running" on (DESIGN.md → Confirm dialog). */
@Composable
fun ConfirmRunDialog(button: DeckButton, onRun: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Run ${button.label}?") },
        text = { Text("This button asks before it runs.") },
        confirmButton = { Button(onClick = onRun) { Text("Run") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
    )
}

/** Debug builds only: pick fake OBS data to check the on-air, Live/Muted and On/Off looks. */
@Composable
fun LivePreviewDialog(selected: LivePreview, onSelect: (LivePreview) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Preview live states") },
        text = {
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Debug builds only. Shows fake OBS states on the deck.", style = DeckType.caption)
                LivePreview.entries.forEach { preview ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(selected = preview == selected, role = Role.RadioButton) { onSelect(preview) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = preview == selected, onClick = null)
                        Text(preview.label, style = DeckType.rowSubtitle, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}
