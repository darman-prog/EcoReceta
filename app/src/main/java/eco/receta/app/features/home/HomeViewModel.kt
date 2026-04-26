package eco.receta.app.features.home

import androidx.lifecycle.ViewModel
import eco.receta.app.data.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val featuredRecipe: Recipe? = null,      // tarjeta grande superior
    val localRecipes: List<Recipe> = emptyList(), // sección "Explora Sabores Locales"
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // TODO: conectar con búsqueda real en Firestore
    }

    // ── Datos de prueba — reemplazar por llamada a RecipeRepository ──────
    // Cuando tengas Firebase listo, esto será:
    // viewModelScope.launch {
    //     recipeRepository.getPublicRecipes().collect { recipes ->
    //         _uiState.update {
    //             it.copy(
    //                 featuredRecipe = recipes.firstOrNull(),
    //                 localRecipes   = recipes.drop(1)
    //             )
    //         }
    //     }
    // }
    private fun loadRecipes() {
        _uiState.update { it.copy(isLoading = true) }

        val mockRecipes = listOf(
            Recipe(
                id = "1",
                name = "Bandeja Paisa Tradicional",
                timeMinutes = 45,
                level = "Fácil",
                totalCost = 12500.0,
                region = "ANTIOQUIA",
                category = "TRADICIONAL",
                isPublic = true,
                authorName = "Carlos Andrés Mejía"
            ),
            Recipe(
                id = "2",
                name = "Ajiaco Santafereño",
                timeMinutes = 60,
                level = "Medio",
                totalCost = 18000.0,
                region = "BOGOTÁ",
                category = "GOURMET",
                isPublic = true,
                authorName = "María Torres"
            ),
            Recipe(
                id = "3",
                name = "Arepa de Chócolo",
                timeMinutes = 20,
                level = "Fácil",
                totalCost = 6500.0,
                region = "ANTIOQUIA",
                category = "SNACK",
                isPublic = true,
                authorName = "Pedro Gómez"
            ),
            Recipe(
                id = "4",
                name = "Sancocho de Gallina",
                timeMinutes = 90,
                level = "Medio",
                totalCost = 22000.0,
                region = "TOLIMA",
                category = "FAMILIAR",
                isPublic = true,
                authorName = "Ana Ruiz"
            )
        )

        _uiState.update {
            it.copy(
                featuredRecipe = mockRecipes.first(),
                localRecipes   = mockRecipes.drop(1),
                isLoading      = false
            )
        }
    }
}