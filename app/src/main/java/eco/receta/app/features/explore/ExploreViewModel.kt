package eco.receta.app.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ExploreState {
    object Loading                          : ExploreState()
    data class Success(val recipes: List<Recipe>) : ExploreState()
    data class Error(val message: String)   : ExploreState()
}

data class ExploreUiState(
    val destacada: ExploreState       = ExploreState.Loading,
    val populares: ExploreState       = ExploreState.Loading,
    val searchQuery: String           = "",
    val categoriaActiva: String       = "Todas"
)

class ExploreViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    // Lista completa sin filtrar
    private var todasLasRecetas: List<Recipe> = emptyList()

    val categorias = listOf("Todas", "ACOMPAÑAMIENTOS", "SOPAS", "BEBIDAS FRÍAS","ALMUERZOS")

    init { cargarRecetas() }

    private fun cargarRecetas() {
        viewModelScope.launch {
            // Carga recetas públicas de comunidad + sistema
            combine(
                repository.getRecetasSistema(),
                repository.getRecetasComunidad()
            ) { sistema, comunidad ->
                sistema + comunidad
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            destacada = ExploreState.Error(e.message ?: "Error al cargar"),
                            populares = ExploreState.Error(e.message ?: "Error al cargar")
                        )
                    }
                }
                .collect { recetas ->
                    todasLasRecetas = recetas
                    actualizarEstado(recetas)
                }
        }
    }

    private fun actualizarEstado(recetas: List<Recipe>) {
        _uiState.update {
            it.copy(
                // Primera receta → tarjeta destacada grande
                destacada = if (recetas.isEmpty()) ExploreState.Success(emptyList())
                else ExploreState.Success(listOf(recetas.first())),
                // El resto → lista "Populares"
                populares = ExploreState.Success(recetas.drop(1))
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        aplicarFiltros(query, _uiState.value.categoriaActiva)
    }

    fun onCategoriaSelected(categoria: String) {
        _uiState.update { it.copy(categoriaActiva = categoria) }
        aplicarFiltros(_uiState.value.searchQuery, categoria)
    }

    private fun aplicarFiltros(query: String, categoria: String) {
        var filtradas = todasLasRecetas

        // Filtro por categoría
        if (categoria != "Todas") {
            filtradas = filtradas.filter {
                it.categoria.equals(categoria, ignoreCase = true)
            }
        }

        // Filtro por búsqueda
        if (query.isNotBlank()) {
            filtradas = filtradas.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.categoria.contains(query, ignoreCase = true) ||
                        it.region.contains(query, ignoreCase = true)
            }
        }

        actualizarEstado(filtradas)
    }
}