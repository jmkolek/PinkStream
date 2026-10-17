package com.example.pinkstream.ui.deck

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Makes a tile (or the hero) pressable: scales to 0.96 while held, ticks on press,
 * plays a fading rose ring on success and a sideways shake on error
 * (DESIGN.md → States not in the mockups, Motion and haptics).
 * [cornerRadius] is the tile's own, so the ring follows its shape.
 * TalkBack reads [description] ("Microphone, live") instead of the tile's texts, and
 * [clickLabel] says what a double tap does ("Run" or "Edit").
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PressableTile(
    cornerRadius: Dp,
    description: String,
    clickLabel: String,
    vibrate: Boolean,
    feedback: Flow<TileFeedback>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val ringColor = DeckTheme.colors.primary
    // rememberUpdatedState: the long-running effects below always see the latest setting.
    val vibrateNow by rememberUpdatedState(vibrate)

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = if (pressed) tween(durationMillis = 100) else spring(),
        label = "press",
    )
    // Animatable = a value we animate by hand from coroutines (one per effect).
    val ring = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Press && vibrateNow) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
    LaunchedEffect(feedback) {
        feedback.collect { kind ->
            when (kind) {
                TileFeedback.SUCCESS -> {
                    if (vibrateNow) view.confirmHaptic()
                    launch {
                        ring.snapTo(1f)
                        ring.animateTo(0f, tween(durationMillis = 400))
                    }
                }
                TileFeedback.ERROR -> {
                    if (vibrateNow) view.rejectHaptic()
                    launch {
                        shake.snapTo(0f)
                        // Three left-right swings of 6 dp in 300 ms.
                        shake.animateTo(0f, keyframes {
                            durationMillis = 300
                            6f at 25
                            -6f at 75
                            6f at 125
                            -6f at 175
                            6f at 225
                            -6f at 275
                        })
                    }
                }
            }
        }
    }

    Box(
        modifier
            // graphicsLayer moves and scales the drawing only; reading the values
            // inside the lambda means animation frames skip recomposition.
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = shake.value.dp.toPx()
            }
            .drawWithContent {
                drawContent()
                val alpha = ring.value
                if (alpha > 0f) {
                    val gap = 3.dp.toPx()
                    drawRoundRect(
                        color = ringColor.copy(alpha = alpha),
                        topLeft = Offset(-gap, -gap),
                        size = Size(size.width + 2 * gap, size.height + 2 * gap),
                        cornerRadius = CornerRadius(cornerRadius.toPx() + gap),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }
            .semantics { contentDescription = description }
            // No ripple: the press scale is the feedback, and a ripple wouldn't follow the corners.
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = clickLabel,
                onLongClickLabel = "Edit",
                onLongClick = onLongClick,
                onClick = onClick,
            ),
    ) {
        // The description above already says everything the texts inside would.
        Box(Modifier.clearAndSetSemantics {}) { content() }
    }
}

private fun View.confirmHaptic() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) performHapticFeedback(HapticFeedbackConstants.CONFIRM)
}

private fun View.rejectHaptic() {
    performHapticFeedback(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS,
    )
}
