package com.example.pinkstream.ui.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.EmptySlotImage
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * The 24 dp ink circle with a minus at a key's top-right corner in edit layout mode
 * (mockup 02: its centre sits 8 dp in from the key's corner). The circle sits in the
 * middle of a 48 dp touch area, drawn on top of the key. Put it in a Box with the key
 * and align it TopEnd.
 */
@Composable
fun RemoveBadge(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .offset(x = 16.dp, y = (-16).dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = "Remove $label", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        InkBadge {
            Icon(painterResource(R.drawable.ic_deck_minus), contentDescription = "Remove $label", tint = DeckTheme.colors.surface, modifier = Modifier.size(14.dp))
        }
    }
}

/**
 * The "!" in the same corner when a key's action isn't in Streamer.bot anymore.
 * Keys have no text, so this replaces the old "No action" label. It's only a sign:
 * tapping the key opens the editor, and TalkBack hears "no action" from the key.
 */
@Composable
fun MissingActionBadge(modifier: Modifier = Modifier) {
    Box(modifier.offset(x = 4.dp, y = (-4).dp).clearAndSetSemantics {}) {
        InkBadge {
            Text("!", style = DeckType.buttonSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = DeckTheme.colors.surface)
        }
    }
}

/** 24 dp ink circle with a 2 dp ring in the background colour, so it stands off the key. */
@Composable
private fun InkBadge(content: @Composable () -> Unit) {
    val colors = DeckTheme.colors
    Box(
        Modifier
            .size(24.dp)
            .background(colors.ink, CircleShape)
            .border(2.dp, colors.background, CircleShape),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** A dashed "+" slot after a page's keys in edit layout mode. Tapping it adds a key. */
@Composable
fun EmptySlot(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(DeckShapes.dockIcon)
            .clickable(role = Role.Button, onClickLabel = "Add a key", onClick = onClick)
            .semantics { contentDescription = "Empty slot" },
    ) {
        EmptySlotImage(Modifier.fillMaxSize())
    }
}

/** Replaces the pencil while editing the layout. */
@Composable
fun DonePill(onClick: () -> Unit) {
    val colors = DeckTheme.colors
    Box(
        Modifier
            .height(44.dp)
            .clip(DeckShapes.pill)
            .background(colors.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Done", style = DeckType.buttonSmall, color = colors.onPrimary)
    }
}
