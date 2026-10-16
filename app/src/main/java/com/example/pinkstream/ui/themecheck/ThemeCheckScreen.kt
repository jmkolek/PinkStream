package com.example.pinkstream.ui.themecheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.ChipStatus
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.components.ConnectionChip
import com.example.pinkstream.ui.components.DeckTile
import com.example.pinkstream.ui.components.MicHero
import com.example.pinkstream.ui.components.MicState
import com.example.pinkstream.ui.components.PageTabs
import com.example.pinkstream.ui.components.PrimaryButton
import com.example.pinkstream.ui.components.SectionLabel
import com.example.pinkstream.ui.components.TileGrid
import com.example.pinkstream.ui.components.TileLook
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType
import com.example.pinkstream.ui.theme.PinkStreamTheme
import com.example.pinkstream.ui.theme.ThemeMode
import com.example.pinkstream.ui.theme.TileColor

/**
 * TEMPORARY (milestone 0): shows every theme piece on one screen so the colours,
 * fonts and icons can be compared with the mockups. Deleted in milestone 3.
 */
@Composable
fun ThemeCheckScreen(
    onOpenDeck: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEdit: () -> Unit,
) {
    val colors = DeckTheme.colors
    // rememberSaveable keeps the selected tab across rotation and process death.
    var selectedPage by rememberSaveable { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            // Edge-to-edge: keep content clear of the status and navigation bars.
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
            ConnectionChip(ChipStatus.CONNECTED)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleIconButton(R.drawable.ic_deck_pencil, "Edit layout", onOpenEdit)
                CircleIconButton(R.drawable.ic_deck_sliders, "Settings", onOpenSettings)
            }
        }

        PageTabs(
            names = listOf("Main", "Model", "Extras"),
            selectedIndex = selectedPage,
            onSelect = { selectedPage = it },
            modifier = Modifier.padding(top = 14.dp),
        )

        Spacer(Modifier.height(16.dp))
        MicHero("Microphone", DeckIcon.MIC, MicState.LIVE)
        Spacer(Modifier.height(10.dp))
        MicHero("Microphone", DeckIcon.MIC, MicState.MUTED)
        Spacer(Modifier.height(10.dp))
        MicHero("Microphone", DeckIcon.MIC, MicState.UNKNOWN)

        SectionLabel("Tile colours")
        TileGrid(TileColor.entries, columns = 3) { color ->
            DeckTile(
                label = color.name.lowercase().replaceFirstChar { it.uppercase() },
                icon = sampleIcons.getValue(color),
                color = color,
                look = TileLook.NORMAL,
            )
        }

        SectionLabel("Tile states")
        TileGrid(sampleStates, columns = 3) { sample ->
            DeckTile(
                label = sample.label,
                icon = sample.icon,
                color = sample.color,
                look = sample.look,
                offline = sample.offline,
            )
        }

        SectionLabel("Connection")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ConnectionChip(ChipStatus.CONNECTING)
            ConnectionChip(ChipStatus.OFFLINE)
        }

        Spacer(Modifier.height(28.dp))
        PrimaryButton("Open the deck", onClick = onOpenDeck)

        Spacer(Modifier.height(28.dp))
        Text(
            "Hold any tile to edit it",
            style = DeckType.caption,
            color = colors.inkSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val sampleIcons = mapOf(
    TileColor.ROSE to DeckIcon.GAMEPAD,
    TileColor.PEACH to DeckIcon.SPARKLES,
    TileColor.LILAC to DeckIcon.SMILE,
    TileColor.MIST to DeckIcon.RESET,
    TileColor.BERRY to DeckIcon.STAR,
    TileColor.PLUM to DeckIcon.FLAG,
)

private data class SampleTile(
    val label: String,
    val icon: DeckIcon,
    val color: TileColor,
    val look: TileLook,
    val offline: Boolean = false,
)

private val sampleStates = listOf(
    SampleTile("Gaming", DeckIcon.GAMEPAD, TileColor.ROSE, TileLook.ON_AIR),
    SampleTile("Desktop", DeckIcon.VOLUME, TileColor.PEACH, TileLook.TOGGLE_ON),
    SampleTile("Music", DeckIcon.MUSIC, TileColor.PEACH, TileLook.TOGGLE_OFF),
    SampleTile("Just chatting", DeckIcon.CHAT, TileColor.ROSE, TileLook.NORMAL),
    SampleTile("Ad break", DeckIcon.PLAY, TileColor.MIST, TileLook.NO_ACTION),
    SampleTile("Offline", DeckIcon.COFFEE, TileColor.ROSE, TileLook.NORMAL, offline = true),
)

@Preview(name = "Day", heightDp = 1400)
@Composable
private fun ThemeCheckDayPreview() {
    PinkStreamTheme(ThemeMode.DAY) { ThemeCheckScreen({}, {}, {}) }
}

@Preview(name = "Night", heightDp = 1400)
@Composable
private fun ThemeCheckNightPreview() {
    PinkStreamTheme(ThemeMode.NIGHT) { ThemeCheckScreen({}, {}, {}) }
}
