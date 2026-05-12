
package eco.receta.app.features.recipes.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import eco.receta.app.data.model.Recipe

// ─── Colores del Figma ───────────────────────────────────────────────────────
private val ColorCream      = Color(0xFFFAF3EE)
private val ColorDarkBrown  = Color(0xFF2C1A0E)
private val ColorRed        = Color(0xFFD94F3D)
private val ColorGold       = Color(0xFFC8922A)
private val ColorBodyText   = Color(0xFF5C4033)
private val ColorCardBg     = Color(0xFFF5EFE8)
private val ColorChipBg     = Color(0xFFEDE8DF)

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: RecipeDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Carga la receta al entrar a la pantalla
    LaunchedEffect(recipeId) {
        viewModel.cargarReceta(recipeId)
    }

    when (val state = uiState.detailState) {

        is DetailState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorGold)
            }
        }

        is DetailState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️ ${state.message}", color = ColorRed, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors  = ButtonDefaults.buttonColors(containerColor = ColorGold)
                    ) {
                        Text("Volver", color = Color.White)
                    }
                }
            }
        }

        is DetailState.Success -> {
            RecipeDetailContent(
                recipe                = state.recipe,
                ingredientesResueltos = uiState.ingredientesResueltos,
                costoCalculado        = uiState.costoCalculado,
                onNavigateBack        = onNavigateBack
            )
        }
    }
}

// ─── Contenido principal ─────────────────────────────────────────────────────
@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    ingredientesResueltos: List<IngredienteResuelto>,
    costoCalculado: Double,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(ColorCream)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // espacio para el footer
        ) {

            // ── Header con imagen grande ─────────────────────────────────
            item {
                HeaderImage(
                    imageUrl       = recipe.imageUrl,
                    nombre         = recipe.nombre,
                    onNavigateBack = onNavigateBack
                )
            }

            // ── Chips: Tiempo · Porciones · Nivel ────────────────────────
            item {
                InfoChips(recipe = recipe)
            }

            // ── Sección ingredientes ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Ingredientes",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = ColorDarkBrown
                    )
                    // Badge con cantidad de ingredientes (Cálculo basado en lista de IDs)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ColorRed
                    ) {
                        Text(
                            text     = "${recipe.ingredientes_ids.size} ITEMS",
                            color    = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Lista de ingredientes ────────────────────────────────────
            if (ingredientesResueltos.isEmpty()) {
                // Si no hay ingredientes resueltos muestra los IDs crudos
                items(recipe.ingredientes_ids) { id ->
                    IngredienteItemSimple(nombre = id)
                }
            } else {
                items(
                    items = ingredientesResueltos,
                    key   = { it.nombre }
                ) { ingrediente ->
                    IngredienteItem(ingrediente = ingrediente)
                }
            }

            // ── Descripción ──────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text       = "Descripción",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = ColorDarkBrown
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text      = recipe.descripcion,
                        fontSize  = 14.sp,
                        color     = ColorBodyText,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }

        // ── Footer fijo: Costo total + botón Agregar ─────────────────────
        RecipeFooter(
            costoTotal = costoCalculado,
            modifier   = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Header con imagen + gradiente + botón atrás ─────────────────────────────
@Composable
private fun HeaderImage(
    imageUrl: String,
    nombre: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Imagen de fondo con Coil
        SubcomposeAsyncImage(
            model              = imageUrl,
            contentDescription = nombre,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading ->
                    Box(Modifier.fillMaxSize().background(Color(0xFFD6C5B5)))
                is AsyncImagePainter.State.Error ->
                    Box(Modifier.fillMaxSize().background(Color(0xFF3D2010)))
                else -> SubcomposeAsyncImageContent()
            }
        }

        // Gradiente inferior
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xDD000000)),
                        startY = 120f
                    )
                )
        )

        // Botón atrás
        IconButton(
            onClick  = onNavigateBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 8.dp)
                .align(Alignment.TopStart)
                .size(48.dp) // ESCALA: Aumentamos el área del botón
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint               = Color.White,
                modifier = Modifier.size(26.dp) // ESCALA: Icono ligeramente más grande
            )
        }

        // Nombre de la receta sobre la imagen
        Text(
            text       = nombre,
            fontSize   = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = Color.White,
            modifier   = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        )
    }
}

// ─── Chips de información ─────────────────────────────────────────────────────
@Composable
private fun InfoChips(recipe: Recipe) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoChipItem(
                icon  = "⏱",
                label = "TIEMPO",
                value = "${recipe.tiempoMinutos} min"
            )
            InfoChipDivider()
            InfoChipItem(
                icon  = "🍽",
                label = "PORCIONES",
                value = "${recipe.porciones} Personas"
            )
            InfoChipDivider()
            InfoChipItem(
                icon  = "📊",
                label = "NIVEL",
                value = recipe.nivel
            )
        }
    }
}

@Composable
private fun InfoChipDivider() {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(ColorChipBg)
    )
}

@Composable
private fun InfoChipItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color      = ColorBodyText.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text       = value,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            color      = ColorDarkBrown
        )
    }
}

// ─── Item de ingrediente con precio ──────────────────────────────────────────
@Composable
private fun IngredienteItem(ingrediente: IngredienteResuelto) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = ColorCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier  = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen del ingrediente
            SubcomposeAsyncImage(
                model              = ingrediente.imagen,
                contentDescription = ingrediente.nombre,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFD6C5B5)))
                    is AsyncImagePainter.State.Error ->
                        Box(
                            Modifier.fillMaxSize().background(ColorChipBg),
                            contentAlignment = Alignment.Center
                        ) { Text("🥘", fontSize = 20.sp) }
                    else -> SubcomposeAsyncImageContent()
                }
            }

            // Nombre y cantidad
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = ingrediente.nombre,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = ColorDarkBrown
                )
                Text(
                    text    = ingrediente.cantidad,
                    fontSize = 12.sp,
                    color   = ColorBodyText.copy(alpha = 0.7f)
                )
            }

            // Precio
            Text(
                text       = "$${"%,.0f".format(ingrediente.precio).replace(",", ".")}",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = ColorGold
            )
        }
    }
}

// ─── Item simple cuando no hay precio disponible ──────────────────────────────
@Composable
private fun IngredienteItemSimple(nombre: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(ColorGold)
        )
        Text(text = nombre, fontSize = 14.sp, color = ColorBodyText)
    }
}

// ─── Footer fijo con costo total ─────────────────────────────────────────────
@Composable
private fun RecipeFooter(costoTotal: Double, modifier: Modifier = Modifier) {
    Surface(
        modifier  = modifier.fillMaxWidth(),
        color     = ColorDarkBrown,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text     = "COSTO TOTAL DE LA RECETA",
                    fontSize = 10.sp,
                    color    = ColorGold,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text       = "COP $${"%,.0f".format(costoTotal).replace(",", ".")}",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White
                )
            }

            Button(
                onClick = { /* TODO: Agregar a lista de compras */ },
                shape   = RoundedCornerShape(50),
                colors  = ButtonDefaults.buttonColors(containerColor = ColorGold)
            ) {
                Text(
                    text       = "Agregar",
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    fontSize   = 15.sp
                )
            }
        }
    }
}
