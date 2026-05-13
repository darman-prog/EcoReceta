package eco.receta.app.features.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eco.receta.app.data.model.Ingredient
import eco.receta.app.data.model.PrecioTienda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceCompareSheet(
    ingredient: Ingredient,
    precios: List<PrecioTienda>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onComparar: () -> Unit,
    onSeleccionar: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMPARATIVA DE PRECIOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = ingredient.producto,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Precio por ${ingredient.unidad} en tiendas locales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (precios.isEmpty()) {
                // Botón para cargar precios
                Button(
                    onClick = onComparar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver comparativa de precios")
                }
            } else {
                // Lista de precios por tienda
                precios.forEachIndexed { index, precioTienda ->
                    PriceStoreItem(
                        precioTienda = precioTienda,
                        isBestPrice = index == 0,  // El primero es el más barato (ordenado)
                        isWorstPrice = index == precios.lastIndex
                    )
                    if (index < precios.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón seleccionar
                Button(
                    onClick = onSeleccionar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Seleccionar y añadir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// PRICE STORE ITEM (cada tienda)
// ═════════════════════════════════════════════════════════════════

@Composable
private fun PriceStoreItem(
    precioTienda: PrecioTienda,
    isBestPrice: Boolean,
    isWorstPrice: Boolean
) {
    val backgroundColor = when {
        isBestPrice -> Color(0xFFE8F5E9)  // Verde claro para el más barato
        isWorstPrice -> Color(0xFFF5F0E8)  // Neutro para el más caro
        else -> Color(0xFFF5F5F5)          // Gris para el del medio
    }

    val borderColor = when {
        isBestPrice -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isBestPrice) {
            androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Iniciales de la tienda
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = precioTienda.tienda.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = precioTienda.tienda,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (precioTienda.esPromocion) {
                        Text(
                            text = precioTienda.tipoPromocion,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Precio
            Column(horizontalAlignment = Alignment.End) {
                if (isBestPrice) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MEJOR PRECIO",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "COP $${"%,.0f".format(precioTienda.precio)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isBestPrice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}