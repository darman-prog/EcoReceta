// eco/receta/app/features/home/HomeViewModel.kt

package eco.receta.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Patrón Loading / Success / Error ────────────────────────────────────────
sealed class RecipesState {
    object Loading : RecipesState()
    data class Success(val recipes: List<Recipe>) : RecipesState()
    data class Error(val message: String) : RecipesState()
}

data class HomeUiState(
    val recetasSistema: RecipesState = RecipesState.Loading,
    val recetasComunidad: RecipesState = RecipesState.Loading,
    val recetasPrivadas: RecipesState = RecipesState.Loading,
    val searchQuery: String = ""
)

class HomeViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Lista completa sin filtrar (para que la búsqueda funcione sobre todos)
    private var todasLasRecetas: List<Recipe> = emptyList()

    init {
        cargarRecetasSistema()
        cargarRecetasPrivadas()
    }

    // ── Consulta A: recetas oficiales del sistema ────────────────────────
    private fun cargarRecetasSistema() {
        viewModelScope.launch {
            repository.getRecetasSistema()
                .catch { e ->
                    _uiState.update {
                        it.copy(recetasSistema = RecipesState.Error(
                            e.message ?: "Error al cargar recetas"
                        ))
                    }
                }
                .collect { recetas ->
                    todasLasRecetas = recetas
                    _uiState.update {
                        it.copy(recetasSistema = RecipesState.Success(recetas))
                    }
                }
        }
    }

    // ── Consulta B: recetas privadas del usuario actual ──────────────────
    private fun cargarRecetasPrivadas() {
        viewModelScope.launch {
            repository.getRecetasPrivadas()
                .catch { e ->
                    _uiState.update {
                        it.copy(recetasPrivadas = RecipesState.Error(
                            e.message ?: "Error al cargar tu recetario"
                        ))
                    }
                }
                .collect { recetas ->
                    _uiState.update {
                        it.copy(recetasPrivadas = RecipesState.Success(recetas))
                    }
                }
        }
    }

    // ── Búsqueda en tiempo real ──────────────────────────────────────────
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            // Sin búsqueda: muestra todo
            _uiState.update {
                it.copy(recetasSistema = RecipesState.Success(todasLasRecetas))
            }
            return
        }

        // Filtra por nombre o categoría (insensible a mayúsculas)
        val filtradas = todasLasRecetas.filter { receta ->
            receta.nombre.contains(query, ignoreCase = true) ||
                    receta.categoria.contains(query, ignoreCase = true)
        }

        _uiState.update {
            it.copy(recetasSistema = RecipesState.Success(filtradas))
        }
    }
}