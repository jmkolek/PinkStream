package com.example.pinkstream.ui.deck

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinkstream.BuildConfig
import com.example.pinkstream.R
import com.example.pinkstream.data.deck.DeckLayout
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.data.deck.MAX_PAGES
import com.example.pinkstream.ui.components.ChipTone
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.components.ConnectionChip
import com.example.pinkstream.ui.components.DeckSnackbarHost
import com.example.pinkstream.ui.components.PageDock
import com.example.pinkstream.ui.components.PageDots
import com.example.pinkstream.ui.components.StatusChip
import com.example.pinkstream.ui.edit.ActionPickerSheet
import com.example.pinkstream.ui.edit.PickerData
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType
import kotlinx.coroutines.launch

/** The deck (mockups 01-main and 02-edit-layout). */
@Composable
fun DeckScreen(
    onOpenSettings: () -> Unit,
    onOpenEdit: (keyId: String) -> Unit,
    onAddKey: (pageId: String, action: ActionInfo) -> Unit,
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
            DeckBody(loaded, viewModel, onOpenSettings, onOpenEdit, onAddKey)
        }
    }

    confirming?.let { key ->
        ConfirmRunDialog(key, onRun = viewModel::confirmRun, onCancel = viewModel::dismissConfirm)
    }
}

@Composable
private fun ColumnScope.DeckBody(
    state: DeckUiState,
    viewModel: DeckViewModel,
    onOpenSettings: () -> Unit,
    onOpenEdit: (String) -> Unit,
    onAddKey: (String, ActionInfo) -> Unit,
) {
    val pages = state.pages
    // The pager remembers the current page itself, also across rotation.
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val openEdit by rememberUpdatedState(onOpenEdit)
    // An empty slot opens the action picker for its page; picking opens Edit key.
    var addingToPageId by rememberSaveable { mutableStateOf<String?>(null) }
    // While a key is dragged, the pager doesn't swipe, so a sideways drag can't change page.
    var dragging by remember { mutableStateOf(false) }
    val actions = remember(viewModel) {
        KeyActions(
            viewModel.keyEvents,
            onRun = viewModel::onTap,
            onEdit = { openEdit(it.id) },
            onRemove = viewModel::removeKey,
            onAdd = { addingToPageId = it },
            onMove = viewModel::moveKey,
            onDragging = { dragging = it },
        )
    }
    // targetPage moves the dock and dots as soon as a swipe settles on its way, not after.
    val currentPage = pages.getOrNull(pagerState.targetPage) ?: pages.first()

    // The page sheet and dialog, by page id. rememberSaveable keeps them open across rotation.
    var editingPageId by rememberSaveable { mutableStateOf<String?>(null) }
    var addingPage by rememberSaveable { mutableStateOf(false) }
    var deletingPageId by rememberSaveable { mutableStateOf<String?>(null) }
    var scrollToPageId by rememberSaveable { mutableStateOf<String?>(null) }

    // A page added from the dock: go to it once it's in the layout.
    LaunchedEffect(viewModel) {
        viewModel.addedPages.collect { scrollToPageId = it }
    }
    LaunchedEffect(pages, scrollToPageId) {
        val index = pages.indexOfFirst { it.id == scrollToPageId }
        if (index >= 0) {
            pagerState.animateScrollToPage(index)
            scrollToPageId = null
        }
    }
    val editPage = { page: DeckPage -> editingPageId = page.id }

    DeckHeader(state, viewModel, onOpenSettings)
    ChipRow(state, currentPage, onRename = { editPage(currentPage) })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        key = { pages[it].id },
        userScrollEnabled = !dragging,
    ) { index ->
        DeckPageContent(pages[index], state, actions, onEditLayout = viewModel::startEditing)
    }

    DeckFooter(state, pagerState, pages.size)
    PageDock(
        pages = pages,
        selectedIndex = pagerState.targetPage,
        onSelect = { scope.launch { pagerState.animateScrollToPage(it) } },
        onLongPress = if (state.editing) { { editPage(pages[it]) } } else null,
        onAddPage = if (state.editing && pages.size < MAX_PAGES) { { addingPage = true } } else null,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
    )

    addingToPageId?.let { pageId ->
        val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
        ActionPickerSheet(
            data = PickerData(
                state.actions, refreshing, state.connected, DeckLayout(pages),
                keyId = null, currentActionId = null, currentActionName = null,
            ),
            onRefresh = viewModel::refreshActions,
            onUse = { action ->
                addingToPageId = null
                onAddKey(pageId, action)
            },
            onDismiss = { addingToPageId = null },
            onOpenSettings = {
                addingToPageId = null
                onOpenSettings()
            },
        )
    }

    if (addingPage) {
        PageSheet(
            page = null,
            onSave = { name, icon ->
                viewModel.addPage(name, icon)
                addingPage = false
            },
            onMoveLeft = null,
            onMoveRight = null,
            onDelete = null,
            onDismiss = { addingPage = false },
        )
    }
    pages.firstOrNull { it.id == editingPageId }?.let { page ->
        val index = pages.indexOf(page)
        // Moving keeps the sheet open and the deck on the moved page.
        val move = { by: Int ->
            viewModel.movePage(page.id, by)
            scrollToPageId = page.id
        }
        PageSheet(
            page = page,
            onSave = { name, icon ->
                viewModel.updatePage(page.id, name, icon)
                editingPageId = null
            },
            onMoveLeft = if (index > 0) { { move(-1) } } else null,
            onMoveRight = if (index < pages.lastIndex) { { move(1) } } else null,
            onDelete = if (pages.size > 1) {
                {
                    editingPageId = null
                    deletingPageId = page.id
                }
            } else {
                null
            },
            onDismiss = { editingPageId = null },
        )
    }
    pages.firstOrNull { it.id == deletingPageId }?.let { page ->
        DeletePageDialog(
            name = page.name,
            keyCount = page.keys.size,
            onDelete = {
                viewModel.deletePage(page.id)
                deletingPageId = null
            },
            onDismiss = { deletingPageId = null },
        )
    }
}

