package com.example.pinkstream.ui.deck

import androidx.activity.compose.BackHandler
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
    onAddButton: (sectionId: String) -> Unit,
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
                    actionLabel = message.action?.label,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    when (message.action) {
                        MessageAction.OPEN_SETTINGS -> onOpenSettings()
                        MessageAction.UNDO_REMOVE -> viewModel.undoRemove()
                        null -> Unit
                    }
                }
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

    // While editing the layout, the back gesture leaves edit mode first.
    BackHandler(enabled = state?.editing == true) { viewModel.finishEditing() }

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
            DeckPages(loaded, viewModel, onOpenEdit, onAddButton)
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
            if (state.editing) {
                DonePill(onClick = viewModel::finishEditing)
            } else {
                CircleIconButton(R.drawable.ic_deck_pencil, "Edit layout", viewModel::startEditing)
            }
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
    onAddButton: (String) -> Unit,
) {
    val pages = state.pages
    // The pager remembers the current page itself, also across rotation.
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val openEdit by rememberUpdatedState(onOpenEdit)
    val addButton by rememberUpdatedState(onAddButton)
    val actions = remember(viewModel) {
        TileActions(
            viewModel.tileEvents,
            onRun = viewModel::onTap,
            onEdit = { openEdit(it.id) },
            onRemove = viewModel::removeButton,
            onAdd = { addButton(it) },
        )
    }

    // Page dialogs, by page id. rememberSaveable keeps them open across rotation.
    var renamingPageId by rememberSaveable { mutableStateOf<String?>(null) }
    var renamingNewPage by rememberSaveable { mutableStateOf(false) }
    var deletingPageId by rememberSaveable { mutableStateOf<String?>(null) }
    var scrollToPageId by rememberSaveable { mutableStateOf<String?>(null) }

    // A page added with "+": go to it once it's in the layout, and ask for its name.
    LaunchedEffect(viewModel) {
        viewModel.addedPages.collect { id ->
            scrollToPageId = id
            renamingPageId = id
            renamingNewPage = true
        }
    }
    LaunchedEffect(pages, scrollToPageId) {
        val index = pages.indexOfFirst { it.id == scrollToPageId }
        if (index >= 0) {
            pagerState.animateScrollToPage(index)
            scrollToPageId = null
        }
    }

    PageTabs(
        names = pages.map { it.name },
        // targetPage moves the dot as soon as a swipe settles on its way, not after.
        selectedIndex = pagerState.targetPage,
        onSelect = { scope.launch { pagerState.animateScrollToPage(it) } },
        onLongPress = if (state.editing) {
            { index ->
                renamingPageId = pages[index].id
                renamingNewPage = false
            }
        } else {
            null
        },
        trailing = if (state.editing) {
            { CircleIconButton(R.drawable.ic_deck_plus, "Add a page", viewModel::addPage) }
        } else {
            null
        },
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
        DeckPageContent(pages[index], state, actions, onEditLayout = viewModel::startEditing)
    }

    if (state.editing || pages.getOrNull(pagerState.currentPage)?.isEmpty() == false) {
        Box(Modifier.fillMaxWidth().padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp)) {
            Text(
                if (state.editing) "Tap a tile to edit it · hold a page name to rename it" else "Hold any tile to edit it",
                style = DeckType.caption,
                color = DeckTheme.colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Spacer(Modifier.height(24.dp))
    }

    pages.firstOrNull { it.id == renamingPageId }?.let { page ->
        RenamePageDialog(
            currentName = page.name,
            startEmpty = renamingNewPage,
            onSave = { name ->
                viewModel.renamePage(page.id, name)
                renamingPageId = null
            },
            onDelete = if (pages.size > 1) {
                {
                    renamingPageId = null
                    deletingPageId = page.id
                }
            } else {
                null
            },
            onDismiss = { renamingPageId = null },
        )
    }
    pages.firstOrNull { it.id == deletingPageId }?.let { page ->
        DeletePageDialog(
            name = page.name,
            buttonCount = page.sections.sumOf { it.buttons.size },
            onDelete = {
                viewModel.deletePage(page.id)
                deletingPageId = null
            },
            onDismiss = { deletingPageId = null },
        )
    }
}
