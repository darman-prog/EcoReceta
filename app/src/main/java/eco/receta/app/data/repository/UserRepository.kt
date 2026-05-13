package eco.receta.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import eco.receta.app.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val usuariosRef = db.collection("usuarios")

    // ═══════════════════════════════════════════════════════════
    // CREAR USUARIO NUEVO (con stats inicializados en 0)
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
    // INCREMENTAR CONTADOR DE RECETAS (ATÓMICO)
    // Usa FieldValue.increment para evitar race conditions
    // ═══════════════════════════════════════════════════════════
    suspend fun incrementarRecetasCreadas(uid: String): Result<Int> = try {
        // 1. Incrementar atómicamente el contador
        usuariosRef.document(uid).update(
            "recetasCreadas", FieldValue.increment(1),
            "ultimaActualizacion", FieldValue.serverTimestamp()
        ).await()

        // 2. Leer el nuevo valor para calcular el badge
        val snapshot = usuariosRef.document(uid).get().await()
        val nuevoContador = snapshot.getLong("recetasCreadas")?.toInt() ?: 0

        // 3. Calcular y actualizar el badge si cambió
        val nuevoBadge = calcularBadge(nuevoContador)
        val badgeActual = snapshot.getString("badgeActual") ?: "Chef Novato"

        if (nuevoBadge != badgeActual) {
            usuariosRef.document(uid).update("badgeActual", nuevoBadge).await()
        }

        Result.success(nuevoContador)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ═══════════════════════════════════════════════════════════
    // ACTUALIZAR BADGE MANUALMENTE (si se necesita recalcular)
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
    // FUNCIÓN PURA: Calcular badge según cantidad de recetas
    // ═══════════════════════════════════════════════════════════
    companion object {
        fun calcularBadge(recetasCreadas: Int): String = when {
            recetasCreadas <= 5 -> "Chef Novato"
            recetasCreadas in 6..15 -> "Cocinero Aficionado"
            else -> "Maestro Culinario"
        }

        // Para obtener el siguiente badge (motivación al usuario)
        fun getProximoBadge(recetasCreadas: Int): Pair<String, Int> = when {
            recetasCreadas <= 5 -> "Cocinero Aficionado" to (6 - recetasCreadas)
            recetasCreadas in 6..15 -> "Maestro Culinario" to (16 - recetasCreadas)
            else -> "Leyenda Culinaria" to 0 // Nivel máximo
        }
    }
}