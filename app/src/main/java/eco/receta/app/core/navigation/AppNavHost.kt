package eco.receta.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import eco.receta.app.features.auth.LoginScreen
import eco.receta.app.features.auth.RegisterScreen
import eco.receta.app.features.home.HomeScreen

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        // ── Login ────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // ── Registro ─────────────────────────────────────────────────────
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToExplore  = { navController.navigate(Routes.EXPLORE) },
                onNavigateToCreate   = { navController.navigate(Routes.CREATE) },
                onNavigateToProfile  = { navController.navigate(Routes.PROFILE) },
                onRecipeClick        = { recipeId ->
                    navController.navigate("${Routes.RECIPE_DETAIL}/$recipeId")
                }
            )
        }
    }
}