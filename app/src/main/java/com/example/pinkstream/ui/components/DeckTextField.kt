package com.example.pinkstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * The Settings-style field: label above, 48 dp inset box, helper or error below.
 * Built on BasicTextField (not Material's OutlinedTextField) to match the mockups.
 * Focused: 1.5 dp primary border. Error: 1.5 dp error border plus the message.
 */
@Composable
fun DeckTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helper: String? = null,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = DeckTheme.colors
    // The interaction source reports focus, which drives the border colour.
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val (borderWidth, borderColor) = when {
        error != null -> 1.5.dp to colors.error
        focused -> 1.5.dp to colors.primary
        else -> 1.dp to colors.fieldBorder
    }

    Column(modifier.fillMaxWidth()) {
        Text(label, style = DeckType.fieldLabel, color = colors.inkSecondary)
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = DeckType.fieldSettings.copy(color = colors.ink),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(colors.fieldBg, DeckShapes.settingsField)
                        .border(borderWidth, borderColor, DeckShapes.settingsField)
                        .padding(start = 14.dp, end = if (trailing != null) 4.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(placeholder, style = DeckType.fieldSettings, color = colors.placeholder)
                        }
                        innerTextField()
                    }
                    trailing?.invoke()
                }
            },
        )
        val below = error ?: helper
        if (below != null) {
            Spacer(Modifier.height(6.dp))
            Text(below, style = DeckType.helper, color = if (error != null) colors.error else colors.inkSecondary)
        }
    }
}
