package com.example.pinkstream.ui.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.components.DeckBottomSheet
import com.example.pinkstream.ui.components.SearchField
import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * "Choose key art": every design in one grid, name under each, no groups, plus a search
 * by name. Tapping one picks it and closes the sheet, so there's no "Use" button.
 */
@Composable
fun KeyArtSheet(selected: DeckKeyArt, onSelect: (DeckKeyArt) -> Unit, onDismiss: () -> Unit) {
    val colors = DeckTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    val matches = DeckKeyArt.search(query)
    // Opens with the current art's row on screen, one row down from the top.
    val gridState = rememberLazyGridState(initialFirstVisibleItemIndex = (selected.ordinal / COLUMNS - 1).coerceAtLeast(0) * COLUMNS)

    DeckBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(start = 20.dp, top = 16.dp, end = 20.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Choose key art", style = DeckType.sheetTitle, color = colors.ink)
                    Text("${DeckKeyArt.entries.size} designs · tap one to use it", style = DeckType.sheetSubtitle, color = colors.inkSecondary)
                }
                CircleIconButton(R.drawable.ic_deck_close, "Close", onDismiss)
            }

            Spacer(Modifier.height(16.dp))
            SearchField(value = query, onValueChange = { query = it }, placeholder = "Search by name")
            Spacer(Modifier.height(14.dp))

            if (matches.isEmpty()) {
                Text(
                    "No key art matches “${query.trim()}”.",
                    style = DeckType.rowTitle,
                    color = colors.ink,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                )
            } else {
                // A lazy grid only builds the rows on screen, like LazyColumn but in columns.
                LazyVerticalGrid(
                    columns = GridCells.Fixed(COLUMNS),
                    state = gridState,
                    modifier = Modifier
                        .weight(1f)
                        .selectableGroup(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(matches, key = { it.name }) { art ->
                        ArtCell(art, selected = art == selected, onClick = { onSelect(art) })
                    }
                }
            }
        }
    }
}

/** 64 dp art with its name under it. The chosen one gets the rose ring and a rose name. */
@Composable
private fun ArtCell(art: DeckKeyArt, selected: Boolean, onClick: () -> Unit) {
    val colors = DeckTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(DeckShapes.artChoice)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .then(
                    if (selected) {
                        // Same ring as the deck's chosen art: 2.5 dp rose, then a 2 dp gap.
                        Modifier
                            .border(2.5.dp, colors.primary, DeckShapes.artChoice)
                            .padding(2.5.dp)
                            .border(2.dp, colors.background, DeckShapes.artChoice)
                    } else {
                        Modifier
                    },
                ),
        ) {
            Image(painterResource(art.off), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        // Up to two lines, so long names like "Instant replay" stay whole at large font sizes.
        Text(
            art.label,
            style = DeckType.helper.copy(fontSize = 11.5.sp, lineHeight = 14.sp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colors.primary else colors.inkSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val COLUMNS = 4
