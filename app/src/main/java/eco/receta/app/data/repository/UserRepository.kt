package eco.receta.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import eco.receta.app.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val usuariosRef = db.collection("usuarios")

    // ═══════════════════════════════════════════════════════════
    // CREAR USUARIO NUEVO
    // ═══════════════════════════════════════════════════════════
    suspend fun crearUsuarioEnFirestore(
        uid: String,
        nombre: String,
        email: String,
        fotoUrl: String = ""
    ): Result<Unit> = try {
        val user = User(
            uid = uid,
            nombreCompleto = nombre,
            email = email,
            fotoUrl = fotoUrl,
            recetasCreadas = 0,
            badgeActual = "Chef Novato"
        )
        usuariosRef.document(uid).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ═══════════════════════════════════════════════════════════
    // OBTENER DATOS DEL USUARIO
    // ═══════════════════════════════════════════════════════════
    suspend fun getUserData(uid: String): Result<User> = try {
        val snapshot = usuariosRef.document(uid).get().await()
        val user = snapshot.toObject<User>()?.copy(uid = snapshot.id)
        if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ═══════════════════════════════════════════════════════════
    // ACTUALIZAR CONTADOR DE RECETAS
    // ═══════════════════════════════════════════════════════════
    suspend fun actualizarContadorRecetas(uid: String, totalRecetas: Int): Result<Unit> = try {
        usuariosRef.document(uid).update(
            "recetasCreadas", totalRecetas,
            "ultimaActualizacion", FieldValue.serverTimestamp()
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ═══════════════════════════════════════════════════════════
    // ACTUALIZAR BADGE
    // ═══════════════════════════════════════════════════════════
    suspend fun actualizarBadge(uid: String, recetasCount: Int): Result<String> = try {
        val nuevoBadge = calcularBadge(recetasCount)
        usuariosRef.document(uid).update(
            "badgeActual", nuevoBadge,
            "ultimaActualizacion", FieldValue.serverTimestamp()
        ).await()
        Result.success(nuevoBadge)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULAR BADGE
    // ═══════════════════════════════════════════════════════════
    companion object {
        fun calcularBadge(recetasCreadas: Int): String = when {
            recetasCreadas <= 5 -> "Chef Novato"
            recetasCreadas in 6..15 -> "Cocinero Aficionado"
            else -> "Maestro Culinario"
        }

        fun getProximoBadge(recetasCreadas: Int): Pair<String, Int> = when {
            recetasCreadas <= 5 -> "Cocinero Aficionado" to (6 - recetasCreadas)
            recetasCreadas in 6..15 -> "Maestro Culinario" to (16 - recetasCreadas)
            else -> "Leyenda Culinaria" to 0
        }
    }
}