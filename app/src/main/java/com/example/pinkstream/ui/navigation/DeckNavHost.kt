package com.example.pinkstream.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pinkstream.ui.connectiontest.ConnectionTestScreen
import com.example.pinkstream.ui.placeholder.PlaceholderScreen
import com.example.pinkstream.ui.settings.SettingsScreen
import com.example.pinkstream.ui.themecheck.ThemeCheckScreen

/** Route names. `edit/{buttonId}` carries the id of the tile being edited. */
object Routes {
    const val CONNECTION_TEST = "connection-test"   // temporary start screen, removed in milestone 3
    const val THEME_CHECK = "theme-check"           // temporary, removed in milestone 3
    const val DECK = "deck"
    const val SETTINGS = "settings"
    const val EDIT = "edit/{buttonId}"

    fun edit(buttonId: String) = "edit/$buttonId"
}

/**
 * The NavHost swaps whole screens as the route changes and keeps a back stack,
 * so the system back gesture returns to the previous screen.
 */
@Composable
fun DeckNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.CONNECTION_TEST) {
        composable(Routes.CONNECTION_TEST) {
            ConnectionTestScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenThemeCheck = { navController.navigate(Routes.THEME_CHECK) },
            )
        }
        composable(Routes.THEME_CHECK) {
            ThemeCheckScreen(
                onOpenDeck = { navController.navigate(Routes.DECK) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenEdit = { navController.navigate(Routes.edit("theme-check")) },
            )
        }
        composable(Routes.DECK) {
            PlaceholderScreen(title = "Deck", note = "Coming in milestone 3.", onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("buttonId") { type = NavType.StringType }),
        ) { entry ->
            val buttonId = entry.arguments?.getString("buttonId").orEmpty()
            PlaceholderScreen(
                title = "Edit button",
                note = "Coming in milestone 4. Button id: $buttonId",
                onBack = { navController.popBackStack() },
            )
        }
    }
}
