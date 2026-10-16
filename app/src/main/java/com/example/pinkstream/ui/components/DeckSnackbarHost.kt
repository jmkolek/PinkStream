package com.example.pinkstream.ui.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pinkstream.ui.theme.DeckShapes

/**
 * Snackbars with the design's 16 dp corners. Colours come from the Material mapping:
 * inverseSurface background, inverseOnSurface text, inversePrimary action.
 */
@Composable
fun DeckSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data ->
        Snackbar(data, shape = DeckShapes.snackbar)
    }
}
