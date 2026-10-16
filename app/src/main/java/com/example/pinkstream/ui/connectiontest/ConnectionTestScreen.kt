package com.example.pinkstream.ui.connectiontest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinkstream.R
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.ui.components.ChipStatus
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.components.ConnectionChip
import com.example.pinkstream.ui.components.DeckSnackbarHost
import com.example.pinkstream.ui.components.TonalButton
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * TEMPORARY (milestones 1–2): shows the connection and runs "Deck - Test".
 * The address lives in Settings. Deleted in milestone 3 along with the theme check screen.
 */
@Composable
fun ConnectionTestScreen(
    onOpenSettings: () -> Unit,
    onOpenThemeCheck: () -> Unit,
    viewModel: ConnectionTestViewModel = viewModel(factory = ConnectionTestViewModel.Factory),
) {
    val colors = DeckTheme.colors
    // collectAsStateWithLifecycle stops collecting while the screen isn't visible.
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
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ConnectionChip(connection.chipStatus())
                CircleIconButton(icon = R.drawable.ic_deck_sliders, contentDescription = "Settings", onClick = onOpenSettings)
            }
            Spacer(Modifier.height(14.dp))
            Text("Connection test", style = DeckType.screenTitle, color = colors.ink)
            Spacer(Modifier.height(10.dp))
            Text(connection.describe(), style = DeckType.rowSubtitle, color = colors.inkSecondary)

            Spacer(Modifier.height(24.dp))
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
    ConnectionState.Disconnected -> "Not connected. Check the address in Settings."
    ConnectionState.Connecting -> "Connecting…"
    ConnectionState.Authenticating -> "Signing in…"
    is ConnectionState.Connected ->
        listOfNotNull("Connected to Streamer.bot", info?.version).joinToString(" ")
    is ConnectionState.Failed -> reason
}
