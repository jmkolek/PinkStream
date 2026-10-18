package com.example.pinkstream.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pinkstream.ui.deck.DeckScreen
import com.example.pinkstream.ui.edit.EditKeyScreen
import com.example.pinkstream.ui.settings.SettingsScreen

/**
 * Route names. `edit/{keyId}` edits an existing key; `add/{pageId}` makes a new one at
 * the end of that page (it's only added to the layout on Save). The optional
 * `?actionId=…&actionName=…` part is the action picked for the new key.
 */
object Routes {
    const val DECK = "deck"
    const val SETTINGS = "settings"
    const val EDIT = "edit/{keyId}"
    const val ADD = "add/{pageId}?actionId={actionId}&actionName={actionName}"

    fun edit(keyId: String) = "edit/$keyId"

    // Uri.encode: action names can hold spaces, "&" or "/", which would break the route.
    fun add(pageId: String, actionId: String, actionName: String) =
        "add/$pageId?actionId=${Uri.encode(actionId)}&actionName=${Uri.encode(actionName)}"
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
                onAddKey = { pageId, action -> navController.navigate(Routes.add(pageId, action.id, action.name)) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        // The arguments land in the screen's SavedStateHandle, where EditKeyViewModel reads them.
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("keyId") { type = NavType.StringType }),
        ) {
            EditKeyScreen(onClose = { navController.popBackStack() })
        }
        composable(
            route = Routes.ADD,
            arguments = listOf(
                navArgument("pageId") { type = NavType.StringType },
                navArgument("actionId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("actionName") { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
        ) {
            EditKeyScreen(onClose = { navController.popBackStack() })
        }
    }
}
