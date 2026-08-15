package com.stackpointer.list.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stackpointer.list.ui.screens.completed.CompletedScreen
import com.stackpointer.list.ui.screens.home.HomeScreen
import com.stackpointer.list.ui.screens.noalert.NoAlertScreen
import com.stackpointer.list.ui.screens.scheduled.ScheduledScreen
import com.stackpointer.list.ui.screens.starred.StarredScreen
import com.stackpointer.list.ui.screens.today.TodayScreen

@Composable
fun DigitalListNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
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
                // TODO(M6): open the detail screen once it exists.
                onOpenItem = {},
            )
        }
        composable(Routes.TODAY) {
            TodayScreen(
                onBack = navController::popBackStack,
                onOpenItem = {},
            )
        }
        composable(Routes.SCHEDULED) {
            ScheduledScreen(
                onBack = navController::popBackStack,
                onOpenItem = {},
            )
        }
        composable(Routes.STARRED) {
            StarredScreen(
                onBack = navController::popBackStack,
                onOpenItem = {},
            )
        }
        composable(Routes.NO_ALERT) {
            NoAlertScreen(
                onBack = navController::popBackStack,
                onOpenItem = {},
            )
        }
        composable(Routes.COMPLETED) {
            CompletedScreen(
                onBack = navController::popBackStack,
                onOpenItem = {},
            )
        }
    }
}
