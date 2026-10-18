package com.example.pinkstream.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** ROSE = blush with rose text ("On air · Gaming"); PLAIN = white with a hairline ("Mic live"). */
enum class ChipTone { ROSE, PLAIN }

/**
 * A 34 dp pill under the deck header. With [onClick] it's a button ("Rename page"),
 * given a 44 dp touch height around the pill.
 */
@Composable
fun StatusChip(
    text: String,
    tone: ChipTone,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    iconTint: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = DeckTheme.colors
    val background = if (tone == ChipTone.ROSE) colors.primaryContainer else colors.surface
    val border = if (tone == ChipTone.ROSE) Color.Transparent else colors.hairline
    val textColor = if (tone == ChipTone.ROSE) colors.onPrimaryContainer else colors.ink
    val pill = @Composable {
        Row(
            Modifier
                .height(34.dp)
                .clip(DeckShapes.pill)
                .background(background)
                .border(1.dp, border, DeckShapes.pill)
                .padding(start = if (icon != null) 12.dp else 14.dp, end = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(painterResource(icon), contentDescription = null, tint = iconTint ?: textColor, modifier = Modifier.size(16.dp))
            }
            Text(text, style = DeckType.pillLarge, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    if (onClick == null) {
        Box(modifier) { pill() }
    } else {
        Box(
            modifier
                .height(44.dp)
                .clip(DeckShapes.pill)
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { pill() }
    }
}
