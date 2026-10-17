package com.example.pinkstream.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.components.DeckTile
import com.example.pinkstream.ui.components.MicHero
import com.example.pinkstream.ui.components.MicState
import com.example.pinkstream.ui.components.TileLook
import com.example.pinkstream.ui.components.softShadow
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * The preview card at the top of Edit button. Shows the states the button can be in:
 * Normal and While on air for scenes, On and Off for toggles, Live and Muted for the mic.
 */
@Composable
fun ButtonPreview(draft: ButtonDraft, isHero: Boolean) {
    val colors = DeckTheme.colors
    val label = draft.label.ifBlank { "Label" }
    Column(
        Modifier
            .fillMaxWidth()
            .softShadow(colors.raisedShadow, 12.dp, DeckShapes.previewStage)
            .background(colors.surface, DeckShapes.previewStage)
            .border(1.dp, colors.hairline, DeckShapes.previewStage)
            .padding(14.dp)
            // Decoration only: the form below says the same thing to TalkBack.
            .clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isHero) {
            val states = if (draft.showState) listOf(MicState.LIVE to "Live", MicState.MUTED to "Muted") else listOf(MicState.UNKNOWN to "Normal")
            states.forEach { (state, caption) ->
                Captioned(caption) { MicHero(label = label, icon = draft.icon, state = state) }
            }
        } else {
            val looks = when {
                draft.onAir -> listOf(TileLook.NORMAL to "Normal", TileLook.ON_AIR to "While on air")
                draft.showState -> listOf(TileLook.TOGGLE_ON to "On", TileLook.TOGGLE_OFF to "Off")
                else -> listOf(TileLook.NORMAL to "Normal")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)) {
                looks.forEach { (look, caption) ->
                    Captioned(caption) {
                        DeckTile(label, draft.icon, draft.color, look, modifier = Modifier.size(width = 116.dp, height = 92.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Captioned(caption: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        content()
        Text(caption, style = DeckType.helper, color = DeckTheme.colors.inkSecondary)
    }
}
