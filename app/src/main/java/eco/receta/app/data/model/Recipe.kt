// eco/receta/app/data/model/Recipe.kt

package eco.receta.app.data.model


import com.google.firebase.firestore.PropertyName



data class Recipe(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val imageUrl: String = "",
    val tiempoMinutos: Int = 0,
    val porciones: Int = 0,
    val nivel: String = "",
    val costoTotal: Double = 0.0,
    val region: String = "",
    val categoria: String = "",
    val tipoOrigen: TipoOrigen = TipoOrigen.USUARIO,
    val visibilidad: Visibilidad = Visibilidad.PRIVADA,
    val autorID: String = "",
    val autorNombre: String = "",
    val esOficial: Boolean = false,
    val ingredientes_ids: List<String> = emptyList(),
    val creadoEn: Long = 0L,
    val imagen: String = ""
)

enum class TipoOrigen {
    @PropertyName("SISTEMA")  SISTEMA,
    @PropertyName("USUARIO")  USUARIO
}

enum class Visibilidad {
    @PropertyName("publica")  PUBLICA,   // Mantiene "publica" en minúsculas como en la foto
    @PropertyName("privada")  PRIVADA
}