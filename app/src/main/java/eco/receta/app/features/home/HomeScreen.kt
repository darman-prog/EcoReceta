// eco/receta/app/features/home/HomeScreen.kt

package eco.receta.app.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
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
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.firebase.auth.FirebaseAuth
import eco.receta.app.core.components.EcoBottomNavBar
import eco.receta.app.data.model.Recipe

// ─── Colores del Figma ───────────────────────────────────────────────────────
private val ColorCream     = Color(0xFFF6EFED)
val ColorDarkBrown = Color(0xFF2C1A0E)
private val ColorRed       = Color(0xFFD94F3D)
val ColorGold      = Color(0xFFC8922A)
private val ColorBodyText  = Color(0xFF5C4033)
private val ColorFieldBg   = Color(0xFFEDE8DF)
private val ColorCardBg    = Color(0xFFF8F6F6)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToExplore: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Nombre del usuario autenticado (Google o Email)
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Chef"

    Scaffold(
        containerColor = ColorCream,
        bottomBar = {
            EcoBottomNavBar(
                currentRoute   = "home",
                onHomeClick    = {},
                onExploreClick = onNavigateToExplore,
                onCreateClick  = onNavigateToCreate,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->



        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espaciado consistente (Grid 8dp)
        ) {
            // ── TopBar con foto de perfil real ───────────────────────────
            item {
                HomeTopBar(
                    userName    = userName,
                    photoUrl    = currentUser?.photoUrl?.toString(),
                    onLogout    = onLogout
                )
            }

            // ── Título ───────────────────────────────────────────────────
            item {
                val visible = remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    visible.value = true
                }

                AnimatedVisibility(
                    visible = visible.value,
                    enter = fadeIn() + slideInVertically { it / 3 }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text(
                            text = "¿Qué cocinamos hoy?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorDarkBrown
                        )
                    }
                }
            }

            // ── Barra de búsqueda ────────────────────────────────────────
            item {
                HomeSearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Sección: Recetas del Sistema ─────────────────────────────
            item {
                SectionHeader(
                    title   = "Inspiración del Día",
                    onVerTodo = onNavigateToExplore
                )
            }

            // ── Manejo de estados Loading / Success / Error ───────────────
            when (val sistemaState = state.recetasSistema) {

                is RecipesState.Loading -> item {
                    LoadingContent()
                }

                is RecipesState.Error -> item {
                    ErrorContent(message = sistemaState.message)
                }

                is RecipesState.Success -> {
                    val recetas = sistemaState.recipes

                    if (recetas.isEmpty()) {
                        item { EmptyContent() }
                    } else {
                        // Primera receta: tarjeta grande destacada
                        item {
                            FeaturedRecipeCard(
                                recipe  = recetas.first(),
                                onClick = { onRecipeClick(recetas.first().id) },
                                modifier = Modifier.padding(
                                    horizontal = 20.dp, vertical = 12.dp
                                )
                            )
                        }

                        // Resto: lista de tarjetas pequeñas
                        items(
                            items = recetas.drop(1),
                            key = { "sistema_${it.id}_${it.tipoOrigen}" }
                        ) { recipe ->

                        if (recipe != recetas.first()) {
                            LocalRecipeItem(
                                recipe = recipe,
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

            // ── Sección: Mi Recetario Privado ────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Mi Recetario", onVerTodo = onNavigateToProfile)
            }

            when (val privadasState = state.recetasPrivadas) {
                is RecipesState.Loading -> item { LoadingContent() }
                is RecipesState.Error   -> item { ErrorContent(message = privadasState.message) }
                is RecipesState.Success -> {
                    if (privadasState.recipes.isEmpty()) {
                        item {
                            Text(
                                text = "Aún no tienes recetas guardadas.",
                                color = ColorBodyText,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        items(
                            items = privadasState.recipes,
                            key = { "privada_${it.id}_${it.creadoEn}" }  // creadoEn es timestamp único
                        ) { recipe ->
                            LocalRecipeItem(
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

// ─── TopBar con foto real de Google ──────────────────────────────────────────
@Composable
private fun HomeTopBar(
    userName: String,
    photoUrl: String?,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xBFF8F4F4))
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
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = ColorDarkBrown
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Hola, $userName",
                fontSize = 13.sp,
                color = ColorBodyText
            )

            // Foto de perfil de Google — si no tiene, muestra ícono
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ColorFieldBg),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = ColorBodyText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ─── Barra de búsqueda ───────────────────────────────────────────────────────
@Composable
private fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focused = remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (focused.value) ColorGold else Color.Transparent,
        label = "searchBorder"
    )

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Buscar recetas o ingredientes",
                color = Color(0xFF9E8E7E),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = ColorGold
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focused.value = it.isFocused },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = ColorFieldBg,
            focusedContainerColor   = ColorFieldBg,
            unfocusedBorderColor    = Color.Transparent,
            focusedBorderColor      = borderColor
        )
    )
}

// ─── Encabezado de sección ───────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, onVerTodo: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorDarkBrown
        )
        TextButton(onClick = onVerTodo) {
            Text(
                text = "Ver todo",
                color = ColorGold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

// ─── Tarjeta destacada (grande) con Coil ─────────────────────────────────────
@Composable
private fun FeaturedRecipeCard(
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
    ){
        Card(
            onClick   = onClick,
            modifier  = modifier
                .fillMaxWidth()
                .height(220.dp),
            shape     = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ){
            Box(modifier = Modifier.fillMaxSize()) {

                // ── Imagen con Coil — muestra shimmer mientras carga ─────────
                SubcomposeAsyncImage(
                    model            = recipe.imageUrl,
                    contentDescription = recipe.nombre,
                    contentScale     = ContentScale.Crop,
                    modifier         = Modifier.fillMaxSize()
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            // Shimmer mientras carga la imagen
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF8F7F6))
                            )
                        }
                        is AsyncImagePainter.State.Error -> {
                            // Placeholder si falla la carga
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF3D2010))
                            )
                        }
                        else -> SubcomposeAsyncImageContent()
                    }
                }

                // ── Gradiente para legibilidad del texto ─────────────────────
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

                // ── Badge precio ─────────────────────────────────────────────
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape    = RoundedCornerShape(50),
                    color    = ColorRed
                ) {
                    Text(
                        text     = "$${"%,.0f".format(recipe.costoTotal).replace(",", ".")}",
                        color    = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // ── Texto inferior ───────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text     = recipe.nombre,
                        color    = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text  = "⏱ ${recipe.tiempoMinutos} min",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                        Text("•", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                        Text(
                            text  = "🍴 ${recipe.nivel}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

}

// ─── Tarjeta pequeña de lista con Coil ───────────────────────────────────────
@Composable
private fun LocalRecipeItem(
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
            modifier  = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Imagen pequeña con Coil ──────────────────────────────────
            SubcomposeAsyncImage(
                model            = recipe.imageUrl,
                contentDescription = recipe.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading ->
                        Box(Modifier
                            .fillMaxSize()
                            .background(Color(0xFFD6C5B5)))
                    is AsyncImagePainter.State.Error ->
                        Box(Modifier
                            .fillMaxSize()
                            .background(Color(0xFF8B5E3C)))
                    else -> SubcomposeAsyncImageContent()
                }
            }

            // ── Info ─────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "${recipe.region}  •  ${recipe.categoria}",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = ColorBodyText.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = recipe.nombre,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = ColorDarkBrown,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text       = "$${"%,.0f".format(recipe.costoTotal).replace(",", ".")}",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = ColorRed
                )
            }

            // ── Botón + ──────────────────────────────────────────────────

            val interactionSource = remember { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (pressed) ColorGold else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = if (pressed) Color.White else ColorGold,
                    modifier = Modifier.size(20.dp)
                )
            }

        }
    }
}

// ─── Estados visuales ─────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
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
            text      = "⚠️ $message",
            color     = ColorRed,
            fontSize  = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text     = "No hay recetas disponibles aún.",
            color    = ColorBodyText,
            fontSize = 14.sp
        )
    }
}
