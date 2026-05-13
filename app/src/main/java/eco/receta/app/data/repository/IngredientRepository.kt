package eco.receta.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import eco.receta.app.data.model.Ingredient
import kotlinx.coroutines.tasks.await

class IngredientRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productosRef = db.collection("productosEcoReceta")

    // ═══════════════════════════════════════════════════════════
    // CARGAR PRIMEROS 20 productos (paginación)
    // ═══════════════════════════════════════════════════════════
    suspend fun getProductosPaginados(limit: Long = 20): Result<List<Ingredient>> = try {
        val snapshot = productosRef
            .limit(limit)           // ← Solo carga 20, no todos
            .get()
            .await()

        val productos = snapshot.documents.mapNotNull { doc ->
            doc.toObject<Ingredient>()?.copy(id = doc.id)
        }

        Result.success(productos)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ═══════════════════════════════════════════════════════════
    // BÚSQUEDA con límite (más rápida)
    // ═══════════════════════════════════════════════════════════
    suspend fun buscarProductos(query: String, limit: Long = 20): Result<List<Ingredient>> {
        return try {
            if (query.isBlank()) {
                return getProductosPaginados(limit)
            }

            // Buscar solo los primeros 20 que coincidan
            val snapshot = productosRef
                .orderBy("producto")           // Requiere índice, si falla quitar
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(limit)
                .get()
                .await()

            val productos = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Ingredient>()?.copy(id = doc.id)
            }

            Result.success(productos)
        } catch (e: Exception) {
            // Si falla el orderBy (falta índice), hacer búsqueda simple con límite
            buscarProductosSimple(query, limit)
        }
    }

    // Búsqueda sin índice (más lenta pero no requiere configuración)
    private suspend fun buscarProductosSimple(query: String, limit: Long): Result<List<Ingredient>> = try {
        val snapshot = productosRef
            .limit(100)  // Cargar solo 100 para filtrar localmente
            .get()
            .await()

        val filtrados = snapshot.documents
            .mapNotNull { doc -> doc.toObject<Ingredient>()?.copy(id = doc.id) }
            .filter { it.producto.contains(query, ignoreCase = true) }
            .take(limit.toInt())

        Result.success(filtrados)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Obtener por ID (para detalle)
    suspend fun getProductoById(id: String): Result<Ingredient> = try {
        val doc = productosRef.document(id).get().await()
        val ingredient = doc.toObject<Ingredient>()?.copy(id = doc.id)

        if (ingredient != null) {
            Result.success(ingredient)
        } else {
            Result.failure(Exception("Producto no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}