package com.example.pinkstream.ui.deck

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.pinkstream.ui.theme.DeckTheme

/** "Delete Model?" — deleting a page can't be undone, so it asks first. */
@Composable
fun DeletePageDialog(name: String, keyCount: Int, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val colors = DeckTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $name?") },
        text = {
            Text(
                when (keyCount) {
                    0 -> "The page is empty."
                    1 -> "Its key is deleted too."
                    else -> "Its $keyCount keys are deleted too."
                },
            )
        },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.surface),
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
