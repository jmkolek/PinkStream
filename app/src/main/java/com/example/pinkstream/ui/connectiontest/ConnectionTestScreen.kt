package com.example.pinkstream.ui.connectiontest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinkstream.R
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.ui.components.ChipStatus
import com.example.pinkstream.ui.components.ConnectionChip
import com.example.pinkstream.ui.components.DeckSnackbarHost
import com.example.pinkstream.ui.components.DeckTextField
import com.example.pinkstream.ui.components.PrimaryButton
import com.example.pinkstream.ui.components.SectionLabel
import com.example.pinkstream.ui.components.TonalButton
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * TEMPORARY (milestone 1): connect to Streamer.bot and run "Deck - Test".
 * Deleted in milestone 3 along with the theme check screen.
 */
@Composable
fun ConnectionTestScreen(
    onOpenThemeCheck: () -> Unit,
    viewModel: ConnectionTestViewModel = viewModel(factory = ConnectionTestViewModel.Factory),
) {
    val colors = DeckTheme.colors
    // collectAsStateWithLifecycle stops collecting while the screen isn't visible.
    val form by viewModel.form.collectAsStateWithLifecycle()
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { DeckSnackbarHost(snackbarHostState) },
    ) { insets ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(insets)
                // Scaffold already padded for the system bars; only add the keyboard on top.
                .consumeWindowInsets(insets)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Text("Connection test", style = DeckType.screenTitle, color = colors.ink)
            Spacer(Modifier.height(14.dp))
            ConnectionChip(connection.chipStatus())
            Spacer(Modifier.height(10.dp))
            Text(connection.describe(), style = DeckType.rowSubtitle, color = colors.inkSecondary)

            SectionLabel("Connection")
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface, DeckShapes.settingsCard)
                    .border(1.dp, colors.hairline, DeckShapes.settingsCard)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DeckTextField(
                    value = form.host,
                    onValueChange = viewModel::onHostChange,
                    label = "PC address",
                    helper = "127.0.0.1 over USB, 10.0.2.2 on the emulator",
                    error = form.hostError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                )
                DeckTextField(
                    value = form.port,
                    onValueChange = viewModel::onPortChange,
                    label = "Port",
                    error = form.portError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                )
                DeckTextField(
                    value = form.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    placeholder = "Optional",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    visualTransformation = PasswordVisualTransformation(),
                )
            }

            Spacer(Modifier.height(24.dp))
            when (connection) {
                ConnectionState.Disconnected -> PrimaryButton("Connect", onClick = viewModel::connect)
                // Failed keeps retrying in the background; offer both "now" and "stop".
                is ConnectionState.Failed -> {
                    PrimaryButton("Try again", onClick = viewModel::connect)
                    Text(
                        "Stop trying",
                        style = DeckType.buttonSmall,
                        color = colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = viewModel::disconnect)
                            .padding(vertical = 14.dp),
                    )
                }
                else -> PrimaryButton("Disconnect", onClick = viewModel::disconnect)
            }
            Spacer(Modifier.height(12.dp))
            TonalButton(
                text = "Run ${ConnectionTestViewModel.TEST_ACTION}",
                onClick = viewModel::runTestAction,
                icon = R.drawable.ic_deck_bolt,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(28.dp))
            Text(
                "Open theme check",
                style = DeckType.caption,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenThemeCheck)
                    .padding(vertical = 14.dp),
            )
        }
    }
}

private fun ConnectionState.chipStatus(): ChipStatus = when (this) {
    is ConnectionState.Connected -> ChipStatus.CONNECTED
    ConnectionState.Connecting, ConnectionState.Authenticating -> ChipStatus.CONNECTING
    ConnectionState.Disconnected, is ConnectionState.Failed -> ChipStatus.OFFLINE
}

private fun ConnectionState.describe(): String = when (this) {
    ConnectionState.Disconnected -> "Not connected. Tap Connect."
    ConnectionState.Connecting -> "Connecting…"
    ConnectionState.Authenticating -> "Signing in…"
    is ConnectionState.Connected ->
        listOfNotNull("Connected to Streamer.bot", info?.version).joinToString(" ")
    is ConnectionState.Failed -> reason
}
