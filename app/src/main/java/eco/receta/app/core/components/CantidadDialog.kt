package eco.receta.app.features.ingredients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eco.receta.app.data.model.Ingredient

@Composable
fun CantidadDialog(
    ingredient: Ingredient,
    cantidad: String,
    unidad: String,
    onCantidadChange: (String) -> Unit,
    onUnidadChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir ${ingredient.producto}") },
        text = {
            Column {
                Text(
                    "¿Cuánto necesitas para tu receta?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cantidad
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = onCantidadChange,
                    label = { Text("Cantidad") },
                    placeholder = { Text("Ej: 500") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Unidad
                OutlinedTextField(
                    value = unidad,
                    onValueChange = onUnidadChange,
                    label = { Text("Unidad") },
                    placeholder = { Text(ingredient.unidad) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Precio estimado
                val cantidadNum = cantidad.toDoubleOrNull() ?: 0.0
                val precioUnitario = ingredient.precios.minOfOrNull { it.precio } ?: 0.0
                val estimado = cantidadNum * precioUnitario

                if (estimado > 0) {
                    Text(
                        text = "Precio estimado: COP $${"%,.0f".format(estimado)}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = cantidad.isNotBlank() && unidad.isNotBlank()
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}