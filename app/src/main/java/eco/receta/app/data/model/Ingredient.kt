package eco.receta.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Ingredient(
    @DocumentId val id: String = "",

    val producto: String = "",
    val marca: String = "",
    val categoria: String = "",
    val unidad: String = "",

    // ═══════════════════════════════════════════════════════════
    // MAPEO CON NOMBRES REALES DE FIRESTORE
    // ═══════════════════════════════════════════════════════════

    @PropertyName("imagen")           // ← Firestore dice "imagen", no "imagenUrl"
    val imagenUrl: String = "",

    @PropertyName("esComestible")    // ← Campo extra que existe en Firestore
    val esComestible: Boolean = false,

    @PropertyName("tamaño")         // ← Campo extra
    val tamano: Int = 0,

    @PropertyName("tiendas_disponibles")  // ← Array de strings
    val tiendasDisponibles: List<String> = emptyList(),

    @PropertyName("precio_maximo")  // ← Campo numérico
    val precioMaximo: Double = 0.0,

    @PropertyName("precio_minimo")  // ← Campo numérico
    val precioMinimo: Double = 0.0,

    // ═══════════════════════════════════════════════════════════
    // PRECIOS: Array de objetos en Firestore
    // Estructura: { precio: 21600, tienda: "Carulla", tiendaID: 9 }
    // ═══════════════════════════════════════════════════════════
    val precios: List<PrecioTiendaFirestore> = emptyList()
)

// ═══════════════════════════════════════════════════════════
// MODELO PARA EL ARRAY "precios" DE FIRESTORE
// ═══════════════════════════════════════════════════════════
data class PrecioTiendaFirestore(
    val precio: Double = 0.0,
    val tienda: String = "",
    @PropertyName("tiendaID") val tiendaId: Int = 0
)

// ═══════════════════════════════════════════════════════════
// MODELO PARA LA UI (convertido desde Firestore)
// ═══════════════════════════════════════════════════════════
data class PrecioTienda(
    val tienda: String = "",
    val precio: Double = 0.0,
    val esPromocion: Boolean = false,
    val tipoPromocion: String = ""
)