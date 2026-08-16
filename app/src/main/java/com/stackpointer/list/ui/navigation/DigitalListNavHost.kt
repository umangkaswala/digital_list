package com.stackpointer.list.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.ui.screens.bin.BinScreen
import com.stackpointer.list.ui.screens.collections.CollectionsScreen
import com.stackpointer.list.ui.screens.completed.CompletedScreen
import com.stackpointer.list.ui.screens.detail.DetailScreen
import com.stackpointer.list.ui.screens.editor.EditorScreen
import com.stackpointer.list.ui.screens.home.HomeScreen
import com.stackpointer.list.ui.screens.noalert.NoAlertScreen
import com.stackpointer.list.ui.screens.scheduled.ScheduledScreen
import com.stackpointer.list.ui.screens.search.SearchScreen
import com.stackpointer.list.ui.screens.settings.SettingsScreen
import com.stackpointer.list.ui.screens.starred.StarredScreen
import com.stackpointer.list.ui.screens.templates.TemplatesScreen
import com.stackpointer.list.ui.screens.today.TodayScreen

@Composable
fun DigitalListNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    deepLink: ItemDeepLink? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    // Notes (a body present) open the editor; tasks/reminders/checklists open the detail
    // screen — see Item.isNote. Both screens are reached the same way from every list.
    val openItem: (Item) -> Unit = { item ->
        navController.navigate(if (item.isNote) Routes.editor(item.id) else Routes.detail(item.id))
    }

    // Screen 15's global overflow menu destinations — shared across every saved-view screen
    // that has one, so each composable() block below just spreads this instead of repeating
    // the same five lambdas.
    val onOpenSearch: () -> Unit = { navController.navigate(Routes.SEARCH) }
    val onOpenCollections: () -> Unit = { navController.navigate(Routes.COLLECTIONS) }
    val onOpenTemplates: () -> Unit = { navController.navigate(Routes.TEMPLATES) }
    val onOpenRecycleBin: () -> Unit = { navController.navigate(Routes.RECYCLE_BIN) }
    val onOpenSettings: () -> Unit = { navController.navigate(Routes.SETTINGS) }

    // A tapped reminder notification's deep link — already know isNote (AlarmReceiver put it
    // in the intent), so no need to load the item first just to pick a route.
    LaunchedEffect(deepLink) {
        deepLink?.let {
            navController.navigate(if (it.isNote) Routes.editor(it.itemId) else Routes.detail(it.itemId))
            onDeepLinkConsumed()
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenToday = { navController.navigate(Routes.TODAY) },
                onOpenScheduled = { navController.navigate(Routes.SCHEDULED) },
                onOpenStarred = { navController.navigate(Routes.STARRED) },
                // TODO(place reminders is deferred — see Features.placeReminders): no screen yet.
                onOpenPlace = {},
                onOpenNoAlert = { navController.navigate(Routes.NO_ALERT) },
                onOpenCompleted = { navController.navigate(Routes.COMPLETED) },
                onOpenItem = openItem,
                onOpenSearch = onOpenSearch,
                onOpenCollections = onOpenCollections,
                onOpenTemplates = onOpenTemplates,
                onOpenRecycleBin = onOpenRecycleBin,
                onOpenSettings = onOpenSettings,
            )
        }
        composable(Routes.TODAY) {
            TodayScreen(
                onBack = navController::popBackStack,
                onOpenItem = openItem,
                onOpenSearch = onOpenSearch,
                onOpenCollections = onOpenCollections,
                onOpenTemplates = onOpenTemplates,
                onOpenRecycleBin = onOpenRecycleBin,
                onOpenSettings = onOpenSettings,
            )
        }
        composable(Routes.SCHEDULED) {
            ScheduledScreen(
                onBack = navController::popBackStack,
                onOpenItem = openItem,
                onOpenSearch = onOpenSearch,
                onOpenCollections = onOpenCollections,
                onOpenTemplates = onOpenTemplates,
                onOpenRecycleBin = onOpenRecycleBin,
                onOpenSettings = onOpenSettings,
            )
        }
        composable(Routes.STARRED) {
            StarredScreen(
                onBack = navController::popBackStack,
                onOpenItem = openItem,
                onOpenSearch = onOpenSearch,
                onOpenCollections = onOpenCollections,
                onOpenTemplates = onOpenTemplates,
                onOpenRecycleBin = onOpenRecycleBin,
                onOpenSettings = onOpenSettings,
            )
        }
        composable(Routes.NO_ALERT) {
            NoAlertScreen(
                onBack = navController::popBackStack,
                onOpenItem = openItem,
                onOpenSearch = onOpenSearch,
                onOpenCollections = onOpenCollections,
                onOpenTemplates = onOpenTemplates,
                onOpenRecycleBin = onOpenRecycleBin,
                onOpenSettings = onOpenSettings,
            )
        }
        composable(Routes.COMPLETED) {
            CompletedScreen(onBack = navController::popBackStack, onOpenItem = openItem)
        }
        composable(Routes.SEARCH) {
            SearchScreen(onBack = navController::popBackStack, onOpenItem = openItem)
        }
        composable(Routes.COLLECTIONS) {
            CollectionsScreen(onBack = navController::popBackStack)
        }
        composable(Routes.TEMPLATES) {
            TemplatesScreen(onBack = navController::popBackStack)
        }
        composable(Routes.RECYCLE_BIN) {
            BinScreen(onBack = navController::popBackStack)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = navController::popBackStack,
                onOpenRecycleBin = onOpenRecycleBin,
            )
        }
        composable(
            route = Routes.DETAIL_PATTERN,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
        ) {
            DetailScreen(onBack = navController::popBackStack)
        }
        composable(
            route = Routes.EDITOR_PATTERN,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
        ) {
            EditorScreen(onBack = navController::popBackStack)
        }
    }
}
