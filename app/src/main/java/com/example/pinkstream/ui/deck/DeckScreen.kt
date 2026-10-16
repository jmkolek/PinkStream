package com.example.pinkstream.ui.deck

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinkstream.BuildConfig
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.components.ConnectionChip
import com.example.pinkstream.ui.components.DeckSnackbarHost
import com.example.pinkstream.ui.components.PageTabs
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType
import kotlinx.coroutines.launch

/** The deck (mockups 01-deck-day and 02-deck-night). */
@Composable
fun DeckScreen(
    onOpenSettings: () -> Unit,
    onOpenEdit: (buttonId: String) -> Unit,
    viewModel: DeckViewModel = viewModel(factory = DeckViewModel.Factory),
) {
    val colors = DeckTheme.colors
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val confirming by viewModel.confirming.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            // The newest message replaces the one on screen instead of waiting behind it.
            snackbarHostState.currentSnackbarData?.dismiss()
            launch {
                val result = snackbarHostState.showSnackbar(
                    message.text,
                    actionLabel = if (message.offerSettings) "Settings" else null,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) onOpenSettings()
            }
        }
    }

    // Keep the screen awake while the deck is showing (and the setting is on).
    // keepScreenOn on the app's view lasts only while that view is on screen.
    val view = LocalView.current
    val keepScreenOn = state?.keepScreenOn == true
    DisposableEffect(view, keepScreenOn) {
        view.keepScreenOn = keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { DeckSnackbarHost(snackbarHostState) },
    ) { insets ->
        val loaded = state ?: return@Scaffold   // layout loads in a few milliseconds
        Column(
            Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
            DeckHeader(loaded, viewModel, onOpenSettings)
            DeckPages(loaded, viewModel, onOpenEdit)
        }
    }

    confirming?.let { button ->
        ConfirmRunDialog(button, onRun = viewModel::confirmRun, onCancel = viewModel::dismissConfirm)
    }
}

@Composable
private fun DeckHeader(state: DeckUiState, viewModel: DeckViewModel, onOpenSettings: () -> Unit) {
    var showPreview by rememberSaveable { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 22.dp, end = 20.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConnectionChip(
            state.chip,
            // Debug builds: long-press the chip to preview live states with fake data.
            modifier = if (BuildConfig.DEBUG) {
                Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { showPreview = true }) }
            } else {
                Modifier
            },
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircleIconButton(R.drawable.ic_deck_pencil, "Edit layout", viewModel::onEditLayout)
            CircleIconButton(R.drawable.ic_deck_sliders, "Settings", onOpenSettings)
        }
    }
    if (showPreview) {
        val preview by viewModel.preview.collectAsStateWithLifecycle()
        LivePreviewDialog(selected = preview, onSelect = viewModel::setPreview, onDismiss = { showPreview = false })
    }
}

@Composable
private fun ColumnScope.DeckPages(
    state: DeckUiState,
    viewModel: DeckViewModel,
    onOpenEdit: (String) -> Unit,
) {
    val pages = state.pages
    // The pager remembers the current page itself, also across rotation.
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val openEdit by rememberUpdatedState(onOpenEdit)
    val actions = remember(viewModel) {
        TileActions(viewModel.tileEvents, onRun = viewModel::onTap, onEdit = { openEdit(it.id) })
    }

    PageTabs(
        names = pages.map { it.name },
        // targetPage moves the dot as soon as a swipe settles on its way, not after.
        selectedIndex = pagerState.targetPage,
        onSelect = { scope.launch { pagerState.animateScrollToPage(it) } },
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 14.dp, end = 20.dp),
    )

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        key = { pages[it].id },
    ) { index ->
        DeckPageContent(pages[index], state, actions, onEditLayout = viewModel::onEditLayout)
    }

    if (pages.getOrNull(pagerState.currentPage)?.isEmpty() == false) {
        Box(Modifier.fillMaxWidth().padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp)) {
            Text(
                "Hold any tile to edit it",
                style = DeckType.caption,
                color = DeckTheme.colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Spacer(Modifier.height(24.dp))
    }
}
