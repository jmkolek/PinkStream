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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.components.KeyShape
import com.example.pinkstream.ui.theme.DeckTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Makes a key pressable: scales to 0.96 while held, ticks on press, plays a fading
 * rose ring on success and a sideways shake on error (DESIGN.md → Motion and haptics).
 * On success [content] also gets `flash = true` for 300 ms, so keys without a live
 * state can show their on art briefly (README.md → How the keys work).
 * TalkBack reads [description] ("Microphone, live"), and [clickLabel] says what a
 * double tap does ("Run" or "Edit").
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PressableKey(
    description: String,
    clickLabel: String,
    vibrate: Boolean,
    feedback: Flow<KeyFeedback>,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable (flash: Boolean) -> Unit,
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
    var flash by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Press && vibrateNow) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
    LaunchedEffect(feedback) {
        feedback.collect { kind ->
            when (kind) {
                KeyFeedback.SUCCESS -> {
                    if (vibrateNow) view.confirmHaptic()
                    launch {
                        ring.snapTo(1f)
                        ring.animateTo(0f, tween(durationMillis = 400))
                    }
                    launch {
                        flash = true
                        delay(FLASH_MS)
                        flash = false
                    }
                }
                KeyFeedback.ERROR -> {
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
                    // 3 dp outside the key's own outline (the art has a small margin).
                    val gap = 3.dp.toPx()
                    val key = KeyShape.rect(size)
                    drawRoundRect(
                        color = ringColor.copy(alpha = alpha),
                        topLeft = Offset(key.left - gap, key.top - gap),
                        size = Size(key.width + 2 * gap, key.height + 2 * gap),
                        cornerRadius = CornerRadius(key.topLeftCornerRadius.x + gap),
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
                onLongClickLabel = if (onLongClick != null) "Edit" else null,
                onLongClick = onLongClick,
                onClick = onClick,
            ),
    ) {
        // The description above already says everything; the art itself has no text.
        Box(Modifier.clearAndSetSemantics {}) { content(flash) }
    }
}

/** How long a key without a live state shows its on art after it ran (README.md). */
private const val FLASH_MS = 300L

private fun View.confirmHaptic() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) performHapticFeedback(HapticFeedbackConstants.CONFIRM)
}

private fun View.rejectHaptic() {
    performHapticFeedback(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS,
    )
}
