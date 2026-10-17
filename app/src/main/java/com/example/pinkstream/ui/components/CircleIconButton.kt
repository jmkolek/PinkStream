package com.example.pinkstream.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckTheme

/**
 * 44 dp round button with a hairline border (Back, Edit layout, Settings, Delete).
 * [tint] defaults to ink; Delete uses primary.
 * Compose widens the touch area of small clickable elements to 48 dp by itself.
 */
@Composable
fun CircleIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = DeckTheme.colors.ink,
) {
    val colors = DeckTheme.colors
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(1.dp, colors.hairline, CircleShape)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}
