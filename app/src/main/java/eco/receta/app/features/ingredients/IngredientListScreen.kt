package eco.receta.app.features.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eco.receta.app.data.model.Ingredient
import eco.receta.app.features.recipes.create.IngredienteSeleccionado

// ─── Paleta EcoReceta ────────────────────────────────────────────────────────
private val Crema        = Color(0xFFF6EFE9)
private val MarronOscuro = Color(0xFF2C1A0E)
private val Dorado       = Color(0xFFC8922A)
private val Rojo         = Color(0xFFD94F3D)
private val TarjetaBg    = Color(0xFFFFF8F2)
private val CampoFondo   = Color(0xFFEDE8DF)
private val GrisTexto    = Color(0xFF8D8D8D)

// ─── Categorías para filtros ─────────────────────────────────────────────────
private val CATEGORIAS = listOf(
    "Todas", "Frutas", "Verduras", "Carnes", "Lácteos",
    "Granos", "Bebidas", "Snacks", "Condimentos"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListScreen(
    navController: NavController,
    viewModel: IngredientViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onIngredientSelected: (IngredienteSeleccionado) -> Unit
) {
    val uiState = viewModel.uiState
    var categoriaActiva by remember { mutableStateOf("Todas") }

    // Filtro local por categoría
    val productosFiltrados = remember(uiState.productos, categoriaActiva) {
        if (categoriaActiva == "Todas") uiState.productos
        else uiState.productos.filter {
            it.categoria.equals(categoriaActiva, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Crema,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Volver",
                            tint = MarronOscuro
                        )
                    }
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    color = MarronOscuro,
                                    fontWeight = FontWeight.ExtraBold
                                )) { append("Eco") }
                                withStyle(SpanStyle(
                                    color = Dorado,
                                    fontWeight = FontWeight.ExtraBold
                                )) { append("Receta") }
                            },
                            fontSize = 20.sp
                        )
                        Text(
                            "Fresco & Local",
                            fontSize = 12.sp,
                            color = GrisTexto
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
        ) {

            // ── Barra de búsqueda ────────────────────────────────────────
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = {
                    Text(
                        "Busca ingredientes frescos...",
                        color = GrisTexto,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        null,
                        tint = Dorado
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CampoFondo,
                    focusedContainerColor   = CampoFondo,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Dorado
                )
            )

            // ── Chips de categoría ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CATEGORIAS.forEach { categoria ->
                    val isActive = categoria == categoriaActiva
                    Surface(
                        onClick = { categoriaActiva = categoria },
                        shape = RoundedCornerShape(50),
                        color = if (isActive) MarronOscuro else CampoFondo,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                categoria,
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Bold
                                else FontWeight.Normal,
                                color = if (isActive) Color.White else MarronOscuro
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Indicador de resultados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${productosFiltrados.size} productos",
                    color = GrisTexto,
                    fontSize = 12.sp
                )
                if (categoriaActiva != "Todas") {
                    TextButton(
                        onClick = { categoriaActiva = "Todas" },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Limpiar filtro ✕",
                            color = Rojo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Estados ──────────────────────────────────────────────────
            when {
                uiState.isLoading && uiState.productos.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Dorado) }
                }

                uiState.isLoading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Dorado,
                        trackColor = CampoFondo
                    )
                }

                productosFiltrados.isEmpty() -> {
                    EmptyState(
                        query = uiState.query,
                        categoria = categoriaActiva,
                        onLimpiar = { categoriaActiva = "Todas" }
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(productosFiltrados) { ingredient ->
                            IngredientCard(
                                ingredient = ingredient,
                                onVerDetalles = {
                                    navController.navigate(
                                        "ingredient_detail/${ingredient.id}"
                                    )
                                }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

// ─── Tarjeta de ingrediente ───────────────────────────────────────────────────
@Composable
private fun IngredientCard(
    ingredient: Ingredient,
    onVerDetalles: () -> Unit
) {
    val precioMinimo = ingredient.precios.minOfOrNull { it.precio } ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Imagen
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CampoFondo),
                contentAlignment = Alignment.Center
            ) {
                if (ingredient.imagenUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ingredient.imagenUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        ingredient.producto.take(1).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MarronOscuro
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                // Categoría
                Text(
                    ingredient.categoria.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Dorado,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    ingredient.producto,
                    fontWeight = FontWeight.Bold,
                    color = MarronOscuro,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                if (ingredient.tiendasDisponibles.isNotEmpty()) {
                    Text(
                        ingredient.tiendasDisponibles.take(3).joinToString(" · "),
                        fontSize = 11.sp,
                        color = GrisTexto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (precioMinimo > 0) {
                    Text(
                        "Desde COP $${"%,.0f".format(precioMinimo)}",
                        fontWeight = FontWeight.Bold,
                        color = Rojo,
                        fontSize = 13.sp
                    )
                }
            }

            // Botón
            Button(
                onClick = onVerDetalles,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MarronOscuro),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    "Ver",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Estado vacío ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(
    query: String,
    categoria: String,
    onLimpiar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("🔍", fontSize = 40.sp)
        Text(
            if (query.isNotBlank()) "No encontramos \"$query\""
            else "Sin resultados en \"$categoria\"",
            color = MarronOscuro,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            "Intenta con otro término o categoría",
            color = GrisTexto,
            fontSize = 13.sp
        )
        if (categoria != "Todas") {
            TextButton(onClick = onLimpiar) {
                Text("Ver todas las categorías", color = Dorado, fontWeight = FontWeight.Bold)
            }
        }
    }
}