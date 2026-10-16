package com.example.pinkstream.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType
import com.example.pinkstream.ui.theme.TileColor

/** One 60 dp row of a settings card: coloured icon tile, title, then [control] on the right. */
@Composable
fun SettingsRow(
    @DrawableRes icon: Int,
    iconColor: TileColor,
    title: String,
    modifier: Modifier = Modifier,
    control: @Composable () -> Unit,
) {
    val colors = DeckTheme.colors
    val palette = colors.tile(iconColor)
    Row(
        modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .background(palette.container, DeckShapes.settingsIconTile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = palette.icon, modifier = Modifier.size(18.dp))
        }
        Text(title, style = DeckType.rowTitle, color = colors.ink, modifier = Modifier.weight(1f))
        control()
    }
}

/**
 * A settings row with a switch. The whole row is the touch target (toggleable), and
 * the Switch itself only draws the state, so TalkBack reads one "switch" item.
 */
@Composable
fun SettingsSwitchRow(
    @DrawableRes icon: Int,
    iconColor: TileColor,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsRow(
        icon = icon,
        iconColor = iconColor,
        title = title,
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** The hairline between settings rows, inset to line up with the row titles. */
@Composable
fun SettingsDivider() {
    HorizontalDivider(Modifier.padding(start = 46.dp), thickness = 1.dp, color = DeckTheme.colors.hairline)
}
