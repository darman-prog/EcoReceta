// eco/receta/app/data/model/Recipe.kt

package eco.receta.app.data.model

data class Recipe(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val imageUrl: String = "",
    val tiempoMinutos: Int = 0,
    val porciones: Int = 0,
    val nivel: String = "",              // "Fácil", "Medio", "Difícil"
    val costoTotal: Double = 0.0,
    val region: String = "",
    val categoria: String = "",

    // ── Metadata Keys del sistema de visibilidad ──────────────────────────
    val tipoOrigen: TipoOrigen = TipoOrigen.USUARIO,
    val visibilidad: Visibilidad = Visibilidad.PRIVADA,
    val autorID: String = "",
    val autorNombre: String = "",
    val esOficial: Boolean = false,

    // ── Referencia a productos para cálculo dinámico de precios ──────────
    // Cada string referencia el campo `producto` de la colección `productos`
    val ingredientes_ids: List<String> = emptyList(),

    val creadoEn: Long = 0L
)

enum class TipoOrigen { SISTEMA, USUARIO }
enum class Visibilidad { PUBLICA, PRIVADA }