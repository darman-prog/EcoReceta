package eco.receta.app.features.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eco.receta.app.data.model.Ingredient
import eco.receta.app.data.model.PrecioTiendaFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailScreen(
    ingredient: Ingredient,
    onNavigateBack: () -> Unit,
    onAddToRecipe: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ingredient.producto) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // ═══════════════════════════════════════════════════════
            // IMAGEN DEL PRODUCTO
            // ═══════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFF5F0E8))
            ) {
                if (ingredient.imagenUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ingredient.imagenUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Badge PRODUCTO LOCAL (usamos esComestible como proxy o campo real)
                if (ingredient.esComestible) {  // ← O crea un campo "esLocal" si lo necesitas
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E7D32))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "PRODUCTO LOCAL",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            // INFO PRINCIPAL
            // ═══════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = ingredient.producto,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Marca y categoría
                Text(
                    text = "${ingredient.marca} • ${ingredient.categoria}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Tiendas disponibles
                if (ingredient.tiendasDisponibles.isNotEmpty()) {
                    Text(
                        text = "Disponible en: ${ingredient.tiendasDisponibles.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción (si tienes campo, si no usa producto)
                Text(
                    text = "${ingredient.producto} de la marca ${ingredient.marca}. " +
                            "Presentación de ${ingredient.tamano}${ingredient.unidad}.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ═══════════════════════════════════════════════════════
                // CARD DE PRECIO DESTACADO (el más barato)
                // ═══════════════════════════════════════════════════════
                val preciosOrdenados = ingredient.precios.sortedBy { it.precio }
                val precioMasBarato = preciosOrdenados.firstOrNull()

                if (precioMasBarato != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MEJOR PRECIO EN ${precioMasBarato.tienda.uppercase()}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "COP $${"%,.0f".format(precioMasBarato.precio)} / ${ingredient.unidad}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (ingredient.precioMinimo == ingredient.precioMaximo) {
                                    Text(
                                        text = "Precio único en todas las tiendas",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(
                                onClick = onAddToRecipe,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Añadir a receta")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ═══════════════════════════════════════════════════════
                // COMPARATIVA DE PRECIOS POR SUPERMERCADO
                // ═══════════════════════════════════════════════════════
                Text(
                    text = "Comparativa de Precios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Precio por ${ingredient.unidad} en tiendas locales",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Lista de precios por tienda
                preciosOrdenados.forEachIndexed { index, precio ->
                    TiendaPrecioItem(
                        precioFirestore = precio,
                        isBestPrice = index == 0,
                        isWorstPrice = index == preciosOrdenados.lastIndex
                    )

                    if (index < preciosOrdenados.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ═══════════════════════════════════════════════════════
                // RESUMEN DE RANGO DE PRECIOSA
                // ═══════════════════════════════════════════════════════
                if (ingredient.precioMinimo > 0 && ingredient.precioMaximo > 0 && ingredient.precioMinimo != ingredient.precioMaximo) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Resumen de Precios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Más barato", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "COP $${"%,.0f".format(ingredient.precioMinimo)}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Más caro", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "COP $${"%,.0f".format(ingredient.precioMaximo)}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// ITEM DE TIENDA CON PRECIO
// ═════════════════════════════════════════════════════════════════

@Composable
private fun TiendaPrecioItem(
    precioFirestore: PrecioTiendaFirestore,
    isBestPrice: Boolean,
    isWorstPrice: Boolean
) {
    val backgroundColor = when {
        isBestPrice -> Color(0xFFE8F5E9)  // Verde claro
        isWorstPrice -> Color(0xFFF5F0E8) // Crema
        else -> MaterialTheme.colorScheme.surface
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
                        text = precioFirestore.tienda.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = precioFirestore.tienda,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isBestPrice) {
                        Text(
                            text = "MEJOR PRECIO",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Precio
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "COP $${"%,.0f".format(precioFirestore.precio)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isBestPrice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}