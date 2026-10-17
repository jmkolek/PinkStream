package com.example.pinkstream.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.components.DeckTextField
import com.example.pinkstream.ui.components.FieldStyle
import com.example.pinkstream.ui.components.PrimaryButton
import com.example.pinkstream.ui.components.SectionLabel
import com.example.pinkstream.ui.components.SwitchCardRow
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** Edit button (mockup 03-edit-button), also used for new buttons from "+ Add". */
@Composable
fun EditButtonScreen(
    onClose: () -> Unit,
    viewModel: EditButtonViewModel = viewModel(factory = EditButtonViewModel.Factory),
) {
    val colors = DeckTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.closed.collect { onClose() } }

    var showActionPicker by rememberSaveable { mutableStateOf(false) }
    var showIconSheet by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            // Status bar, navigation bar and the keyboard: Save stays above the keyboard.
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val loaded = state ?: return@Column   // loads in a few milliseconds
        val draft = loaded.draft

        Header(
            title = if (loaded.isNew) "New button" else "Edit button",
            onBack = onClose,
            onDelete = if (loaded.isNew) null else { { confirmDelete = true } },
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(14.dp))
            ButtonPreview(draft, loaded.isHero)

            SectionLabel("Label", top = 14.dp, bottom = 8.dp)
            DeckTextField(
                value = draft.label,
                onValueChange = viewModel::onLabelChange,
                label = null,
                style = FieldStyle.EDIT,
                placeholder = "What the tile says",
                error = loaded.errors.label,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            )

            SectionLabel("Action", top = 14.dp, bottom = 8.dp)
            ActionRow(draft, actions, onClick = { showActionPicker = true })

            SectionLabel("Icon", top = 14.dp, bottom = 10.dp)
            IconRow(loaded.iconRow, selected = draft.icon, onSelect = viewModel::onIconChange, onMore = { showIconSheet = true })

            SectionLabel("Colour", top = 14.dp, bottom = 10.dp)
            ColorSwatches(selected = draft.color, onSelect = viewModel::onColorChange)

            Spacer(Modifier.height(14.dp))
            OptionsCard(loaded, viewModel)
            Spacer(Modifier.height(16.dp))
        }

        PrimaryButton(
            text = if (loaded.isNew) "Add button" else "Save changes",
            onClick = viewModel::save,
            modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 24.dp),
        )

        if (showActionPicker) {
            val layout by viewModel.layout.collectAsStateWithLifecycle()
            val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
            val connected by viewModel.connected.collectAsStateWithLifecycle()
            ActionPickerSheet(
                data = PickerData(actions, refreshing, connected, layout, buttonId = if (loaded.isNew) null else viewModel.editedButtonId, draft = draft),
                onRefresh = viewModel::refreshActions,
                onUse = { action ->
                    viewModel.onActionChosen(action)
                    showActionPicker = false
                },
                onDismiss = { showActionPicker = false },
            )
        }
        if (showIconSheet) {
            MoreIconsSheet(
                selected = draft.icon,
                onSelect = { icon ->
                    viewModel.onIconChange(icon)
                    showIconSheet = false
                },
                onDismiss = { showIconSheet = false },
            )
        }
        if (confirmDelete) {
            DeleteButtonDialog(
                label = draft.label.ifBlank { "this button" },
                onDelete = {
                    confirmDelete = false
                    viewModel.delete()
                },
                onDismiss = { confirmDelete = false },
            )
        }
    }
}

/** Back, the title, and Delete (hidden for a button that isn't saved yet). */
@Composable
private fun Header(title: String, onBack: () -> Unit, onDelete: (() -> Unit)?) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 22.dp, end = 20.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(R.drawable.ic_deck_chevron_left, "Back", onBack)
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(title, style = DeckType.button, color = DeckTheme.colors.ink, modifier = Modifier.semantics { heading() })
        }
        if (onDelete != null) {
            CircleIconButton(R.drawable.ic_deck_trash, "Delete button", onDelete, tint = DeckTheme.colors.primary)
        } else {
            Spacer(Modifier.size(44.dp))
        }
    }
}

/** The switches, each with its OBS name field right under it while it's on. */
@Composable
private fun OptionsCard(state: EditButtonUiState, viewModel: EditButtonViewModel) {
    val colors = DeckTheme.colors
    val draft = state.draft
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, DeckShapes.listCard)
            .border(1.dp, colors.hairline, DeckShapes.listCard)
            .padding(horizontal = 16.dp),
    ) {
        // The mic hero has no on-air look, so it doesn't get this switch.
        if (!state.isHero) {
            SwitchCardRow(
                title = "Light up when on air",
                subtitle = "Fills in while this scene is live",
                checked = draft.onAir,
                onCheckedChange = viewModel::onOnAirChange,
            )
            if (draft.onAir) {
                DeckTextField(
                    value = draft.onAirScene,
                    onValueChange = viewModel::onSceneChange,
                    label = "OBS scene name",
                    helper = "Exactly as it's written in OBS",
                    error = state.errors.onAirScene,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            HorizontalDivider(thickness = 1.dp, color = colors.hairline)
        }

        SwitchCardRow(
            title = if (state.isHero) "Show live or muted" else "Show on/off state",
            subtitle = if (state.isHero) "Follows an OBS audio source's mute" else "Follows an OBS source's mute",
            checked = draft.showState,
            onCheckedChange = viewModel::onShowStateChange,
        )
        if (draft.showState) {
            DeckTextField(
                value = draft.stateInput,
                onValueChange = viewModel::onInputChange,
                label = "OBS input name",
                placeholder = if (state.isHero) "Mic/Aux" else "Desktop Audio",
                helper = "The source's name in the OBS audio mixer",
                error = state.errors.stateInput,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.hairline)

        SwitchCardRow(
            title = "Ask before running",
            subtitle = "Adds a confirm step for risky buttons",
            checked = draft.confirm,
            onCheckedChange = viewModel::onConfirmChange,
        )
    }
}

@Composable
private fun DeleteButtonDialog(label: String, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val colors = DeckTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $label?") },
        text = { Text("The button is removed from the deck. The action stays in Streamer.bot.") },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.surface),
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
