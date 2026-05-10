// eco/receta/app/features/home/HomeViewModel.kt

package eco.receta.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val recetasDestacadas: List<Recipe> = emptyList(), // SISTEMA
    val miRecetario: List<Recipe> = emptyList(),       // privadas del usuario
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { cargarDatos() }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // ── Fusión de Consulta A + Consulta B ────────────────────────
            // combine() espera ambos flows y emite cada vez que uno cambia
            combine(
                repository.getRecetasSistema(),   // Consulta A
                repository.getRecetasPrivadas()   // Consulta B
            ) { sistema, privadas ->
                HomeUiState(
                    recetasDestacadas = sistema,
                    miRecetario       = privadas,
                    isLoading         = false
                )
            }.catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }.collect { nuevoEstado ->
                _uiState.value = nuevoEstado
            }
        }
    }
}