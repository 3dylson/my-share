package pt.ms.myshare.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pt.ms.myshare.presentation.ui.home.HomeRoute
import pt.ms.myshare.presentation.ui.home.GoalAddRoute
import pt.ms.myshare.presentation.ui.home.RuleAddRoute
import pt.ms.myshare.presentation.ui.onboarding.OnboardingEntryRoute

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    onManageAdsConsent: () -> Unit = {},
    adsConsentManager: pt.ms.myshare.presentation.ui.ads.AdsConsentManager? = null
) {
    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingEntryRoute(parentNavController = navController)
        }
        composable("home") {
            HomeRoute(
                navController = navController,
                onManageAdsConsent = onManageAdsConsent,
                adsConsentManager = adsConsentManager
            )
        }
        composable(
            route = "add_rule?ruleId={ruleId}",
            arguments = listOf(navArgument("ruleId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            RuleAddRoute(navController = navController)
        }
        composable(
            route = "add_goal?goalId={goalId}",
            arguments = listOf(navArgument("goalId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            GoalAddRoute(navController = navController)
        }
    }
}
