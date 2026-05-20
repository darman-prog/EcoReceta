package eco.receta.app.data.model

import com.google.firebase.firestore.DocumentId

data class Ingredient(
    @DocumentId val id: String = "",
    val producto: String = "",
    val marca: String = "",
    val categoria: String = "",
    val unidad: String = "",
    val imagen: String = "",              // ← Firestore dice "imagen"
    val esComestible: Boolean = false,    // ← Firestore dice "esComestible"
    val tamaño: Int = 0,                // ← Firestore dice "tamaño" (con ñ)
    val tiendas_disponibles: List<String> = emptyList(),  // ← Firestore dice "tiendas_disponibles"
    val precio_maximo: Double = 0.0,    // ← Firestore dice "precio_maximo"
    val precio_minimo: Double = 0.0,    // ← Firestore dice "precio_minimo"

    // Array de precios (estructura anidada)
    val precios: List<PrecioTiendaFirestore> = emptyList()
)

data class PrecioTiendaFirestore(
    val precio: Double = 0.0,
    val tienda: String = "",
    val tiendaID: Int = 0   // ← Firestore dice "tiendaID"
)