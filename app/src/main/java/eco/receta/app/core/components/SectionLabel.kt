package eco.receta.app.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eco.receta.app.features.recipes.create.GrisTexto
import eco.receta.app.features.recipes.create.MarronOscuro
import eco.receta.app.features.recipes.create.VerdeValido

@Composable
public fun SectionLabel(
    icon: String,
    text: String,
    isValid: Boolean = true,
    showValidation: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Text(
            text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MarronOscuro
        )
        // Indicador de validación junto al label
        AnimatedVisibility(
            visible = showValidation,
            enter = fadeIn() + scaleIn(),
            exit  = fadeOut() + scaleOut()
        ) {
            Icon(
                imageVector = if (isValid) Icons.Default.CheckCircle
                else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isValid) VerdeValido else GrisTexto,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}