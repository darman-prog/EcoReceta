// eco/receta/app/data/repository/RecipeRepository.kt
package eco.receta.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import eco.receta.app.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RecipeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ═══════════════════════════════════════════════════════════
    // CREAR RECETA CON IMAGEN (LOCAL-ONLY)
    // ═══════════════════════════════════════════════════════════
    suspend fun crearRecetaConImagen(
        context: Context,
        recipe: Recipe,
        imageUri: Uri
    ): String {
        val recipeRef = db.collection("recetas_publicas").document()
        val recipeId = recipeRef.id

        // Guardar imagen SOLO local y obtener "file://..."
        val localImageUrl = saveRecipeImageLocal(context, recipeId, imageUri)

        // Guardar receta con URL local (file://...) -> SOLO pruebas mismo dispositivo
        val recipeConImagen = recipe.copy(
            id = recipeId,
            imageUrl = localImageUrl
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
    // OBTENER RECETAS DEL SISTEMA
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
    // OBTENER RECETAS PÚBLICAS DE USUARIOS
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
    // OBTENER RECETAS POR USUARIO
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
    // GUARDAR IMAGEN LOCALMENTE (filesDir) Y DEVOLVER "file://..."
    // ═══════════════════════════════════════════════════════════
    private suspend fun saveRecipeImageLocal(
        context: Context,
        recipeId: String,
        imageUri: Uri
    ): String = withContext(Dispatchers.IO) {
        // Carpeta interna: /data/data/<pkg>/files/recipe_images/<recipeId>/cover.jpg
        val dir = File(context.filesDir, "recipe_images/$recipeId")
        if (!dir.exists()) dir.mkdirs()

        val destFile = File(dir, "cover.jpg")

        // Forma robusta: leer Uri con ContentResolver.openInputStream() y copiar con copyTo(). [1](https://codingtechroom.com/question/-android-kotlin-file-uri-action-get-content)[2](https://www.baeldung.com/kotlin/inputstream-to-file)
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: error("No se pudo abrir la imagen seleccionada")

        // Devolver como URI local "file://..." para que Coil lo cargue como string
        destFile.toURI().toString()
    }
}
