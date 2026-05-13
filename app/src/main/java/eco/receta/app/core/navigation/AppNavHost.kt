package eco.receta.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import eco.receta.app.features.auth.LoginScreen
import eco.receta.app.features.auth.RegisterScreen
import eco.receta.app.features.explore.ExploreScreen
import eco.receta.app.features.home.HomeScreen
import eco.receta.app.features.recipes.detail.RecipeDetailScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startRoute: String              // ← recibe "home" o "login" desde MainActivity
) {
    NavHost(
        navController    = navController,
        startDestination = startRoute  // ← ya no lo calcula aquí
    ) {

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
                onRecipeClick        = { id ->
                    navController.navigate("recipe_detail/$id")
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }


        // ── DETALLE DE RECETA ─────────────────────────────────────────────
        composable(
            route = "recipe_detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
            RecipeDetailScreen(
                recipeId       = recipeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EXPLORE) {
            ExploreScreen(
                onNavigateToHome    = { navController.popBackStack() },
                onNavigateToCreate  = { navController.navigate(Routes.CREATE) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onRecipeClick       = { id ->
                    navController.navigate("${Routes.RECIPE_DETAIL}/$id")
                }
            )
        }

        composable(Routes.CREATE)   { }
        composable(Routes.PROFILE)  { }
    }
}