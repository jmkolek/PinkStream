package com.example.pinkstream.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.DeckTextField
import com.example.pinkstream.ui.components.TonalButton
import com.example.pinkstream.ui.components.softShadow
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * The raised card: a status row each for Streamer.bot and OBS, then PC address,
 * Streamer.bot port and password, OBS port and password, and Test connection.
 * The OBS rows aren't in the mockup; they copy the Streamer.bot ones.
 */
@Composable
fun ConnectionCard(
    form: ConnectionForm,
    status: ConnectionStatus,
    obsStatus: ConnectionStatus,
    viewModel: SettingsViewModel,
) {
    val colors = DeckTheme.colors
    val focusManager = LocalFocusManager.current
    Column(
        Modifier
            .fillMaxWidth()
            .softShadow(colors.raisedShadow, 12.dp, DeckShapes.settingsCard)
            .background(colors.surface, DeckShapes.settingsCard)
            .border(1.dp, colors.hairline, DeckShapes.settingsCard)
            .padding(18.dp),
    ) {
        StatusRow(status)
        Spacer(Modifier.height(12.dp))
        StatusRow(obsStatus)
        HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = colors.hairline)

        DeckTextField(
            value = form.host,
            onValueChange = viewModel::onHostChange,
            label = "PC address",
            helper = "127.0.0.1 over USB, your PC's local IP on Wi-Fi",
            error = form.hostError,
            onFocusLost = viewModel::onHostFocusLost,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DeckTextField(
                value = form.port,
                onValueChange = viewModel::onPortChange,
                label = "Port",
                error = form.portError,
                onFocusLost = viewModel::onPortFocusLost,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.width(104.dp),
            )
            DeckTextField(
                value = form.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                placeholder = "Optional",
                onFocusLost = viewModel::onPasswordFocusLost,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                visualTransformation = if (form.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailing = { PasswordToggle(visible = form.passwordVisible, onToggle = viewModel::togglePasswordVisible) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DeckTextField(
                value = form.obsPort,
                onValueChange = viewModel::onObsPortChange,
                label = "OBS port",
                error = form.obsPortError,
                onFocusLost = viewModel::onObsPortFocusLost,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.width(104.dp),
            )
            DeckTextField(
                value = form.obsPassword,
                onValueChange = viewModel::onObsPasswordChange,
                label = "OBS password",
                placeholder = "Optional",
                onFocusLost = viewModel::onObsPasswordFocusLost,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                // Done closes the keyboard and leaves the field, which saves it.
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                visualTransformation = if (form.obsPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailing = { PasswordToggle(visible = form.obsPasswordVisible, onToggle = viewModel::toggleObsPasswordVisible) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
        TonalButton(
            text = "Test connection",
            icon = R.drawable.ic_deck_refresh,
            onClick = {
                focusManager.clearFocus()
                viewModel.testConnection()
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StatusRow(status: ConnectionStatus) {
    val colors = DeckTheme.colors
    val (tileColor, iconColor, icon) = when (status.kind) {
        StatusKind.CONNECTED -> Triple(colors.successContainer, colors.onSuccessContainer, R.drawable.ic_deck_check)
        StatusKind.PROBLEM -> Triple(colors.errorContainer, colors.onErrorContainer, R.drawable.ic_deck_close)
        StatusKind.WORKING -> Triple(colors.boxMist.container, colors.boxMist.icon, R.drawable.ic_deck_refresh)
        StatusKind.IDLE -> Triple(colors.boxMist.container, colors.boxMist.icon, R.drawable.ic_deck_minus)
    }
    Row(
        // One TalkBack item, announced again whenever the status changes.
        Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(tileColor, DeckShapes.pickerIconBox),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(status.title, style = DeckType.rowTitle, color = colors.ink)
            Text(status.subtitle, style = DeckType.rowSubtitle, color = colors.inkSecondary)
        }
    }
}

/** Eye button at the end of the password field. */
@Composable
private fun PasswordToggle(visible: Boolean, onToggle: () -> Unit) {
    val label = if (visible) "Hide password" else "Show password"
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(if (visible) R.drawable.ic_deck_eye_off else R.drawable.ic_deck_eye),
            contentDescription = label,
            tint = DeckTheme.colors.inkSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}
