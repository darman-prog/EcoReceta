package eco.receta.app.data.model

data class Recipe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val timeMinutes: Int = 0,
    val servings: Int = 0,
    val level: String = "",           // "Fácil", "Medio", "Difícil"
    val totalCost: Double = 0.0,
    val region: String = "",          // "BOGOTÁ", "ANTIOQUIA", etc.
    val category: String = "",        // "GOURMET", "SNACK", "TRADICIONAL", etc.
    val isPublic: Boolean = true,
    val authorId: String = "",
    val authorName: String = "",
    val createdAt: Long = 0L
)