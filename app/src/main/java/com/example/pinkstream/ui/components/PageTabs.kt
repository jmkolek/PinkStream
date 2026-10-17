package com.example.pinkstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * Serif page names; the active one is italic with a rose dot under it.
 * [onLongPress] (edit layout mode) renames a page; [trailing] sits after the last tab.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageTabs(
    names: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: ((Int) -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = DeckTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.Top) {
        names.forEachIndexed { index, name ->
            val active = index == selectedIndex
            Column(
                Modifier
                    .semantics { selected = active }
                    .combinedClickable(
                        // No ripple: the italic and the dot already show the change.
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onLongClickLabel = if (onLongPress != null) "Rename" else null,
                        onLongClick = onLongPress?.let { { it(index) } },
                    ) { onSelect(index) },
            ) {
                Text(
                    name,
                    style = DeckType.pageTab.copy(fontStyle = if (active) FontStyle.Italic else FontStyle.Normal),
                    color = if (active) colors.ink else colors.inactiveTab,
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    Modifier
                        .padding(start = 3.dp)
                        .size(6.dp)
                        .then(if (active) Modifier.background(colors.primary, CircleShape) else Modifier),
                )
            }
        }
        trailing?.invoke()
    }
}
