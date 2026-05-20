package eco.receta.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class RecipesState {
    object Loading : RecipesState()
    data class Success(val recipes: List<Recipe>) : RecipesState()
    data class Error(val message: String) : RecipesState()
}

data class HomeUiState(
    val recetasSistema: RecipesState = RecipesState.Loading,   // Admin
    val misRecetas: RecipesState = RecipesState.Loading,       // Privadas + públicas del usuario
    val searchQuery: String = ""
)

class HomeViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var recetasSistemaCache: List<Recipe> = emptyList()
    private var misRecetasCache: List<Recipe> = emptyList()

    init {
        cargarRecetasSistema()
        cargarMisRecetas()
    }

    // ═══════════════════════════════════════════════════════════
    // RECETAS DEL SISTEMA (admin)
    // ═══════════════════════════════════════════════════════════
    private fun cargarRecetasSistema() {
        viewModelScope.launch {
            _uiState.update { it.copy(recetasSistema = RecipesState.Loading) }

            try {
                val recetas = repository.getRecetasSistema()
                recetasSistemaCache = recetas

                _uiState.update {
                    it.copy(recetasSistema = RecipesState.Success(recetas))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(recetasSistema = RecipesState.Error(
                        e.message ?: "Error al cargar recetas"
                    ))
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MIS RECETAS (privadas + públicas del usuario)
    // ═══════════════════════════════════════════════════════════
    private fun cargarMisRecetas() {
        viewModelScope.launch {
            _uiState.update { it.copy(misRecetas = RecipesState.Loading) }

            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("No autenticado")

                val recetas = repository.getRecetasPorUsuario(uid)
                misRecetasCache = recetas

                _uiState.update {
                    it.copy(misRecetas = RecipesState.Success(recetas))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(misRecetas = RecipesState.Error(
                        e.message ?: "Error al cargar tu recetario"
                    ))
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    recetasSistema = RecipesState.Success(recetasSistemaCache),
                    misRecetas = RecipesState.Success(misRecetasCache)
                )
            }
            return
        }

        val filtradasSistema = recetasSistemaCache.filter { receta ->
            receta.nombre.contains(query, ignoreCase = true) ||
                    receta.categoria.contains(query, ignoreCase = true)
        }

        val filtradasMias = misRecetasCache.filter { receta ->
            receta.nombre.contains(query, ignoreCase = true) ||
                    receta.categoria.contains(query, ignoreCase = true)
        }

        _uiState.update {
            it.copy(
                recetasSistema = RecipesState.Success(filtradasSistema),
                misRecetas = RecipesState.Success(filtradasMias)
            )
        }
    }
}