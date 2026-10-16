package com.example.pinkstream.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Equal-width columns with gaps, for use inside a scrolling column.
 * (LazyVerticalGrid can't sit inside another vertical scroll, and a deck is small.)
 */
@Composable
fun <T> TileGrid(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    gap: Dp = 10.dp,
    itemContent: @Composable (T) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { item ->
                    Box(Modifier.weight(1f)) { itemContent(item) }
                }
                // Keep the last row's tiles the same width as the rows above.
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
