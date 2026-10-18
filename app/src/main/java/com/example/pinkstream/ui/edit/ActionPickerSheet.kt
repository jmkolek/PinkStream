package com.example.pinkstream.ui.edit

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinkstream.R
import com.example.pinkstream.data.deck.DeckLayout
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.components.DeckBottomSheet
import com.example.pinkstream.ui.components.DeckChip
import com.example.pinkstream.ui.components.PrimaryButton
import com.example.pinkstream.ui.components.SearchField
import com.example.pinkstream.ui.components.SectionLabel
import com.example.pinkstream.ui.components.TonalButton
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** Everything the "Choose an action" sheet shows. */
class PickerData(
    /** null = never loaded (not connected yet). */
    val actions: List<ActionInfo>?,
    val refreshing: Boolean,
    val connected: Boolean,
    val layout: DeckLayout?,
    /** The key being edited (null while it's new or not made yet). */
    val keyId: String?,
    /** The action the key's form has now, if any. */
    val currentActionId: String?,
    val currentActionName: String?,
)

/**
 * "Choose an action" (mockup 04-choose-action). Search, group chips, one choice, "Use this action".
 * [onOpenSettings] adds a Settings button when the list couldn't be loaded (not connected yet).
 */
@Composable
fun ActionPickerSheet(
    data: PickerData,
    onRefresh: () -> Unit,
    onUse: (ActionInfo) -> Unit,
    onDismiss: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
) {
    val colors = DeckTheme.colors
    val actions = data.actions.orEmpty()
    // The form's action counts as "this key's", matched by id or (with no id yet) by name.
    fun isDraftAction(action: ActionInfo) =
        if (data.currentActionId != null) data.currentActionId == action.id else data.currentActionName == action.name

    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf(defaultGroup(actions)) }
    var selectedId by rememberSaveable { mutableStateOf(actions.firstOrNull(::isDraftAction)?.id) }
    val matches = filterActions(actions, query, group)

    DeckBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Choose an action", style = DeckType.sheetTitle, color = colors.ink)
                    Text(pickerSubtitle(data), style = DeckType.sheetSubtitle, color = colors.inkSecondary)
                }
                RefreshButton(spinning = data.refreshing, onClick = onRefresh)
            }

            Spacer(Modifier.height(16.dp))
            SearchField(value = query, onValueChange = { query = it }, placeholder = "Search actions")

            Spacer(Modifier.height(10.dp))
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (actions.any { it.group == STREAM_DECK_GROUP }) {
                    DeckChip(STREAM_DECK_GROUP, selected = group == STREAM_DECK_GROUP, onClick = { group = STREAM_DECK_GROUP })
                }
                DeckChip("All groups", selected = group == null, onClick = { group = null })
            }

            SectionLabel(
                when {
                    query.isBlank() -> "${matches.size} ${if (matches.size == 1) "action" else "actions"}"
                    else -> "${matches.size} ${if (matches.size == 1) "match" else "matches"}"
                },
                top = 16.dp,
                bottom = 8.dp,
            )

            // The list takes the space it needs, up to what's left above the button.
            Column(Modifier.weight(1f)) {
                if (matches.isNotEmpty()) {
                    LazyColumn(
                        Modifier
                            .weight(1f, fill = false)
                            .clip(DeckShapes.listCard)
                            .background(colors.surface)
                            .border(1.dp, colors.hairline, DeckShapes.listCard)
                            .padding(6.dp)
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(matches, key = { it.id }) { action ->
                            PickerRow(
                                action = action,
                                query = query,
                                selected = action.id == selectedId,
                                usage = data.layout?.let { actionUsage(it, action, isDraftAction(action), data.keyId) } ?: "",
                                onClick = { selectedId = action.id },
                            )
                        }
                    }
                } else {
                    Text(
                        when {
                            data.actions == null -> "Connect to Streamer.bot to see your actions."
                            query.isNotBlank() -> "No actions match “${query.trim()}”."
                            else -> "No actions in this group."
                        },
                        style = DeckType.rowTitle,
                        color = colors.ink,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                    if (data.actions == null && onOpenSettings != null) {
                        TonalButton(
                            text = "Settings",
                            icon = R.drawable.ic_deck_sliders,
                            onClick = onOpenSettings,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                Text(
                    "Missing one? Create it in Streamer.bot, then tap refresh.",
                    style = DeckType.caption,
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(start = 4.dp, top = 14.dp, end = 4.dp),
                )
            }

            Spacer(Modifier.height(12.dp))
            val chosen = actions.firstOrNull { it.id == selectedId }
            PrimaryButton("Use this action", onClick = { chosen?.let(onUse) }, enabled = chosen != null)
        }
    }
}

private fun pickerSubtitle(data: PickerData): String {
    val actions = data.actions
    return when {
        data.refreshing -> "From Streamer.bot · Refreshing…"
        actions == null -> if (data.connected) "From Streamer.bot" else "Not connected to your PC"
        else -> "From Streamer.bot · ${actions.size} ${if (actions.size == 1) "action" else "actions"}" +
            if (data.connected) "" else " · offline"
    }
}

/** The refresh button; its icon turns while GetActions is running. */
@Composable
private fun RefreshButton(spinning: Boolean, onClick: () -> Unit) {
    val rotation = if (spinning) {
        val transition = rememberInfiniteTransition(label = "refresh")
        transition.animateFloat(0f, 360f, infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart), label = "spin").value
    } else {
        0f
    }
    // The whole round button turns; being a circle, only the arrow looks like it moves.
    CircleIconButton(
        R.drawable.ic_deck_refresh,
        contentDescription = "Refresh actions",
        onClick = onClick,
        modifier = Modifier.graphicsLayer { rotationZ = rotation },
    )
}

@Composable
private fun PickerRow(action: ActionInfo, query: String, selected: Boolean, usage: String, onClick: () -> Unit) {
    val colors = DeckTheme.colors
    val style = guessActionStyle(action.name)
    val palette = colors.iconBox(style.tint)
    val second = if (action.enabled) usage else "$usage · off in Streamer.bot"
    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(DeckShapes.pickerRow)
            .background(if (selected) colors.primaryContainer else Color.Transparent)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(start = 10.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).background(if (selected) colors.surface else palette.container, DeckShapes.pickerIconBox),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(style.icon.drawable), contentDescription = null, tint = palette.icon, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                highlighted(action.name, query, if (selected) colors.highlightSelected else colors.highlight),
                style = DeckType.rowTitle.copy(fontSize = 14.5.sp),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(second, style = DeckType.rowSubtitle, color = colors.inkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Radio(selected)
    }
}

/** The search match painted with a soft pink background. */
private fun highlighted(text: String, query: String, background: Color): AnnotatedString = buildAnnotatedString {
    append(text)
    matchRanges(text, query).forEach { addStyle(SpanStyle(background = background), it.first, it.last + 1) }
}

/** 22 dp radio: a rose disc with a check when chosen, an outline ring when not. */
@Composable
private fun Radio(selected: Boolean) {
    val colors = DeckTheme.colors
    if (selected) {
        Box(Modifier.size(22.dp).background(colors.primary, CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_deck_check), contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(13.dp))
        }
    } else {
        Box(Modifier.size(22.dp).border(2.dp, colors.outline, CircleShape))
    }
}
