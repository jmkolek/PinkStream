package com.example.pinkstream.ui.deck

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.components.DeckTextField
import com.example.pinkstream.ui.theme.DeckTheme

/**
 * Rename a page (long-press its tab in edit layout mode). A just-added page starts
 * with an empty field; saving it empty keeps the current name.
 * [onDelete] is null for the last page, which can't be deleted.
 */
@Composable
fun RenamePageDialog(
    currentName: String,
    startEmpty: Boolean,
    onSave: (String) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(if (startEmpty) "" else currentName) }
    val focusRequester = remember { FocusRequester() }
    // Open with the keyboard up, ready to type.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename page") },
        text = {
            Column {
                DeckTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Page name",
                    placeholder = currentName,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSave(name) }),
                    modifier = Modifier.focusRequester(focusRequester),
                )
                if (onDelete != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onDelete) { Text("Delete this page", color = DeckTheme.colors.error) }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(name) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** "Delete Model?" — deleting a page can't be undone, so it asks first. */
@Composable
fun DeletePageDialog(name: String, buttonCount: Int, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val colors = DeckTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $name?") },
        text = {
            Text(
                when (buttonCount) {
                    0 -> "The page is empty."
                    1 -> "Its button is deleted too."
                    else -> "Its $buttonCount buttons are deleted too."
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
