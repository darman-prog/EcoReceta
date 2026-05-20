package eco.receta.app.features.recipes.create

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.model.TipoOrigen
import eco.receta.app.data.model.Visibilidad
import eco.receta.app.data.repository.RecipeRepository
import eco.receta.app.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CreateUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val imagenUri: Uri? = null,
    val ingredientesSeleccionados: List<IngredienteSeleccionado> = emptyList(),
    val visibilidad: Visibilidad = Visibilidad.PRIVADA,
    val tiempoMinutos: String = "",
    val porciones: Int = 1,
    val nivel: String = "Fácil",
    val categoria: String = "ALMUERZOS",
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
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    var uiState by mutableStateOf(CreateUiState())
        private set

    fun onNombreChange(value: String) { uiState = uiState.copy(nombre = value) }
    fun onDescripcionChange(value: String) { uiState = uiState.copy(descripcion = value) }
    fun onImagenSelected(uri: Uri) { uiState = uiState.copy(imagenUri = uri) }
    fun onTiempoChange(value: String) { uiState = uiState.copy(tiempoMinutos = value) }
    fun onNivelChange(value: String) { uiState = uiState.copy(nivel = value) }
    fun onVisibilidadChange(visibilidad: Visibilidad) {
        uiState = uiState.copy(visibilidad = visibilidad)
    }

    fun incrementarPorciones() {
        uiState = uiState.copy(porciones = uiState.porciones + 1)
    }

    fun decrementarPorciones() {
        if (uiState.porciones > 1) {
            uiState = uiState.copy(porciones = uiState.porciones - 1)
        }
    }

    fun onCategoriaChange(value: String) { uiState = uiState.copy(categoria = value) }

    fun addIngrediente(ingrediente: IngredienteSeleccionado) {
        val existe = uiState.ingredientesSeleccionados.any { it.productoId == ingrediente.productoId }

        if (!existe) {
            uiState = uiState.copy(
                ingredientesSeleccionados = uiState.ingredientesSeleccionados + ingrediente
            )
        }
    }

    fun removeIngrediente(productoId: String) {
        uiState = uiState.copy(
            ingredientesSeleccionados = uiState.ingredientesSeleccionados
                .filter { it.productoId != productoId }
        )
    }

    private suspend fun getAutorNombre(uid: String): String {
        val snap = FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .get()
            .await()

        return snap.getString("nombreCompleto")
            ?: snap.getString("nombre")
            ?: "Usuario"
    }

    fun guardarReceta() {
        if (uiState.nombre.isBlank()) {
            uiState = uiState.copy(error = "El nombre del plato es obligatorio")
            return
        }

        val tiempoInt = uiState.tiempoMinutos.toIntOrNull()
        if (tiempoInt == null || tiempoInt <= 0) {
            uiState = uiState.copy(error = "Ingresa un tiempo válido")
            return
        }

        val imagenUri = uiState.imagenUri
        if (imagenUri == null) {
            uiState = uiState.copy(error = "Debes seleccionar una imagen")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val uid = auth.currentUser?.uid
                ?: run {
                    uiState = uiState.copy(error = "Usuario no autenticado")
                    return@launch
                }

            val autorNombre = getAutorNombre(uid)

            try {
                recipeRepository.crearRecetaConImagen(
                    recipe = Recipe(
                        nombre = uiState.nombre,
                        descripcion = uiState.descripcion,
                        tiempoMinutos = tiempoInt,
                        porciones = uiState.porciones,
                        nivel = uiState.nivel,
                        categoria = uiState.categoria,
                        costoTotal = uiState.ingredientesSeleccionados.sumOf { it.precio },
                        tipoOrigen = TipoOrigen.USUARIO,
                        visibilidad = uiState.visibilidad,
                        autorID = uid,
                        autorNombre = autorNombre,
                        esOficial = false,
                        ingredientes_ids = uiState.ingredientesSeleccionados.map { it.nombre },
                        creadoEn = System.currentTimeMillis()
                    ),
                    imageUri = imagenUri
                )

                val recetasTotales = recipeRepository.getRecetasPorUsuario(uid).size

                userRepository.actualizarBadge(uid, recetasTotales)
                userRepository.actualizarContadorRecetas(uid, recetasTotales)

                Log.d("CreateRecipe", "Receta guardada. Total recetas: $recetasTotales")

                uiState = uiState.copy(isLoading = false, isSuccess = true)

            } catch (e: Exception) {
                Log.e("CreateRecipe", "Error: ${e.message}")
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Error al guardar la receta"
                )
            }
        }
    }

    fun clearSuccess() { uiState = uiState.copy(isSuccess = false) }
    fun clearError() { uiState = uiState.copy(error = null) }
}