package eco.receta.app.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.repository.RecipeRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class ExploreState {
    object Loading : ExploreState()
    data class Success(val recipes: List<Recipe>) : ExploreState()
    data class Error(val message: String) : ExploreState()
}

// ✅ 1. CORRECCIÓN: Agregamos las dos variables que la UI espera encontrar
data class ExploreUiState(
    val recetasSistema: ExploreState = ExploreState.Loading,
    val recetasComunidad: ExploreState = ExploreState.Loading,
    val searchQuery: String = "",
    val categoriaActiva: String = "Todas"
)

class ExploreViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    // Guardamos las listas originales para poder filtrarlas luego
    private var todasSistema: List<Recipe> = emptyList()
    private var todasComunidad: List<Recipe> = emptyList()

    val categorias = listOf("Todas", "ACOMPAÑAMIENTOS", "SOPAS", "BEBIDAS FRÍAS", "ALMUERZOS")

    init { cargarRecetas() }

    private fun cargarRecetas() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                recetasSistema = ExploreState.Loading,
                recetasComunidad = ExploreState.Loading
            ) }

            try {
                // Obtenemos ambas colecciones por separado
                val sistema = repository.getRecetasSistema()
                val comunidad = repository.getRecetasComunidad()

                todasSistema = sistema
                todasComunidad = comunidad

                // ✅ 2. CORRECCIÓN: Actualizamos ambos estados por separado
                _uiState.update {
                    it.copy(
                        recetasSistema = ExploreState.Success(sistema),
                        recetasComunidad = ExploreState.Success(comunidad)
                    )
                }

            } catch (e: Exception) {
                val errorState = ExploreState.Error(e.message ?: "Error al cargar")
                _uiState.update {
                    it.copy(recetasSistema = errorState, recetasComunidad = errorState)
                }
            }
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
        // Filtramos ambas listas
        val filtradasSistema = filtrarLista(todasSistema, query, categoria)
        val filtradasComunidad = filtrarLista(todasComunidad, query, categoria)

        _uiState.update {
            it.copy(
                recetasSistema = ExploreState.Success(filtradasSistema),
                recetasComunidad = ExploreState.Success(filtradasComunidad)
            )
        }
    }

    // Función auxiliar para no repetir la lógica de filtro
    private fun filtrarLista(lista: List<Recipe>, query: String, categoria: String): List<Recipe> {
        var filtradas = lista

        if (categoria != "Todas") {
            filtradas = filtradas.filter {
                it.categoria.equals(categoria, ignoreCase = true)
            }
        }

        if (query.isNotBlank()) {
            filtradas = filtradas.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.categoria.contains(query, ignoreCase = true)
            }
        }
        return filtradas
    }
}