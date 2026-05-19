package eco.receta.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await



suspend fun uploadRecipeImage(
    recipeId: String,
    imageUri: Uri
): String {
    val storageRef = FirebaseStorage.getInstance()
        .reference
        .child("recetas_publicas")
        .child(recipeId)
        .child("cover.jpg")
    storageRef.putFile(imageUri).await()
    return storageRef.downloadUrl.await().toString()
}


suspend fun uploadAndAttachImage(
    recipeId: String,
    uri: Uri,
    docRef: DocumentReference
) {
    val storageRef = FirebaseStorage.getInstance()
        .reference
        .child("recetas_publicas/$recipeId/cover.jpg")

    try {
        // Subir a Storage
        storageRef.putFile(uri).await()

        // Guardar URL https en Firestore
        val url = storageRef.downloadUrl.await().toString()
        docRef.update("imageUrl", url).await()

    } catch (e: Exception) {
        // Rollback completo
        runCatching { storageRef.delete().await() }
        runCatching { docRef.delete().await() }
        throw e
    }
}


suspend fun updateRecipeImageUrl(
    recipeId: String,
    imageUrl: String
) {
    FirebaseFirestore.getInstance()
        .collection("recetas_publicas") // ✅ nombre REAL
        .document(recipeId)
        .update("imageUrl", imageUrl)
        .await()
}