package com.example.pinkstream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * The page tabs in a white card at the bottom of the deck (mockups 01 and 02).
 * [onLongPress] (edit layout mode) opens a page's settings; [onAddPage] adds the
 * dashed "Add page" slot after the tabs.
 */
@Composable
fun PageDock(
    pages: List<DeckPage>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: ((Int) -> Unit)? = null,
    onAddPage: (() -> Unit)? = null,
) {
    val colors = DeckTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(66.dp)
            .softShadow(colors.raisedShadow, 12.dp, DeckShapes.dock)
            .background(colors.surface, DeckShapes.dock)
            .border(1.dp, colors.hairline, DeckShapes.dock)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        pages.forEachIndexed { index, page ->
            DockTab(
                page = page,
                position = "page ${index + 1} of ${pages.size}",
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                onLongClick = onLongPress?.let { { it(index) } },
                modifier = Modifier.weight(1f),
            )
        }
        if (onAddPage != null) AddPageSlot(onAddPage, Modifier.weight(1f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockTab(
    page: DeckPage,
    position: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier,
) {
    val colors = DeckTheme.colors
    val pillColor by animateColorAsState(if (selected) colors.primaryContainer else Color.Transparent, label = "tab")
    Column(
        modifier
            .fillMaxHeight()
            .clip(DeckShapes.dockIcon)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            // TalkBack: "Main, page 1 of 3, tab, selected"; the long press is offered as an action.
            .clearAndSetSemantics {
                contentDescription = "${page.name}, $position"
                role = Role.Tab
                this.selected = selected
                onClick(action = { onClick(); true })
                if (onLongClick != null) onLongClick(label = "Edit page", action = { onLongClick(); true })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Box(
            Modifier
                .width(56.dp)
                .height(30.dp)
                .background(pillColor, DeckShapes.dockIcon),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(page.icon.drawable),
                contentDescription = null,
                tint = if (selected) colors.primary else colors.inkSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            page.name,
            style = if (selected) DeckType.dockLabel.copy(fontWeight = FontWeight.SemiBold) else DeckType.dockLabel,
            color = if (selected) colors.ink else colors.inkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}

@Composable
private fun AddPageSlot(onClick: () -> Unit, modifier: Modifier) {
    val colors = DeckTheme.colors
    Column(
        modifier
            .fillMaxHeight()
            .clip(DeckShapes.dockIcon)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .dashedBorder(colors.primary.copy(alpha = 0.55f), 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_deck_plus), contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
        }
        Text("Add page", style = DeckType.dockLabel, color = colors.primary, maxLines = 1)
    }
}

/** The page dots above the dock: an 18 dp rose bar for the current page, 6 dp dots for the rest. */
@Composable
fun PageDots(count: Int, selectedIndex: Int, modifier: Modifier = Modifier) {
    val colors = DeckTheme.colors
    Row(
        modifier.clearAndSetSemantics {},   // the dock already tells TalkBack which page is on
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(count) { index ->
            val selected = index == selectedIndex
            val width by animateDpAsState(if (selected) 18.dp else 6.dp, label = "dot")
            Box(
                Modifier
                    .width(width)
                    .height(6.dp)
                    .background(if (selected) colors.primary else colors.pageDot, DeckShapes.pill),
            )
        }
    }
}
