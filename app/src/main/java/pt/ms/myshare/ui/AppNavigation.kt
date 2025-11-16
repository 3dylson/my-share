package pt.ms.myshare.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.ms.myshare.ui.edit_profile.EditProfileRoute
import pt.ms.myshare.ui.home.HomeRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeRoute(navController = navController) }
        composable("edit_profile") { EditProfileRoute(navController = navController) }
    }
}
