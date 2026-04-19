package pt.ms.myshare.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.ms.myshare.presentation.ui.onboarding.OnboardingEntryRoute
import pt.ms.myshare.presentation.ui.home.HomeRoute
import pt.ms.myshare.presentation.ui.home.GoalAddRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") { OnboardingEntryRoute(parentNavController = navController) }
        composable("home") { HomeRoute(navController = navController) }
        composable("add_goal") { GoalAddRoute(navController = navController) }
    }
}
