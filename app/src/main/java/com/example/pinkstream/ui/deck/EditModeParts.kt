package com.example.pinkstream.ui.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.dashedBorder
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** The 22 dp ink circle with a minus at a tile's top-right corner in edit layout mode. */
@Composable
fun RemoveBadge(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DeckTheme.colors
    Box(
        modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(colors.ink)
            .clickable(role = Role.Button, onClickLabel = "Remove $label", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // surface = white by day, dark plum at night: always the opposite of ink.
        Icon(painterResource(R.drawable.ic_deck_minus), contentDescription = "Remove $label", tint = colors.surface, modifier = Modifier.size(14.dp))
    }
}

/** The dashed "+ Add" tile at the end of a section in edit layout mode. */
@Composable
fun AddTile(onClick: () -> Unit, modifier: Modifier = Modifier, height: Dp = 94.dp, cornerRadius: Dp = 22.dp) {
    val colors = DeckTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .dashedBorder(colors.inkSecondary.copy(alpha = 0.4f), cornerRadius)
            .clickable(role = Role.Button, onClickLabel = "Add a button", onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_deck_plus), contentDescription = null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
        Text("Add", style = DeckType.tileLabel, color = colors.inkSecondary)
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
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Done", style = DeckType.buttonSmall, color = colors.onPrimary)
    }
}
