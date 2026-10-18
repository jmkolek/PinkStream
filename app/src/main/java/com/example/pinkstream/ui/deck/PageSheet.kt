package com.example.pinkstream.ui.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.ui.components.DeckBottomSheet
import com.example.pinkstream.ui.components.DeckTextField
import com.example.pinkstream.ui.components.FieldStyle
import com.example.pinkstream.ui.components.PrimaryButton
import com.example.pinkstream.ui.components.SectionLabel
import com.example.pinkstream.ui.components.TonalButton
import com.example.pinkstream.ui.icons.PageIcon
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * New page / Edit page, in the Choose an action sheet's style: name, dock icon, and for
 * an existing page Move left / Move right and Delete. [page] null = a new page, which is
 * only added on Save. [onMove] and [onDelete] are null when that isn't possible.
 */
@Composable
fun PageSheet(
    page: DeckPage?,
    onSave: (name: String, icon: PageIcon) -> Unit,
    onMoveLeft: (() -> Unit)?,
    onMoveRight: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val colors = DeckTheme.colors
    var name by rememberSaveable { mutableStateOf(page?.name.orEmpty()) }
    var icon by rememberSaveable { mutableStateOf(page?.icon ?: PageIcon.STAR) }
    // A blank name keeps the page's current one (or "New page" for a new page).
    val save = { onSave(name.trim().ifEmpty { page?.name ?: NEW_PAGE_NAME }, icon) }

    DeckBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Text(if (page == null) "New page" else "Edit page", style = DeckType.sheetTitle, color = colors.ink)

            SectionLabel("Name", top = 16.dp, bottom = 8.dp)
            DeckTextField(
                value = name,
                onValueChange = { name = it },
                label = null,
                style = FieldStyle.EDIT,
                placeholder = page?.name ?: NEW_PAGE_NAME,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            )

            SectionLabel("Icon", top = 16.dp, bottom = 8.dp)
            IconGrid(selected = icon, onSelect = { icon = it })

            if (page != null) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TonalButton(
                        text = "Move left",
                        icon = R.drawable.ic_deck_chevron_left,
                        onClick = { onMoveLeft?.invoke() },
                        enabled = onMoveLeft != null,
                        modifier = Modifier.weight(1f),
                    )
                    TonalButton(
                        text = "Move right",
                        icon = R.drawable.ic_deck_chevron_right,
                        onClick = { onMoveRight?.invoke() },
                        enabled = onMoveRight != null,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (onDelete != null) {
                    TextButton(onClick = onDelete, modifier = Modifier.padding(top = 4.dp)) {
                        Text("Delete this page", style = DeckType.buttonSmall, color = colors.error)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            PrimaryButton(if (page == null) "Add page" else "Save changes", onClick = save)
        }
    }
}

/** Every [PageIcon], six to a row. The chosen one sits on the blush pill, like the active dock tab. */
@Composable
private fun IconGrid(selected: PageIcon, onSelect: (PageIcon) -> Unit) {
    val colors = DeckTheme.colors
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PageIcon.entries.chunked(ICONS_PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { icon ->
                    val isSelected = icon == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(DeckShapes.dockIcon)
                            .background(if (isSelected) colors.primaryContainer else colors.background)
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(icon) })
                            .semantics { contentDescription = "${icon.label} icon" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painterResource(icon.drawable),
                            contentDescription = null,
                            tint = if (isSelected) colors.primary else colors.inkSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                // Keep a short last row lined up with the rows above.
                repeat(ICONS_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val ICONS_PER_ROW = 6
private const val NEW_PAGE_NAME = "New page"
