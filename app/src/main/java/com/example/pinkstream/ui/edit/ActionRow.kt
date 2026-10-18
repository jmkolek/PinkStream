package com.example.pinkstream.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** The key's action, as a row that opens the picker. */
@Composable
fun ActionRow(draft: KeyDraft, actions: List<ActionInfo>?, onClick: () -> Unit) {
    val colors = DeckTheme.colors
    val action = actions?.firstOrNull { it.id == draft.actionId } ?: actions?.firstOrNull { it.name == draft.actionName }
    val title = draft.actionName ?: action?.name ?: "Choose an action"
    val subtitle = when {
        draft.actionName == null && draft.actionId == null -> "Pick what this key runs"
        actions == null -> "Streamer.bot"                          // not connected yet: can't check
        action == null -> "Not in Streamer.bot anymore. Pick another one."
        action.group != null -> "Streamer.bot · ${action.group} group"
        else -> "Streamer.bot"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(DeckShapes.actionRow)
            .background(colors.surface)
            .border(1.dp, colors.hairline, DeckShapes.actionRow)
            .clickable(role = Role.Button, onClickLabel = "Choose an action", onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).background(colors.primaryContainer, DeckShapes.pickerIconBox), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_deck_bolt), contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = DeckType.rowTitle, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = DeckType.rowSubtitle,
                color = if (action == null && actions != null && draft.actionName != null) colors.error else colors.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(painterResource(R.drawable.ic_deck_chevron_right), contentDescription = null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
    }
}
