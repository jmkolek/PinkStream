package com.example.pinkstream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.HeroPalette
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** UNKNOWN = live styling without a pill, used until live state works (milestone 5). */
enum class MicState { LIVE, MUTED, UNKNOWN }

/**
 * The full-width 82 dp microphone tile at the top of the deck. Visual only.
 * [subtitle] replaces the state's own second line (the deck uses it for "No action").
 */
@Composable
fun MicHero(
    label: String,
    icon: DeckIcon,
    state: MicState,
    modifier: Modifier = Modifier,
    offline: Boolean = false,
    subtitle: String? = null,
) {
    val colors = DeckTheme.colors
    val palette = animatePalette(if (state == MicState.MUTED) colors.heroMuted else colors.heroLive)
    val secondLine = subtitle ?: when (state) {
        MicState.LIVE -> "Tap to mute"
        MicState.MUTED -> "Chat can't hear you"
        MicState.UNKNOWN -> "Tap to toggle"
    }

    Row(
        modifier
            .fillMaxWidth()
            .height(82.dp)
            .alpha(if (offline) 0.45f else 1f)
            .softShadow(palette.shadow, 14.dp, DeckShapes.hero)
            .clip(DeckShapes.hero)
            .background(palette.container)
            .then(if (palette.border.alpha > 0f) Modifier.border(1.dp, palette.border, DeckShapes.hero) else Modifier)
            .padding(start = 14.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(52.dp)
                .background(palette.iconBox, DeckShapes.heroIconBox),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(if (state == MicState.MUTED) icon.offDrawable else icon.drawable),
                contentDescription = null,
                tint = palette.icon,
                modifier = Modifier.size(26.dp),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = DeckType.heroTitle, color = palette.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(secondLine, style = DeckType.heroSubtitle, color = palette.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (state != MicState.UNKNOWN) {
            Row(
                Modifier
                    .height(32.dp)
                    .background(palette.pill, DeckShapes.pill)
                    .padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                // Live: the dot gets a soft ring like the mockup; Muted: a plain dot.
                val halo = if (state == MicState.LIVE) palette.pillText.copy(alpha = 0.28f) else Color.Transparent
                HaloDot(color = palette.pillText, size = 7.dp, halo = halo)
                Text(if (state == MicState.LIVE) "Live" else "Muted", style = DeckType.pillLarge, color = palette.pillText)
            }
        }
    }
}

/** Crossfades every hero colour over 200 ms when Live and Muted swap. */
@Composable
private fun animatePalette(target: HeroPalette): HeroPalette {
    val container by animateColorAsState(target.container, StateChange, label = "heroContainer")
    val border by animateColorAsState(target.border, StateChange, label = "heroBorder")
    val shadow by animateColorAsState(target.shadow, StateChange, label = "heroShadow")
    val title by animateColorAsState(target.title, StateChange, label = "heroTitle")
    val subtitle by animateColorAsState(target.subtitle, StateChange, label = "heroSubtitle")
    val iconBox by animateColorAsState(target.iconBox, StateChange, label = "heroIconBox")
    val icon by animateColorAsState(target.icon, StateChange, label = "heroIcon")
    val pill by animateColorAsState(target.pill, StateChange, label = "heroPill")
    val pillText by animateColorAsState(target.pillText, StateChange, label = "heroPillText")
    return HeroPalette(container, border, shadow, title, subtitle, iconBox, icon, pill, pillText)
}
