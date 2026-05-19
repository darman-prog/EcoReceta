package eco.receta.app.features.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import eco.receta.app.core.components.EcoBottomNavBar
import eco.receta.app.core.utils.AuthorNameFormatter
import eco.receta.app.data.model.Recipe
import eco.receta.app.features.home.ColorGold
import eco.receta.app.features.recipes.create.GrisTexto

// ─── Colores  ───────────────────────────────────────────────────────
private val ColorCream     = Color(0xFFFAF3EE)
private val ColorDarkBrown = Color(0xFF2C1A0E)
private val ColorRed       = Color(0xFFD94F3D)
private val ColorBodyText  = Color(0xFF5C4033)
private val ColorCardBg    = Color(0xFFFFF8F2)
private val ColorChipActive = Color(0xFF2C1A0E)
private val ColorChipInactive = Color(0xFFEDE8DF)

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ColorCream,
        bottomBar = {
            EcoBottomNavBar(
                currentRoute   = "explore",  // ← EXPLORAR activo
                onHomeClick    = onNavigateToHome,
                onExploreClick = {},
                onCreateClick  = onNavigateToCreate,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── TopBar ───────────────────────────────────────────────────
            item {
                ExploreTopBar()
            }

            // ── Título
            item {
                val visible = remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    visible.value = true
                }

                AnimatedVisibility(
                    visible = visible.value,
                    enter = fadeIn() + slideInVertically { it / 3 }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        )
                    ) {
                        Text(
                            text = "Sabores que",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorDarkBrown
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = ColorGold,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                ) { append(" inspiran.") }
                            },
                            fontSize = 32.sp
                        )
                    }
                }
            }


            // ── Chips de categorías ──────────────────────────────────────
            item {
                CategoryChips(
                    categorias     = viewModel.categorias,
                    categoriaActiva = state.categoriaActiva,
                    onCategoriaSelected = viewModel::onCategoriaSelected,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ── Tarjeta destacada grande ─────────────────────────────────
            when (val dest = state.destacada) {
                is ExploreState.Loading -> item { LoadingCard() }
                is ExploreState.Error   -> item { ErrorContent(dest.message) }
                is ExploreState.Success -> {
                    dest.recipes.firstOrNull()?.let { receta ->
                        item {
                            DestacadaCard(
                                recipe  = receta,
                                onClick = { onRecipeClick(receta.id) },
                                modifier = Modifier.padding(
                                    horizontal = 20.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                    }
                }
            }

            // ── Encabezado sección populares ─────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Populares esta semana",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkBrown
                    )
                }
            }

            // ── Lista de populares ───────────────────────────────────────
            when (val pop = state.populares) {
                is ExploreState.Loading -> item { LoadingCard() }
                is ExploreState.Error   -> item { ErrorContent(pop.message) }
                is ExploreState.Success -> {
                    if (pop.recipes.isEmpty()) {
                        item {
                            Text(
                                text = "No hay recetas con este filtro.",
                                color = ColorBodyText,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(
                                    horizontal = 20.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                    } else {
                        items(
                            items = pop.recipes,
                            key   = { it.id }
                        ) { recipe ->
                            PopularRecipeItem(
                                recipe  = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                modifier = Modifier.padding(
                                    horizontal = 20.dp,
                                    vertical = 6.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── TopBar ──────────────────────────────────────────────────────────────────
@Composable
private fun ExploreTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                append("Eco")
                withStyle(style = SpanStyle(color = ColorGold)) {
                    append("Receta")
                }
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorDarkBrown
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = "Explorar Recetas",
                fontSize = 16.sp,
                color = GrisTexto,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

// ─── Chips de categoría ───────────────────────────────────────────────────────
@Composable
private fun CategoryChips(
    categorias: List<String>,
    categoriaActiva: String,
    onCategoriaSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categorias.forEach { categoria ->
            val isActive = categoria == categoriaActiva

            val bgColor by animateColorAsState(
                targetValue = if (isActive) ColorChipActive else ColorChipInactive,
                label = "chipColor"
            )

            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.05f else 1f,
                label = "chipScale"
            )

            Surface(
                onClick = { onCategoriaSelected(categoria) },
                shape = RoundedCornerShape(50),
                color = bgColor,
                modifier = Modifier
                    .height(34.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = categoria,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) Color.White else ColorBodyText
                    )
                }
            }
        }
    }
}

// ─── Tarjeta destacada grande ─────────────────────────────────────────────────

@Composable
private fun DestacadaCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
    ) {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn() + slideInVertically { it / 2 }
    ) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        )
        {
            Box(modifier = Modifier.fillMaxSize()) {

                // ── Imagen con Coil ──────────────────────────────────────────
                SubcomposeAsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading ->
                            Box(Modifier.fillMaxSize().background(Color(0xFFD6C5B5)))

                        is AsyncImagePainter.State.Error ->
                            Box(Modifier.fillMaxSize().background(Color(0xFF3D2010)))

                        else -> SubcomposeAsyncImageContent()
                    }
                }

                // ── Gradiente ────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xAA2C1A0E),
                                    Color(0xFF2C1A0E)
                                ),
                                startY = 120f
                            )
                        )
                )

                // ── Badge "RECOMENDADO" ───────────────────────────────────────
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 92.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = ColorGold
                ) {
                    Text(
                        text = "RECOMENDADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // ── Info inferior ────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recipe.nombre,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$${"%,.0f".format(recipe.costoTotal).replace(",", ".")}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Botón "Ver Receta"

                    val interactionSource = remember { MutableInteractionSource() }
                    val pressed by interactionSource.collectIsPressedAsState()

                    Button(
                        onClick = onClick,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(30),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pressed) ColorGold else Color(0xFF3D2E1A)
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Ver\nReceta",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── Item de lista "Populares" ────────────────────────────────────────────────
@Composable
private fun PopularRecipeItem(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = ColorCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Imagen ───────────────────────────────────────────────────
            SubcomposeAsyncImage(
                model              = recipe.imageUrl,
                contentDescription = recipe.nombre,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFD6C5B5)))
                    is AsyncImagePainter.State.Error ->
                        Box(Modifier.fillMaxSize().background(Color(0xFF8B5E3C)))
                    else -> SubcomposeAsyncImageContent()
                }
            }

            // ── Info ─────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                // Categoría
                Text(
                    text = recipe.categoria.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorGold,
                    letterSpacing = 1.sp
                )
                // Nombre
                Text(
                    text = recipe.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkBrown,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                //autor
                val isPublic = recipe.visibilidad.name.equals("publica", ignoreCase = true)
                if (isPublic) {
                    val autor = AuthorNameFormatter.format(recipe.autorNombre, maxChars = 18)
                    if (autor.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Por $autor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorBodyText.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text       = "$${"%,.0f".format(recipe.costoTotal).replace(",", ".")}",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = ColorRed
                )
            }
            Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⏱ ${recipe.tiempoMinutos} min",
                        fontSize = 12.sp,
                        color = ColorBodyText.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

// ─── Estados visuales ─────────────────────────────────────────────────────────
@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = ColorGold)
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⚠️ $message",
            color = ColorRed,
            fontSize = 14.sp
        )
    }
}