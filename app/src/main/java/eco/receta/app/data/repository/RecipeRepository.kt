package eco.receta.app.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import eco.receta.app.data.model.Recipe
import kotlinx.coroutines.tasks.await

class RecipeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ═══════════════════════════════════════════════════════════
    // GUARDAR RECETA NUEVA CON IMAGEN
    // ═══════════════════════════════════════════════════════════
    suspend fun crearRecetaConImagen(recipe: Recipe, imageUri: Uri): String {
        val recipeRef = db.collection("recetas_publicas").document()
        val recipeId = recipeRef.id

        // Subir imagen
        val imageUrl = uploadRecipeImage(recipeId, imageUri)

        // Guardar receta con URL de imagen
        val recipeConImagen = recipe.copy(
            id = recipeId,
            imageUrl = imageUrl
        )
        recipeRef.set(recipeConImagen).await()

        // Si es privada, también guardar en recetas_privadas del usuario
        if (recipe.visibilidad.name == "PRIVADA") {
            val uid = auth.currentUser?.uid ?: return recipeId
            db.collection("usuarios")
                .document(uid)
                .collection("recetas_privadas")
                .document(recipeId)
                .set(recipeConImagen)
                .await()
        }

        return recipeId
    }

    // ═══════════════════════════════════════════════════════════
    // OBTENER RECETAS DEL SISTEMA (autorID = "admin") - Para Home destacadas
    // ═══════════════════════════════════════════════════════════
    suspend fun getRecetasSistema(): List<Recipe> {
        return try {
            val snapshot = db.collection("recetas_publicas")
                .whereEqualTo("autorID", "admin")
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject<Recipe>()?.copy(id = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // OBTENER RECETAS PÚBLICAS DE USUARIOS (no admin) - Para Explore
    // ═══════════════════════════════════════════════════════════
    suspend fun getRecetasComunidad(): List<Recipe> {
        return try {
            val snapshot = db.collection("recetas_publicas")
                .whereEqualTo("visibilidad", "publica")
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject<Recipe>()?.copy(id = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // OBTENER RECETAS PRIVADAS DEL USUARIO ACTUAL
    // ═══════════════════════════════════════════════════════════
    suspend fun getRecetasPrivadas(): List<Recipe> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("usuarios")
                .document(uid)
                .collection("recetas_privadas")
                .get()
                .await()

            Log.d("RecipeRepository", "Recetas privadas: ${snapshot.size()}")

            snapshot.documents.mapNotNull {
                it.toObject<Recipe>()?.copy(id = it.id)
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getRecetasPrivadas: ${e.message}")
            emptyList()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // OBTENER RECETAS POR USUARIO (para contar y recalcular badge)
    // ═══════════════════════════════════════════════════════════
    suspend fun getRecetasPorUsuario(userId: String): List<Recipe> {
        return try {
            val privadas = db.collection("usuarios")
                .document(userId)
                .collection("recetas_privadas")
                .get()
                .await()
                .documents.mapNotNull { it.toObject<Recipe>()?.copy(id = it.id) }

            val publicas = db.collection("recetas_publicas")
                .whereEqualTo("autorID", userId)
                .get()
                .await()
                .documents.mapNotNull { it.toObject<Recipe>()?.copy(id = it.id) }

            privadas + publicas
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FUNCIONES DE IMAGEN
    // ═══════════════════════════════════════════════════════════
    suspend fun uploadRecipeImage(recipeId: String, imageUri: Uri): String {
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("recetas_publicas")
            .child(recipeId)
            .child("cover.jpg")
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }


}