package com.example.pinkstream.data.live

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * The live state the tiles read. Milestone 5 fills [real] from OBS events; until then
 * it stays unknown. [preview] is fake data from the debug-only preview and wins while set.
 */
class LiveStateRepository(scope: CoroutineScope) {

    private val real = MutableStateFlow(LiveState())

    private val _preview = MutableStateFlow<LiveState?>(null)
    val preview: StateFlow<LiveState?> = _preview.asStateFlow()

    val state: StateFlow<LiveState> =
        combine(real, _preview) { real, preview -> preview ?: real }
            .stateIn(scope, SharingStarted.Eagerly, LiveState())

    /** Debug builds only. null = back to the real state. */
    fun setPreview(state: LiveState?) {
        _preview.value = state
    }
}
