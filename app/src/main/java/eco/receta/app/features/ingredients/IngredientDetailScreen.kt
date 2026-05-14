package eco.receta.app.features.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eco.receta.app.data.model.Ingredient
import eco.receta.app.data.model.PrecioTiendaFirestore

// ─── Paleta EcoReceta ────────────────────────────────────────────────────────
private val Crema        = Color(0xFFF6EFE9)
private val MarronOscuro = Color(0xFF2C1A0E)
private val Dorado       = Color(0xFFC8922A)
private val Rojo         = Color(0xFFD94F3D)
private val TarjetaBg    = Color(0xFFFFF8F2)
private val CampoFondo   = Color(0xFFEDE8DF)
private val GrisTexto    = Color(0xFF8D8D8D)
private val VerdePrecio  = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailScreen(
    ingredient: Ingredient,
    onNavigateBack: () -> Unit,
    onAddToRecipe: (Ingredient) -> Unit  // ← AHORA RECIBE Ingredient
) {
    val preciosOrdenados = ingredient.precios.sortedBy { it.precio }

    Scaffold(
        containerColor = Crema,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Volver",
                            tint = MarronOscuro
                        )
                    }
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = MarronOscuro, fontWeight = FontWeight.ExtraBold)) {
                                append("Eco")
                            }
                            withStyle(SpanStyle(color = Dorado, fontWeight = FontWeight.ExtraBold)) {
                                append("Receta")
                            }
                        },
                        fontSize = 20.sp
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MarronOscuro,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "MEJOR PRECIO",
                            color = Dorado,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "COP $${"%,.0f".format(
                                preciosOrdenados.firstOrNull()?.precio ?: 0.0
                            )}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Button(
                        onClick = { onAddToRecipe(ingredient) },  // ← PASAR ingredient
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rojo),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            "Añadir a receta",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Imagen del producto ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(CampoFondo)
            ) {
                if (ingredient.imagen.isNotEmpty()) {
                    AsyncImage(
                        model = ingredient.imagen,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Gradiente inferior
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Crema),
                                startY = 140f
                            )
                        )
                )
                // Badge PRODUCTO LOCAL
                if (ingredient.esComestible) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = VerdePrecio
                    ) {
                        Text(
                            "🌿 PRODUCTO LOCAL",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ── Info principal ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Text(
                    ingredient.producto,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MarronOscuro
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${ingredient.marca} · ${ingredient.categoria} · ${ingredient.tamaño}${ingredient.unidad}",
                    color = GrisTexto,
                    fontSize = 13.sp
                )

                if (ingredient.tiendas_disponibles.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Disponible en: ${ingredient.tiendas_disponibles.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = Dorado,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // ── Comparativa de precios ───────────────────────────────
                Spacer(Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Text("📊", fontSize = 18.sp)
                    Text(
                        "Comparativa de Precios",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MarronOscuro
                    )
                }
                Text(
                    "Precio por ${ingredient.unidad} en tiendas locales",
                    color = GrisTexto,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                preciosOrdenados.forEachIndexed { index, precio ->
                    TiendaPrecioCard(
                        precio = precio,
                        isBest = index == 0,
                        isWorst = index == preciosOrdenados.lastIndex
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // ── Rango de precios ─────────────────────────────────────
                if (ingredient.precio_minimo > 0 &&
                    ingredient.precio_maximo > ingredient.precio_minimo
                ) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TarjetaBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Más barato", fontSize = 12.sp, color = GrisTexto)
                                Text(
                                    "COP $${"%,.0f".format(ingredient.precio_minimo)}",
                                    fontWeight = FontWeight.Bold,
                                    color = VerdePrecio,
                                    fontSize = 16.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Más caro", fontSize = 12.sp, color = GrisTexto)
                                Text(
                                    "COP $${"%,.0f".format(ingredient.precio_maximo)}",
                                    fontWeight = FontWeight.Bold,
                                    color = Rojo,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TiendaPrecioCard(
    precio: PrecioTiendaFirestore,
    isBest: Boolean,
    isWorst: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isBest  -> Color(0xFFE8F5E9)
                isWorst -> TarjetaBg
                else    -> Color.White
            }
        ),
        border = if (isBest) androidx.compose.foundation.BorderStroke(
            2.dp, Color(0xFF2E7D32)
        ) else null,
        elevation = CardDefaults.cardElevation(if (isBest) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isBest) Color(0xFF2E7D32).copy(0.1f)
                            else CampoFondo
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        precio.tienda.take(2).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isBest) Color(0xFF2E7D32) else MarronOscuro,
                        fontSize = 13.sp
                    )
                }
                Column {
                    Text(
                        precio.tienda,
                        fontWeight = FontWeight.SemiBold,
                        color = MarronOscuro,
                        fontSize = 14.sp
                    )
                    if (isBest) {
                        Text(
                            "✓ MEJOR PRECIO",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Text(
                "COP $${"%,.0f".format(precio.precio)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = if (isBest) Color(0xFF2E7D32) else MarronOscuro
            )
        }
    }
}