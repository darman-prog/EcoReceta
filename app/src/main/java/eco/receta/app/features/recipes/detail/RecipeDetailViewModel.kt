// eco/receta/app/features/recipes/detail/RecipeDetailViewModel.kt

package eco.receta.app.features.recipes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import eco.receta.app.data.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Estado del detalle ───────────────────────────────────────────────────────
sealed class DetailState {
    object Loading                       : DetailState()
    data class Success(val recipe: Recipe,
                       val precios: Map<String, Double> = emptyMap()) : DetailState()
    data class Error(val message: String): DetailState()
}

// Representa un ingrediente ya resuelto con su precio desde la colección productos
data class IngredienteResuelto(
    val nombre: String  = "",
    val cantidad: String = "",   // ej: "500g", "1 Unidad"
    val precio: Double  = 0.0,
    val imagen: String  = ""
)

data class RecipeDetailUiState(
    val detailState: DetailState               = DetailState.Loading,
    val ingredientesResueltos: List<IngredienteResuelto> = emptyList(),
    val costoCalculado: Double                 = 0.0
)

class RecipeDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    // ── Carga la receta y resuelve sus ingredientes ──────────────────────
    fun cargarReceta(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(detailState = DetailState.Loading) }
            try {
                // 1. Busca primero en recetas_publicas
                var doc = db.collection("recetas_publicas")
                    .document(recipeId)
                    .get()
                    .await()

                // 2. Si no está, busca en recetas privadas del usuario
                if (!doc.exists()) {
                    val uid = com.google.firebase.auth.FirebaseAuth
                        .getInstance().currentUser?.uid ?: ""
                    doc = db.collection("usuarios")
                        .document(uid)
                        .collection("recetas_privadas")
                        .document(recipeId)
                        .get()
                        .await()
                }

                if (!doc.exists()) {
                    _uiState.update {
                        it.copy(detailState = DetailState.Error("Receta no encontrada."))
                    }
                    return@launch
                }

                val receta = doc.toObject<Recipe>()!!.copy(id = doc.id)

                // 3. Resuelve los ingredientes cruzando con colección productos
                val ingredientesResueltos = resolverIngredientes(receta.ingredientes_ids)
                val costoCalculado = ingredientesResueltos.sumOf { it.precio }

                _uiState.update {
                    it.copy(
                        detailState           = DetailState.Success(receta),
                        ingredientesResueltos = ingredientesResueltos,
                        // Si hay costo calculado úsalo, si no usa el del documento
                        costoCalculado        = if (costoCalculado > 0)
                            costoCalculado
                        else receta.costoTotal
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(detailState = DetailState.Error(
                        e.message ?: "Error al cargar la receta."
                    ))
                }
            }
        }
    }

    // ── Cruza ingredientes_ids con la colección productos ────────────────
    private suspend fun resolverIngredientes(
        ids: List<String>
    ): List<IngredienteResuelto> {
        if (ids.isEmpty()) return emptyList()

        return ids.mapNotNull { nombreProducto ->
            try {
                // ingredientes_ids contiene el campo `producto` de la colección
                val snapshot = db.collection("productos")
                    .whereEqualTo("producto", nombreProducto)
                    .limit(1)
                    .get()
                    .await()

                val doc = snapshot.documents.firstOrNull() ?: return@mapNotNull null
                val data = doc.data ?: return@mapNotNull null

                // Toma el precio más bajo entre todas las tiendas
                @Suppress("UNCHECKED_CAST")
                val precios = data["precios"] as? List<Map<String, Any>> ?: emptyList()
                val precioMinimo = precios
                    .mapNotNull { (it["precio"] as? Number)?.toDouble() } // Convertimos a Double aquí
                    .minOrNull() ?: 0.0
                IngredienteResuelto(
                    nombre   = data["producto"] as? String ?: "",
                    cantidad = "${data["tamaño"] ?: ""} ${data["unidad"] ?: ""}".trim(),
                    precio   = precioMinimo,
                    imagen   = data["imagen"] as? String ?: ""
                )
            } catch (e: Exception) {
                null  // si un ingrediente falla, lo omite sin crashear
            }
        }
    }
}