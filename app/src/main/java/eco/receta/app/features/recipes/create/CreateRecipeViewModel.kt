package eco.receta.app.features.recipes.create

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.model.TipoOrigen
import eco.receta.app.data.model.Visibilidad
import eco.receta.app.data.repository.RecipeRepository
import kotlinx.coroutines.launch




data class CreateUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val imagenUri: Uri? = null,
    val ingredientesSeleccionados: List<IngredienteSeleccionado> = emptyList(),
    val visibilidad: Visibilidad = Visibilidad.PRIVADA,  // ← Usa el de data.model
    val tiempoMinutos: String = "",
    val porciones: Int = 1,
    val nivel: String = "Fácil",
    val categoria: String = "ALMUERZOS",  // ← NUEVO con valor por defecto
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)


data class IngredienteSeleccionado(
    val productoId: String,
    val nombre: String,
    val precio: Double,
    val cantidad: String,
    val unidad: String
)

class CreateRecipeViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()

    var uiState by mutableStateOf(CreateUiState())
        private set

    fun onNombreChange(value: String) { uiState = uiState.copy(nombre = value) }
    fun onDescripcionChange(value: String) { uiState = uiState.copy(descripcion = value) }
    fun onImagenSelected(uri: Uri) { uiState = uiState.copy(imagenUri = uri) }
    fun onTiempoChange(value: String) { uiState = uiState.copy(tiempoMinutos = value) }


    fun onNivelChange(value: String) { uiState = uiState.copy(nivel = value) }

    // ═══════════════════════════════════════════════════════════
    // AHORA USA Visibilidad de data.model (eco.receta.app.data.model.Visibilidad)
    // ═══════════════════════════════════════════════════════════
    fun onVisibilidadChange(visibilidad: Visibilidad) {
        uiState = uiState.copy(visibilidad = visibilidad)
    }

    fun onCategoriaChange(value: String) { uiState = uiState.copy(categoria = value) }
    fun addIngrediente(ingrediente: IngredienteSeleccionado) {
        val existe = uiState.ingredientesSeleccionados.any { it.productoId == ingrediente.productoId }

        Log.d("FLUJO", "10. addIngrediente llamado: ${ingrediente.nombre}")
        Log.d("FLUJO", "10. Ya existe: $existe")
        Log.d("FLUJO", "10. Lista actual: ${uiState.ingredientesSeleccionados.map { it.nombre }}")

        if (!existe) {
            uiState = uiState.copy(
                ingredientesSeleccionados = uiState.ingredientesSeleccionados + ingrediente
            )
            Log.d("FLUJO", "11. NUEVA lista: ${uiState.ingredientesSeleccionados.map { it.nombre }}")
        }
    }

    fun removeIngrediente(productoId: String) {
        uiState = uiState.copy(
            ingredientesSeleccionados = uiState.ingredientesSeleccionados
                .filter { it.productoId != productoId }
        )
    }

    fun guardarReceta() {

        if (uiState.nombre.isBlank()) {
            uiState = uiState.copy(error = "El nombre del plato es obligatorio")
            return
        }

        val tiempoInt = uiState.tiempoMinutos.toIntOrNull()
        if (tiempoInt == null || tiempoInt <= 0) {
            uiState = uiState.copy(error = "Ingresa un tiempo válido en minutos")
            return
        }

        if (uiState.porciones <= 0) {
            uiState = uiState.copy(error = "Ingresa al menos 1 porción")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val costoTotal = uiState.ingredientesSeleccionados.sumOf { it.precio }

            val recipe = Recipe(
                nombre = uiState.nombre,
                descripcion = uiState.descripcion,
                imageUrl = uiState.imagenUri?.toString() ?: "",
                tiempoMinutos = tiempoInt,
                porciones = uiState.porciones,
                nivel = uiState.nivel,
                costoTotal = costoTotal,
                tipoOrigen = TipoOrigen.USUARIO,
                visibilidad = uiState.visibilidad,  // ← Mismo tipo, no necesita conversión
                autorID = "",
                categoria   = uiState.categoria,   // ← NUEVO
                autorNombre = "",
                esOficial = false,
                ingredientes_ids = uiState.ingredientesSeleccionados.map { it.nombre },
                creadoEn = System.currentTimeMillis(),
                imagen = uiState.imagenUri?.toString() ?: ""
            )


            try {
                recipeRepository.guardarReceta(recipe)
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearSuccess() { uiState = uiState.copy(isSuccess = false) }
    fun clearError() { uiState = uiState.copy(error = null) }
}