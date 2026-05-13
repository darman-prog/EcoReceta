package eco.receta.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId val uid: String = "",
    val nombreCompleto: String = "",
    val email: String = "",
    val fotoUrl: String = "",

    // ═══════════════════════════════════════════════════════════
    // STATS Y BADGES (NUEVO)
    // ═══════════════════════════════════════════════════════════
    val recetasCreadas: Int = 0,           // Contador atómico en Firestore
    val badgeActual: String = "Chef Novato", // Se calcula automáticamente

    @ServerTimestamp val fechaRegistro: Date? = null,
    val ultimaActualizacion: Date? = null
)