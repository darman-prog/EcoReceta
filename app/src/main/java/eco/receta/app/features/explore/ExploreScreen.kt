package eco.receta.app.features.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import eco.receta.app.core.components.EcoBottomNavBar
import eco.receta.app.data.model.Recipe
import eco.receta.app.features.home.ColorDarkBrown

// ─── Paleta EcoReceta ────────────────────────────────────────────────────────
private val Crema         = Color(0xFFF6EFE9)
private val MarronOscuro  = Color(0xFF2C1A0E)
private val MarronMedio   = Color(0xFF5C3D2E)
private val Dorado        = Color(0xFFC8922A)
private val Rojo          = Color(0xFFD94F3D)
private val TarjetaBg     = Color(0xFFFFF8F2)
private val CampoFondo    = Color(0xFFE3E0DA)
private val GrisTexto     = Color(0xFFB0A9A9)
private val BlancoCálido  = Color(0xFFFFFBF7)

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Animación de entrada única
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )

    Scaffold(
        containerColor = Crema,
        bottomBar = {
            EcoBottomNavBar(
                currentRoute   = "explore",
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
                .graphicsLayer { this.alpha = alpha }
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── TopBar ───────────────────────────────────────────────────
            item { ExploreTopBar() }

            // ── Hero Header ──────────────────────────────────────────────
            item { ExploreHeroHeader() }

            // ── Chips de categoría ───────────────────────────────────────
            item {
                CategoryChips(
                    categorias          = viewModel.categorias,
                    categoriaActiva     = state.categoriaActiva,
                    onCategoriaSelected = viewModel::onCategoriaSelected
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Sección: Recetas EcoReceta ───────────────────────────────
            item {
                SectionHeader(
                    titulo   = "Recetas EcoReceta",
                    subtitulo = "Creadas por nuestro equipo",
                    emoji    = "⭐"
                )
            }

            when (val sistema = state.recetasSistema) {
                is ExploreState.Loading -> item { LoadingContent() }
                is ExploreState.Error   -> item { ErrorContent(sistema.message) }
                is ExploreState.Success -> {
                    if (sistema.recipes.isEmpty()) {
                        item { EmptyContent("Aún no hay recetas del equipo") }
                    } else {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(
                                    items = sistema.recipes,
                                    key   = { "sistema_${it.id}" }
                                ) { recipe ->
                                    SistemaRecipeCard(
                                        recipe  = recipe,
                                        onClick = { onRecipeClick(recipe.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Sección: De la Comunidad ─────────────────────────────────
            item {
                Spacer(Modifier.height(28.dp))
                SectionHeader(
                    titulo    = "De la Comunidad",
                    subtitulo = "Compartidas por usuarios como tú",
                    emoji     = "👨‍🍳"
                )
            }

            when (val comunidad = state.recetasComunidad) {
                is ExploreState.Loading -> item { LoadingContent() }
                is ExploreState.Error   -> item { ErrorContent(comunidad.message) }
                is ExploreState.Success -> {
                    if (comunidad.recipes.isEmpty()) {
                        item { EmptyContent("Sé el primero en compartir una receta") }
                    } else {
                        items(
                            items = comunidad.recipes,
                            key   = { "comunidad_${it.id}" }
                        ) { recipe ->
                            var itemVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { itemVisible = true }
                            AnimatedVisibility(
                                visible = itemVisible,
                                enter   = fadeIn(tween(300)) +
                                        slideInVertically { it / 3 }
                            ) {
                                ComunidadRecipeCard(
                                    recipe  = recipe,
                                    onClick = { onRecipeClick(recipe.id) },
                                    modifier = Modifier.padding(
                                        horizontal = 20.dp, vertical = 6.dp
                                    )
                                )
                            }
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
                withStyle(style = SpanStyle(color = Dorado)) {
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

// ─── Hero Header ─────────────────────────────────────────────────────────────
@Composable
private fun ExploreHeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(BlancoCálido, Crema)
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            // Creamos un estado para disparar la animación al entrar en la pantalla
            var visible by remember { mutableStateOf(false) }

            // Se activa apenas el componente se monta en la composición
            LaunchedEffect(Unit) {
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000)) // Duración de 1 segundo
            ) {
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Sabores que\n")
                            withStyle(SpanStyle(
                                color     = Dorado,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.ExtraBold
                            )) { append("inspiran.") }
                        },
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MarronOscuro,
                        lineHeight = 40.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Descubre recetas colombianas auténticas.",
                        fontSize = 14.sp,
                        color = GrisTexto
                    )
                }
            }
        }


        // Decoración — círculo dorado
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Dorado.copy(0.15f), Color.Transparent)
                    )
                )
        )
    }
}

// ─── Chips de categoría ───────────────────────────────────────────────────────
@Composable
private fun CategoryChips(
    categorias: List<String>,
    categoriaActiva: String,
    onCategoriaSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categorias.forEach { categoria ->
            val isActive = categoria == categoriaActiva

            val bgColor by animateColorAsState(
                targetValue = if (isActive) MarronOscuro else CampoFondo,
                animationSpec = tween(250),
                label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color.White else MarronMedio,
                animationSpec = tween(250),
                label = "chipText"
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
                    .height(36.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale },
                shadowElevation = if (isActive) 4.dp else 0.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = categoria,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold
                        else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

// ─── Encabezado de sección ────────────────────────────────────────────────────
@Composable
private fun SectionHeader(titulo: String, subtitulo: String, emoji: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ícono con fondo
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CampoFondo),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Column {
            Text(
                titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MarronOscuro
            )
            Text(
                subtitulo,
                fontSize = 12.sp,
                color = GrisTexto
            )
        }
    }
    Spacer(Modifier.height(12.dp))
}

// ─── Tarjeta sistema (horizontal scroll) ─────────────────────────────────────
@Composable
private fun SistemaRecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.width(185.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = TarjetaBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                SubcomposeAsyncImage(
                    model              = recipe.imageUrl,
                    contentDescription = recipe.nombre,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading ->
                            Box(Modifier.fillMaxSize().background(CampoFondo))
                        is AsyncImagePainter.State.Error ->
                            Box(
                                Modifier.fillMaxSize().background(MarronOscuro),
                                contentAlignment = Alignment.Center
                            ) { Text("🍽", fontSize = 28.sp) }
                        else -> SubcomposeAsyncImageContent()
                    }
                }
                // Gradiente inferior
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, MarronOscuro.copy(0.6f)),
                                startY = 60f
                            )
                        )
                )
                // Badge oficial
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Dorado
                ) {
                    Text(
                        "OFICIAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    recipe.categoria.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Dorado,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    recipe.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MarronOscuro,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⏱ ${recipe.tiempoMinutos} min",
                        fontSize = 11.sp,
                        color = GrisTexto
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Rojo.copy(0.1f)
                    ) {
                        Text(
                            "$${"%,.0f".format(recipe.costoTotal)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Rojo,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Tarjeta comunidad (lista vertical) ──────────────────────────────────────
@Composable
private fun ComunidadRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = TarjetaBg),
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
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                SubcomposeAsyncImage(
                    model              = recipe.imageUrl,
                    contentDescription = recipe.nombre,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading ->
                            Box(Modifier.fillMaxSize().background(CampoFondo))
                        is AsyncImagePainter.State.Error ->
                            Box(
                                Modifier.fillMaxSize().background(MarronOscuro),
                                contentAlignment = Alignment.Center
                            ) { Text("🍽", fontSize = 24.sp) }
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {

                // Categoría
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Dorado.copy(0.12f)
                ) {
                    Text(
                        recipe.categoria.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Dorado,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.height(5.dp))

                // Nombre
                Text(
                    recipe.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MarronOscuro,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(5.dp))

                // Autor
                if (recipe.autorNombre.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                ,
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 8.sp)
                        }
                        Text(
                            text = recipe.autorNombre
                                .split(" ")
                                .take(2)
                                .joinToString(" "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GrisTexto,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                }

                // Fila inferior: tiempo + precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⏱ ${recipe.tiempoMinutos} min",
                        fontSize = 11.sp,
                        color = GrisTexto
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Rojo.copy(0.1f)
                    ) {
                        Text(
                            "$${"%,.0f".format(recipe.costoTotal)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Rojo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Estados ──────────────────────────────────────────────────────────────────
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color       = Dorado,
            strokeWidth = 3.dp,
            modifier    = Modifier.size(36.dp)
        )
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Rojo.copy(0.08f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚠️", fontSize = 16.sp)
                Text(message, color = Rojo, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun EmptyContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CampoFondo
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🍳", fontSize = 28.sp)
                Text(
                    message,
                    color = GrisTexto,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}