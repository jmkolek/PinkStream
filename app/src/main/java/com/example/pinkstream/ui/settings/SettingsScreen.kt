package com.example.pinkstream.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinkstream.R
import com.example.pinkstream.data.settings.AppSettings
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.components.DeckSnackbarHost
import com.example.pinkstream.ui.components.SectionLabel
import com.example.pinkstream.ui.components.SegmentedControl
import com.example.pinkstream.ui.components.SettingsDivider
import com.example.pinkstream.ui.components.SettingsRow
import com.example.pinkstream.ui.components.SettingsSwitchRow
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType
import com.example.pinkstream.ui.theme.IconTint

/** Settings (mockup 05-settings): the connections to Streamer.bot and OBS, and how the deck behaves. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    backupViewModel: BackupViewModel = viewModel(factory = BackupViewModel.Factory),
) {
    val colors = DeckTheme.colors
    val form by viewModel.form.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val obsStatus by viewModel.obsStatus.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(backupViewModel) {
        backupViewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        SettingsContent(form, settings, status, obsStatus, viewModel, backupViewModel, onBack)
        DeckSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}

@Composable
private fun SettingsContent(
    form: ConnectionForm?,
    settings: AppSettings?,
    status: ConnectionStatus,
    obsStatus: ConnectionStatus,
    viewModel: SettingsViewModel,
    backupViewModel: BackupViewModel,
    onBack: () -> Unit,
) {
    val colors = DeckTheme.colors

    // The Box around this pads for safeDrawing = status bar, navigation bar and the
    // keyboard while it's open, so the focused field can scroll above the keyboard.
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 24.dp),
    ) {
        CircleIconButton(icon = R.drawable.ic_deck_chevron_left, contentDescription = "Back", onClick = onBack)
        Spacer(Modifier.height(12.dp))
        Text("Settings", style = DeckType.screenTitle, color = colors.ink)

        // Both load from DataStore in a few milliseconds; show the cards once they have.
        val loadedForm = form
        val loadedSettings = settings
        if (loadedForm != null && loadedSettings != null) {
            SectionLabel("Connection", top = 20.dp, bottom = 10.dp)
            ConnectionCard(form = loadedForm, status = status, obsStatus = obsStatus, viewModel = viewModel)

            SectionLabel("Deck", top = 22.dp, bottom = 10.dp)
            DeckCard(loadedSettings, viewModel)

            SectionLabel("Backup", top = 22.dp, bottom = 10.dp)
            BackupCard(backupViewModel)
        }
    }
}

@Composable
private fun DeckCard(settings: AppSettings, viewModel: SettingsViewModel) {
    val colors = DeckTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, DeckShapes.settingsCard)
            .border(1.dp, colors.hairline, DeckShapes.settingsCard)
            .padding(horizontal = 16.dp),
    ) {
        SettingsSwitchRow(
            icon = R.drawable.ic_deck_sun,
            iconColor = IconTint.PEACH,
            title = "Keep screen on",
            checked = settings.keepScreenOn,
            onCheckedChange = viewModel::setKeepScreenOn,
        )
        SettingsDivider()
        SettingsSwitchRow(
            icon = R.drawable.ic_deck_vibrate,
            iconColor = IconTint.LILAC,
            title = "Vibrate on press",
            checked = settings.vibrate,
            onCheckedChange = viewModel::setVibrate,
        )
        SettingsDivider()
        SettingsRow(icon = R.drawable.ic_deck_grid, iconColor = IconTint.MIST, title = "Columns") {
            SegmentedControl(
                options = AppSettings.COLUMN_CHOICES,
                selected = settings.columns,
                onSelect = viewModel::setColumns,
                label = { it.toString() },
                optionPadding = 12.dp,
                optionMinWidth = 44.dp,
            )
        }
    }
}
