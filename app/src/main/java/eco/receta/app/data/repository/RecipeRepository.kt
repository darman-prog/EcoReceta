package eco.receta.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.model.TipoOrigen
import eco.receta.app.data.model.Visibilidad
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecipeRepository {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ═══════════════════════════════════════════════════════════
    // INSTANCIA DE UserRepository (NUEVO)
    // ═══════════════════════════════════════════════════════════
    private val userRepository = UserRepository()

    // ── Rutas de Firestore ────────────────────────────────────────────────
    private val colPublicas  = db.collection("recetas_publicas")
    private fun colPrivadas(uid: String) =
        db.collection("usuarios").document(uid)
            .collection("recetas_privadas")

    // ─────────────────────────────────────────────────────────────────────
    // CONSULTA A — Recetas del SISTEMA (bienvenida en Home)
    // Fuente: recetas_publicas donde tipoOrigen == "SISTEMA"
    // ─────────────────────────────────────────────────────────────────────
    fun getRecetasSistema(): Flow<List<Recipe>> = callbackFlow {
        val listener = colPublicas
            .whereEqualTo("tipoOrigen", "SISTEMA")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val recetas = snapshot?.documents
                    ?.mapNotNull { it.toObject<Recipe>()?.copy(id = it.id) }
                    ?: emptyList()
                trySend(recetas)
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CONSULTA B — Recetas privadas del usuario actual
    // Fuente: /usuarios/{UID}/recetas_privadas/
    // ─────────────────────────────────────────────────────────────────────
    fun getRecetasPrivadas(): Flow<List<Recipe>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(emptyList()); close(); return@callbackFlow }

        val listener = colPrivadas(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val recetas = snapshot?.documents
                    ?.mapNotNull { it.toObject<Recipe>()?.copy(id = it.id) }
                    ?: emptyList()
                trySend(recetas)
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CONSULTA C — Recetas de comunidad (para Explorar)
    // Fuente: recetas_publicas donde tipoOrigen == "USUARIO"
    // ─────────────────────────────────────────────────────────────────────
    fun getRecetasComunidad(): Flow<List<Recipe>> = callbackFlow {
        val listener = colPublicas
            .whereEqualTo("tipoOrigen", "USUARIO")
            .whereEqualTo("visibilidad", "publica")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val recetas = snapshot?.documents
                    ?.mapNotNull { it.toObject<Recipe>()?.copy(id = it.id) }
                    ?: emptyList()
                trySend(recetas)
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GUARDAR receta — decide la colección según visibilidad
    // ─────────────────────────────────────────────────────────────────────
    suspend fun guardarReceta(recipe: Recipe) {
        val uid  = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado.")
        val nombre = auth.currentUser?.displayName ?: "Anónimo"

        val recetaFinal = recipe.copy(
            autorID     = uid,
            autorNombre = nombre,
            creadoEn    = System.currentTimeMillis()
        )

        if (recipe.visibilidad == Visibilidad.PUBLICA) {
            // Va a recetas_publicas con tipoOrigen USUARIO
            colPublicas.add(
                recetaFinal.copy(tipoOrigen = TipoOrigen.USUARIO)
            ).await()
        } else {
            // Va a /usuarios/{UID}/recetas_privadas/
            colPrivadas(uid).add(recetaFinal).await()
        }

        // ═══════════════════════════════════════════════════════════
        // NUEVO: Incrementar contador de recetas del usuario
        // Se ejecuta DESPUÉS de guardar la receta exitosamente
        // ═══════════════════════════════════════════════════════════
        userRepository.incrementarRecetasCreadas(uid)
            .onFailure { e ->
                // Loguear error pero no fallar la creación de receta
                // El contador puede sincronizarse después
                println("Error actualizando stats: ${e.message}")
            }
    }
}