/** Connection chip, Edit layout and Settings; while editing, "Edit layout" and Done. */
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
        if (state.editing) {
            Text(
                "Edit layout",
                style = DeckType.button,
                color = DeckTheme.colors.ink,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.weight(1f))
            DonePill(onClick = viewModel::finishEditing)
        } else {
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
                CircleIconButton(R.drawable.ic_deck_pencil, "Edit layout", viewModel::startEditing)
                CircleIconButton(R.drawable.ic_deck_sliders, "Settings", onOpenSettings)
            }
        }
    }
    if (showPreview) {
        val preview by viewModel.preview.collectAsStateWithLifecycle()
        LivePreviewDialog(selected = preview, onSelect = viewModel::setPreview, onDismiss = { showPreview = false })
    }
}

/**
 * Under the header: what's on air and whether the mic is live (hidden while offline or
 * unknown); while editing, which page is being edited and "Rename page".
 */
@Composable
private fun ChipRow(state: DeckUiState, page: DeckPage, onRename: () -> Unit) {
    val colors = DeckTheme.colors
    val rowModifier = Modifier
        .fillMaxWidth()
        .padding(start = 20.dp, top = 14.dp, end = 20.dp)
    if (state.editing) {
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusChip(
                "Editing · ${page.name} page",
                ChipTone.ROSE,
                icon = R.drawable.ic_deck_pencil,
                modifier = Modifier.weight(1f, fill = false),
            )
            StatusChip("Rename page", ChipTone.PLAIN, onClick = onRename)
        }
        return
    }
    val onAir = onAirChipText(state.pages, state.live).takeIf { state.connected }
    val micLive = micChipLive(state.pages, state.live).takeIf { state.connected }
    if (onAir == null && micLive == null) return
    Row(
        // TalkBack announces the chips again when what's on air changes.
        rowModifier.semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onAir != null) {
            StatusChip("On air · $onAir", ChipTone.ROSE, modifier = Modifier.weight(1f, fill = false))
        }
        if (micLive != null) {
            StatusChip(
                if (micLive) "Mic live" else "Mic muted",
                ChipTone.PLAIN,
                icon = if (micLive) R.drawable.ic_deck_mic else R.drawable.ic_deck_mic_off,
                iconTint = colors.primary,
            )
        }
    }
}

/** Page dots above the dock; while editing, the hint about what taps do. */
@Composable
private fun DeckFooter(state: DeckUiState, pagerState: PagerState, pageCount: Int) {
    if (state.editing) {
        Text(
            "Drag a key to move it · Tap a key to edit it",
            style = DeckType.caption,
            color = DeckTheme.colors.inkSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 10.dp),
        )
    } else if (pageCount > 1) {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp), horizontalArrangement = Arrangement.Center) {
            PageDots(pageCount, pagerState.targetPage)
        }
    } else {
        Spacer(Modifier.height(12.dp))
    }
}
