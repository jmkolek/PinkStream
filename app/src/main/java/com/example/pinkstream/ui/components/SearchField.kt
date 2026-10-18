package com.example.pinkstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** 50 dp search box: magnifier, text, and a round clear button once something is typed. */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = DeckTheme.colors
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = placeholder },
        singleLine = true,
        textStyle = DeckType.fieldSettings.copy(color = colors.ink),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.surface, DeckShapes.searchField)
                    .border(if (focused) 1.5.dp else 1.dp, if (focused) colors.primary else colors.hairline, DeckShapes.searchField)
                    .padding(start = 14.dp, end = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_deck_search), contentDescription = null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, style = DeckType.fieldSettings, color = colors.placeholder)
                    innerTextField()
                }
                if (value.isNotEmpty()) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClickLabel = "Clear search") { onValueChange("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(28.dp).background(colors.boxMist.container, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_deck_close), contentDescription = "Clear search", tint = colors.inkSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                } else {
                    Box(Modifier.size(width = 11.dp, height = 44.dp))   // keeps the text clear of the edge
                }
            }
        },
    )
}
