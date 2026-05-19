package eco.receta.app.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
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
    suspend fun crearRecetaConImagen(
        recipe: Recipe,
        imageUri: Uri
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("recetas_publicas").document()
        val recipeId = docRef.id

        val recipeWithId = recipe.copy(id = recipeId)

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("recetas_publicas/$recipeId/cover.jpg")

        var imageUploaded = false

        try {
            // 1️⃣ Guardar receta base
            docRef.set(recipeWithId).await()

            // 2️⃣ Subir imagen
            storageRef.putFile(imageUri).await()
            imageUploaded = true

            val url = storageRef.downloadUrl.await().toString()

            // 3️⃣ Guardar URL
            docRef.update("imageUrl", url).await()

        } catch (e: Exception) {

            // ✅ Solo borrar si realmente se subió
            if (imageUploaded) {
                runCatching { storageRef.delete().await() }
            }

            // ✅ Firestore siempre se limpia
            runCatching { docRef.delete().await() }

            throw e
        }
    }


}