// eco/receta/app/core/utils/AuthorNameFormatter.kt
package eco.receta.app.core.utils

object AuthorNameFormatter {

    private val stopWords = setOf("de", "del", "la", "las", "los", "y")

    fun format(raw: String, maxChars: Int = 18): String {
        val clean = raw.trim().replace(Regex("\\s+"), " ")
        if (clean.isEmpty()) return ""
        if (clean.length <= maxChars) return clean

        val parts = clean.split(" ")
        if (parts.size == 1) return ellipsize(clean, maxChars)

        val first = parts.first()
        val last = parts.last()

        // 1) Nombre + Apellido
        val firstLast = "$first $last"
        if (firstLast.length <= maxChars) return firstLast

        // 2) Nombre + (hasta 2 iniciales) + Apellido (sin conectores)
        val middleInitials = parts
            .subList(1, parts.size - 1)
            .filter { it.lowercase() !in stopWords }
            .take(2)
            .joinToString(" ") { "${it.first().uppercaseChar()}." }

        if (middleInitials.isNotEmpty()) {
            val withInitials = "$first $middleInitials $last"
            if (withInitials.length <= maxChars) return withInitials
        }
        // 3) N. Apellido
        val initialLast = "${first.first().uppercaseChar()}. $last"
        if (initialLast.length <= maxChars) return initialLast
        // 4) N. Apellid… (recorte final)
        val availableForLast = (maxChars - 3).coerceAtLeast(4) // "N. " = 3
        return "${first.first().uppercaseChar()}. ${ellipsize(last, availableForLast)}"
    }

    private fun ellipsize(text: String, maxChars: Int): String {
        if (text.length <= maxChars) return text
        if (maxChars <= 1) return "…"
        return text.take(maxChars - 1) + "…"
    }
}