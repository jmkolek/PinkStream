package com.example.pinkstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

enum class ChipStatus { CONNECTED, CONNECTING, OFFLINE }

/** The pill at the top of the deck: "● Connected · Streamer.bot". */
@Composable
fun ConnectionChip(status: ChipStatus, modifier: Modifier = Modifier) {
    val colors = DeckTheme.colors
    val label = when (status) {
        ChipStatus.CONNECTED -> "Connected"
        ChipStatus.CONNECTING -> "Connecting…"
        ChipStatus.OFFLINE -> "Offline"
    }
    Row(
        modifier
            .height(34.dp)
            .background(colors.surface, DeckShapes.pill)
            .border(1.dp, colors.hairline, DeckShapes.pill)
            .padding(start = 12.dp, end = 14.dp)
            // TalkBack reads one sentence instead of the dot and two texts, and
            // (live region) announces it again whenever the connection changes.
            .clearAndSetSemantics {
                contentDescription = "$label to Streamer.bot"
                liveRegion = LiveRegionMode.Polite
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (status == ChipStatus.CONNECTED) {
            HaloDot(color = colors.connected, size = 8.dp, halo = colors.connectedHalo)
        } else {
            HaloDot(color = colors.offline, size = 8.dp)
        }
        Text(label, style = DeckType.pillLarge, color = colors.ink)
        if (status == ChipStatus.CONNECTED) {
            Text(
                "· Streamer.bot",
                style = DeckType.pillLarge.copy(fontWeight = FontWeight.Medium),
                color = colors.inkSecondary,
            )
        }
    }
}
