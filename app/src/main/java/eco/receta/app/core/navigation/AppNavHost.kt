package eco.receta.app.core.navigation


import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.google.gson.Gson
import eco.receta.app.features.recipes.create.CreateRecipeViewModel
import eco.receta.app.features.splash.SplashScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    startRoute: String,
    auth: FirebaseAuth              // ← recibe auth
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.SPLASH  // ← ya no lo calcula aquí
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    // Después del splash decide a dónde ir
                    val destination = if (auth.currentUser != null) {
                        Routes.HOME
                    } else {
                        Routes.LOGIN
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true } // elimina splash del backstack
                    }
                }
            )
        }

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
                    val json = Gson().toJson(ingrediente)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("ingrediente_seleccionado", json)  // guarda como String
                    navController.popBackStack()
                }
            )
        }

        // ═══════════════════════════════════════════════════════════
        // NUEVO: Detalle de ingrediente
        // ═══════════════════════════════════════════════════════════
        composable(Routes.CREATE) { backStackEntry ->
            val viewModel: CreateRecipeViewModel = viewModel()

            // 🔍 LOG 5: Verificar que se lee el JSON
            val keys = backStackEntry.savedStateHandle.keys()
            Log.d("FLUJO", "5. CREATE keys: $keys")

            val json = backStackEntry.savedStateHandle.get<String>("ingrediente_seleccionado")
            Log.d("FLUJO", "6. JSON leído: $json")

            json?.let {
                try {
                    val ingrediente = Gson().fromJson(it, IngredienteSeleccionado::class.java)
                    Log.d("FLUJO", "7. Ingrediente parseado: ${ingrediente.nombre}")

                    viewModel.addIngrediente(ingrediente)
                    Log.d("FLUJO", "8. Llamado addIngrediente")

                    backStackEntry.savedStateHandle.remove<String>("ingrediente_seleccionado")
                    Log.d("FLUJO", "9. Limpiado savedStateHandle")
                } catch (e: Exception) {
                    Log.e("FLUJO", "ERROR parseando: ${e.message}")
                    e.printStackTrace()
                }
            }

            CreateRecipeScreen(
                viewModel = viewModel,
                navController = navController,
                onNavigateToRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.CREATE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToAddIngredients = {
                    navController.navigate(Routes.INGREDIENT_LIST)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("${Routes.INGREDIENT_DETAIL}/{ingredientId}") { backStackEntry ->
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
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ingredient == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Ingrediente no encontrado")
                    }
                }
                else -> {
                    IngredientDetailScreen(
                        ingredient = ingredient!!,
                        onNavigateBack = { navController.popBackStack() },
                        onAddToRecipe = { ing ->  // ← RECIBE Ingredient aquí
                            val precioMasBarato = ing.precios.minOfOrNull { it.precio } ?: 0.0

                            val seleccionado = IngredienteSeleccionado(
                                productoId = ing.id,
                                nombre = ing.producto,
                                precio = precioMasBarato,
                                cantidad = "${ing.tamaño} ${ing.unidad}",
                                unidad = ing.unidad
                            )

                            val json = Gson().toJson(seleccionado)


                            Log.d("FLUJO", "1. JSON creado: $json")
                            Log.d("FLUJO", "1. Ingrediente: ${seleccionado.nombre}")

                            val previousEntry = navController.previousBackStackEntry
                            Log.d("FLUJO", "2. Previous entry: ${previousEntry?.destination?.route}")

                            previousEntry?.savedStateHandle?.set("ingrediente_seleccionado", json)
                            Log.d("FLUJO", "3. Guardado en savedStateHandle")

                            navController.popBackStack()
                            Log.d("FLUJO", "4. popBackStack ejecutado")

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("ingrediente_seleccionado", json)

                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}