package eco.receta.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import eco.receta.app.data.model.Ingredient
import eco.receta.app.features.auth.LoginScreen
import eco.receta.app.features.auth.RegisterScreen
import eco.receta.app.features.explore.ExploreScreen
import eco.receta.app.features.home.HomeScreen
import eco.receta.app.features.ingredients.IngredientDetailScreen
import eco.receta.app.features.ingredients.IngredientListScreen
import eco.receta.app.features.ingredients.IngredientViewModel
import eco.receta.app.features.profile.ProfileScreen
import eco.receta.app.features.recipes.create.CreateRecipeScreen
import eco.receta.app.features.recipes.create.IngredienteSeleccionado
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
                onNavigateToExplore = { navController.navigate(Routes.EXPLORE) },
                onNavigateToCreate = { navController.navigate(Routes.CREATE) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onRecipeClick = { id ->
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
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EXPLORE) {
            ExploreScreen(
                onNavigateToHome = { navController.navigate(Routes.HOME) },
                onNavigateToCreate = { navController.navigate(Routes.CREATE) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onRecipeClick = { id ->
                    navController.navigate("${Routes.RECIPE_DETAIL}/$id")
                }
            )
        }

        composable(Routes.CREATE) {
            CreateRecipeScreen(
                navController = navController,
                onNavigateToRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.CREATE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                // Ir a buscar ingredientes
                onNavigateToAddIngredients = {
                    navController.navigate(Routes.INGREDIENT_LIST)
                },
                // Volver atrás después de guardar
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        // ═══════════════════════════════════════════════════════════
        // PROFILE — PERFIL CON STATS Y BADGES (COMPLETO)
        // ═══════════════════════════════════════════════════════════
        composable(Routes.PROFILE) {
            ProfileScreen(
                navController = navController  // ← Solo esto
            )
        }
        composable(Routes.INGREDIENT_LIST) {
            IngredientListScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onIngredientSelected = { ingrediente ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("ingrediente_seleccionado", ingrediente)
                    navController.popBackStack()
                }
            )
        }

        // ═══════════════════════════════════════════════════════════
        // NUEVO: Detalle de ingrediente
        // ═══════════════════════════════════════════════════════════
        composable("ingredient_detail/{ingredientId}") { backStackEntry ->
            val ingredientId = backStackEntry.arguments?.getString("ingredientId") ?: ""
            val ingredientViewModel: IngredientViewModel = viewModel()

            var ingredient by remember { mutableStateOf<Ingredient?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(ingredientId) {
                isLoading = true
                ingredientViewModel.getProductoByIdDesdeFirestore(ingredientId)
                    .onSuccess { ingredient = it }
                isLoading = false
            }

            when {
                isLoading -> { /* loading */ }
                ingredient == null -> { /* error */ }
                else -> {
                    IngredientDetailScreen(
                        ingredient = ingredient!!,
                        onNavigateBack = { navController.popBackStack() },
                        onAddToRecipe = {
                            // Crear aquí en el NavHost
                            ingredient?.let { ing ->  // ← 'ing' es garantizado no-null aquí
                                val precioMasBarato = ing.precios.minOfOrNull { it.precio } ?: 0.0
                                val seleccionado = IngredienteSeleccionado(
                                    productoId = ing.id,
                                    nombre = ing.producto,
                                    precio = precioMasBarato,
                                    cantidad = "${ing.tamaño} ${ing.unidad}",
                                    unidad = ing.unidad
                                )
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("ingrediente_seleccionado", seleccionado)
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }
        }
    }
}