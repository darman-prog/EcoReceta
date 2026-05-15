package eco.receta.app.features.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eco.receta.app.core.components.EcoBottomNavBar
import eco.receta.app.core.navigation.Routes
import eco.receta.app.data.model.Recipe

// ─── Paleta EcoReceta ────────────────────────────────────────────────────────

private val Crema = Color(0xFFF6EFE9)
private val MarronOscuro = Color(0xFF2C1A0E)
private val Dorado = Color(0xFFC8922A)
private val Rojo = Color(0xFFD94F3D)
private val TarjetaBg = Color(0xFFFFF8F2)
private val CampoFondo = Color(0xFFEDE8DF)
private val GrisTexto = Color(0xFF8D8D8D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    fun navigateBottom(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.PROFILE) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = Crema,
        topBar = {
            ProfileTopBar(
                onLogoutClick = { showLogoutDialog = true }
            )
        },
        bottomBar = {
            EcoBottomNavBar(
                currentRoute = Routes.PROFILE,
                onHomeClick = { navigateBottom(Routes.HOME) },
                onExploreClick = { navigateBottom(Routes.EXPLORE) },
                onCreateClick = { navigateBottom(Routes.CREATE) },
                onProfileClick = {}
            )
        }
    ) { padding ->

        when {
            uiState.isLoading -> {
                LoadingProfileState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            uiState.user == null -> {
                ErrorProfileState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            else -> {
                val user = uiState.user!!
                val recetas = uiState.recetasPublicas + uiState.recetasPrivadas

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeroSection(
                        nombreCompleto = user.nombreCompleto,
                        fotoUrl = user.fotoUrl,
                        badgeActual = user.badgeActual
                    )

                    ProfileStatsCard(
                        recetasCreadas = user.recetasCreadas,
                        recetasParaSiguienteRango = uiState.recetasParaSiguienteRango,
                        proximoBadge = uiState.proximoBadge,
                        progresoRango = uiState.progresoRango
                    )

                    MyCreationsHeader(
                        onSeeAllClick = {
                            navController.navigate("${Routes.EXPLORE}?filter=mis_recetas")
                        }
                    )

                    if (recetas.isEmpty()) {
                        EmptyRecipesState(
                            onCreateClick = {
                                navController.navigate(Routes.CREATE)
                            }
                        )
                    } else {
                        MyRecipesRow(
                            recetasPublicas = uiState.recetasPublicas,
                            recetasPrivadas = uiState.recetasPrivadas,
                            onRecipeClick = { recipeId ->
                                navController.navigate(recipeDetailRoute(recipeId))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = {
                showLogoutDialog = false
            },
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false

                navController.navigate(Routes.LOGIN) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfileTopBar(
    onLogoutClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EcoRecetaLogo()

            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = Rojo
                )
            }
        }
    }
}

@Composable
private fun EcoRecetaLogo() {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = MarronOscuro,
                    fontWeight = FontWeight.ExtraBold
                )
            ) {
                append("Eco")
            }

            withStyle(
                SpanStyle(
                    color = Dorado,
                    fontWeight = FontWeight.ExtraBold
                )
            ) {
                append("Receta")
            }
        },
        fontSize = 22.sp
    )
}

// ─── Estados de pantalla ─────────────────────────────────────────────────────

@Composable
private fun LoadingProfileState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Dorado)
    }
}

@Composable
private fun ErrorProfileState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error cargando perfil",
            color = Rojo,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Hero Perfil ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroSection(
    nombreCompleto: String,
    fotoUrl: String,
    badgeActual: String
) {
    val badgeBackground = getBadgeBackgroundColor(badgeActual)
    val badgeTextColor = getBadgeTextColor(badgeActual)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Crema)
                )
            )
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileAvatar(fotoUrl = fotoUrl)

            Text(
                text = nombreCompleto,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MarronOscuro,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            BadgePill(
                badgeActual = badgeActual,
                backgroundColor = badgeBackground,
                textColor = badgeTextColor
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    fotoUrl: String
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Dorado, MarronOscuro)
                )
            )
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(CampoFondo),
            contentAlignment = Alignment.Center
        ) {
            if (fotoUrl.isNotBlank()) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuario sin foto",
                    tint = MarronOscuro,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}

@Composable
private fun BadgePill(
    badgeActual: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = "Rango del usuario",
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = badgeActual.uppercase(),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Estadísticas ────────────────────────────────────────────────────────────

@Composable
private fun ProfileStatsCard(
    recetasCreadas: Int,
    recetasParaSiguienteRango: Int,
    proximoBadge: String,
    progresoRango: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MarronOscuro),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecipesCreatedSummary(
                recetasCreadas = recetasCreadas
            )

            if (recetasParaSiguienteRango > 0) {
                NextBadgeProgress(
                    recetasParaSiguienteRango = recetasParaSiguienteRango,
                    proximoBadge = proximoBadge,
                    progresoRango = progresoRango
                )
            } else {
                MaxBadgeReached()
            }
        }
    }
}

