package com.example.pinkstream.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.components.KeyArtImage
import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** The preview card: the key's off art ("Normal") and on art ("On air", "On" or "Pressed"). */
@Composable
fun KeyPreview(draft: KeyDraft) {
    val colors = DeckTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, DeckShapes.previewStage)
            .border(1.dp, colors.hairline, DeckShapes.previewStage)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
    ) {
        PreviewArt(draft.art, on = false, label = "Normal", name = draft.label)
        PreviewArt(draft.art, on = true, label = draft.onArtLabel(), name = draft.label)
    }
}

@Composable
private fun PreviewArt(art: DeckKeyArt, on: Boolean, label: String, name: String) {
    Column(
        Modifier.clearAndSetSemantics { contentDescription = "${name.ifBlank { art.label }} key, ${label.lowercase()}" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KeyArtImage(art, on = on, modifier = Modifier.size(104.dp))
        Spacer(Modifier.size(10.dp))
        Text(label, style = DeckType.dockLabel, color = DeckTheme.colors.inkSecondary)
    }
}
