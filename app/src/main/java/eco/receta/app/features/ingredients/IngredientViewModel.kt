package eco.receta.app.features.ingredients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eco.receta.app.data.model.Ingredient
import eco.receta.app.data.repository.IngredientRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class IngredientUiState(
    val query: String = "",
    val productos: List<Ingredient> = emptyList(),
    val productoSeleccionado: Ingredient? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,  // ← Para paginación infinita
    val error: String? = null
)

class IngredientViewModel : ViewModel() {

    private val repository = IngredientRepository()

    var uiState by mutableStateOf(IngredientUiState())
        private set

    // ═══════════════════════════════════════════════════════════
    // CACHÉ: Guardar productos cargados para no recargar
    // ═══════════════════════════════════════════════════════════
    private var productosCache: List<Ingredient> = emptyList()
    private var busquedaJob: Job? = null  // Para cancelar búsquedas anteriores

    init {
        cargarProductosIniciales()
    }

    // ═══════════════════════════════════════════════════════════
    // CARGAR productos iniciales
    // ═══════════════════════════════════════════════════════════
    private fun cargarProductosIniciales() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            // Si ya tenemos caché, usarla
            if (productosCache.isNotEmpty()) {
                uiState = uiState.copy(
                    productos = productosCache,
                    isLoading = false
                )
                return@launch
            }

            repository.getProductosPaginados(limit = 638)
                .onSuccess { productos ->
                    productosCache = productos  // ← Guardar en caché
                    uiState = uiState.copy(
                        productos = productos,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BÚSQUEDA con debounce (espera 300ms después de escribir)
    // ═══════════════════════════════════════════════════════════
    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query)

        // Cancelar búsqueda anterior
        busquedaJob?.cancel()

        if (query.isBlank()) {
            // Mostrar caché si borra la búsqueda
            uiState = uiState.copy(productos = productosCache)
            return
        }

        // Esperar 300ms antes de buscar (evita búsquedas mientras escribe)
        busquedaJob = viewModelScope.launch {
            delay(300)  // ← DEBOUNCE

            uiState = uiState.copy(isLoading = true)

            repository.buscarProductos(query, limit = 160)
                .onSuccess { productos ->
                    uiState = uiState.copy(
                        productos = productos,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSCAR por ID (para detalle)
    // ═══════════════════════════════════════════════════════════
    fun buscarProductoPorId(id: String): Ingredient? {
        // Primero buscar en caché
        val enCache = productosCache.find { it.id == id }
        if (enCache != null) return enCache

        // Si no está, buscar en los actuales
        return uiState.productos.find { it.id == id }
    }

    suspend fun getProductoByIdDesdeFirestore(id: String): Result<Ingredient> {
        // Primero buscar en caché
        val enCache = uiState.productos.find { it.id == id }
        if (enCache != null) {
            return Result.success(enCache)
        }

        // Si no está, cargar desde Firestore
        return repository.getProductoById(id)
    }
}