package eco.receta.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eco.receta.app.features.recipes.create.CampoFondo
import eco.receta.app.features.recipes.create.Dorado
import eco.receta.app.features.recipes.create.GrisTexto
import eco.receta.app.features.recipes.create.MarronOscuro

@Composable
public fun VisibilidadOpcionAnimada(
    emoji: String,
    titulo: String,
    descripcion: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animaciones de color suaves
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MarronOscuro else CampoFondo,
        animationSpec = tween(durationMillis = 300),
        label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MarronOscuro,
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )
    val descColor by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(0.7f) else GrisTexto,
        animationSpec = tween(durationMillis = 300),
        label = "descColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Dorado else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(
                titulo,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 14.sp
            )
            Text(
                descripcion,
                color = descColor,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}