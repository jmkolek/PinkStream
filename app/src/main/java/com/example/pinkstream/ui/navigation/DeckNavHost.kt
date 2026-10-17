package com.example.pinkstream.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pinkstream.ui.deck.DeckScreen
import com.example.pinkstream.ui.edit.EditButtonScreen
import com.example.pinkstream.ui.settings.SettingsScreen

/**
 * Route names. `edit/{buttonId}` edits an existing tile; `add/{sectionId}` makes a new
 * one in that section (it's only added to the layout on Save).
 */
object Routes {
    const val DECK = "deck"
    const val SETTINGS = "settings"
    const val EDIT = "edit/{buttonId}"
    const val ADD = "add/{sectionId}"

    fun edit(buttonId: String) = "edit/$buttonId"
    fun add(sectionId: String) = "add/$sectionId"
}

/**
 * The NavHost swaps whole screens as the route changes and keeps a back stack,
 * so the system back gesture returns to the previous screen.
 */
@Composable
fun DeckNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.DECK) {
        composable(Routes.DECK) {
            DeckScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenEdit = { navController.navigate(Routes.edit(it)) },
                onAddButton = { navController.navigate(Routes.add(it)) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        // The arguments land in the screen's SavedStateHandle, where EditButtonViewModel reads them.
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("buttonId") { type = NavType.StringType }),
        ) {
            EditButtonScreen(onClose = { navController.popBackStack() })
        }
        composable(
            route = Routes.ADD,
            arguments = listOf(navArgument("sectionId") { type = NavType.StringType }),
        ) {
            EditButtonScreen(onClose = { navController.popBackStack() })
        }
    }
}
