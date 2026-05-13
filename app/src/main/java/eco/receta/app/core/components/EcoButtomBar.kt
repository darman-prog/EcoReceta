package eco.receta.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eco.receta.app.features.home.ColorDarkBrown
import eco.receta.app.features.home.ColorGold


// ─── Barra de Navegación Personalizada (EcoBottomNavBar) ───────────────────

@Composable
fun EcoBottomNavBar(
    currentRoute: String,           // Ruta actual para saber qué botón resaltar
    onHomeClick: () -> Unit,        // Acción al pulsar Inicio
    onExploreClick: () -> Unit,     // Acción al pulsar Explorar
    onCreateClick: () -> Unit,      // Acción al pulsar Crear
    onProfileClick: () -> Unit      // Acción al pulsar Perfil
) {
    // Definimos la lista de ítems para no repetir código
    val navItems = listOf(
        Triple("home", "🏠", "INICIO"),
        Triple("explore", "🧭", "EXPLORAR"),
        Triple("create", "🍴", "CREAR"),
        Triple("profile", "👤", "PERFIL")
    )
    val actions = listOf(onHomeClick, onExploreClick, onCreateClick, onProfileClick)

    // Contenedor principal de la barra (Material 3)
    NavigationBar(
        containerColor = ColorDarkBrown, // Fondo oscuro definido arriba
        tonalElevation = 8.dp            // Sombra sutil
    ) {
        navItems.forEachIndexed { index, item ->
            val isSelected = currentRoute == item.first

            NavigationBarItem(
                selected = isSelected,
                onClick = actions[index],
                // Personalización del ICONO con degradado y bordes
                icon = {
                    Box(
                        modifier = Modifier
                            // 1. Radio del borde (esquinas suaves)
                            .clip(RoundedCornerShape(12.dp))
                            // 2. Fondo: Degradado si está seleccionado, transparente si no
                            .background(
                                if (isSelected) Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFD2B48C), // Marrón claro (Arriba)
                                        Color(0xFF8B5E3C)  // Marrón medio (Abajo)
                                    )
                                ) else Brush.linearGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            )
                            // 3. Espaciado interno del cuadro resaltado
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.second, fontSize = 20.sp)
                    }
                },
                // Etiqueta de texto debajo del icono
                label = {
                    Text(
                        text = item.third,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                // Colores de los estados del botón
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = ColorGold,              // Texto amarillo al seleccionar
                    unselectedTextColor = Color.White.copy(0.5f), // Texto grisáceo al no seleccionar
                    indicatorColor = Color.Transparent          // Ocultamos el círculo feo por defecto
                )
            )
        }
    }
}