@Composable
private fun RecipesCreatedSummary(
    recetasCreadas: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = recetasCreadas.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 52.sp
            )

            Text(
                text = "RECETAS CREADAS",
                color = Dorado,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏆",
                fontSize = 28.sp
            )
        }
    }
}

@Composable
private fun NextBadgeProgress(
    recetasParaSiguienteRango: Int,
    proximoBadge: String,
    progresoRango: Float
) {
    val progreso by animateFloatAsState(
        targetValue = progresoRango.coerceIn(0f, 1f),
        label = "profile_badge_progress"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hacia: $proximoBadge",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$recetasParaSiguienteRango más",
                color = Dorado,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Dorado,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun MaxBadgeReached() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Dorado.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "⭐",
                fontSize = 14.sp
            )

            Text(
                text = "¡Rango máximo alcanzado!",
                color = Dorado,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// ─── Mis Creaciones ──────────────────────────────────────────────────────────

@Composable
private fun MyCreationsHeader(
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mis Creaciones",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MarronOscuro
        )

        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "Ver todas",
                color = Dorado,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MyRecipesRow(
    recetasPublicas: List<Recipe>,
    recetasPrivadas: List<Recipe>,
    onRecipeClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            items = recetasPublicas,
            key = { recipe -> "public_${recipe.id}" }
        ) { recipe ->
            RecetaCard(
                recipe = recipe,
                badge = "🌍 PÚBLICA",
                badgeColor = Dorado,
                onClick = {
                    onRecipeClick(recipe.id)
                }
            )
        }

        items(
            items = recetasPrivadas,
            key = { recipe -> "private_${recipe.id}" }
        ) { recipe ->
            RecetaCard(
                recipe = recipe,
                badge = "🔒 PRIVADA",
                badgeColor = MarronOscuro,
                onClick = {
                    onRecipeClick(recipe.id)
                }
            )
        }
    }
}

@Composable
private fun EmptyRecipesState(
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TarjetaBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🍳",
                    fontSize = 32.sp
                )

                Text(
                    text = "Aún no has creado recetas",
                    color = GrisTexto,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "¡Comparte tu primer plato con la comunidad!",
                    color = GrisTexto,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = onCreateClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Rojo)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Crear mi primera receta",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecetaCard(
    recipe: Recipe,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(155.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBg),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            RecipeImageSection(
                imageUrl = recipe.imageUrl,
                badge = badge,
                badgeColor = badgeColor
            )

            RecipeInfoSection(recipe = recipe)
        }
    }
}

@Composable
private fun RecipeImageSection(
    imageUrl: String,
    badge: String,
    badgeColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(CampoFondo)
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen de receta",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍽",
                    fontSize = 28.sp
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = badgeColor.copy(alpha = 0.9f)
        ) {
            Text(
                text = badge,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun RecipeInfoSection(
    recipe: Recipe
) {
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Text(
            text = recipe.nombre,
            fontWeight = FontWeight.Bold,
            color = MarronOscuro,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(3.dp))

        if (recipe.tiempoMinutos > 0) {
            Text(
                text = "⏱ ${recipe.tiempoMinutos} min",
                color = GrisTexto,
                fontSize = 11.sp
            )
        }

        if (recipe.costoTotal > 0) {
            Text(
                text = "COP $${"%,.0f".format(recipe.costoTotal)}",
                fontWeight = FontWeight.Bold,
                color = Dorado,
                fontSize = 12.sp
            )
        }
    }
}

// ─── Diálogo Logout ──────────────────────────────────────────────────────────

@Composable
private fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "¿Cerrar sesión?",
                fontWeight = FontWeight.Bold,
                color = MarronOscuro
            )
        },
        text = {
            Text(
                text = "¿Estás seguro? Tendrás que volver a iniciar sesión para acceder a tu despensa.",
                color = GrisTexto
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Rojo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cerrar sesión",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Cancelar")
            }
        }
    )
}

// ─── Utilidades ──────────────────────────────────────────────────────────────

private fun getBadgeBackgroundColor(
    badge: String
): Color = when (badge) {
    "Chef Novato" -> Color(0xFFF5E6D0)
    "Cocinero Aficionado" -> Color(0xFFE8F5E9)
    "Maestro Culinario" -> Color(0xFFFFF9C4)
    else -> CampoFondo
}

private fun getBadgeTextColor(
    badge: String
): Color = when (badge) {
    "Chef Novato" -> Color(0xFF8B5E3C)
    "Cocinero Aficionado" -> Color(0xFF2E7D32)
    "Maestro Culinario" -> Color(0xFFF57F17)
    else -> MarronOscuro
}

private fun recipeDetailRoute(
    recipeId: String
): String {
    return "recipe_detail/$recipeId"
}
