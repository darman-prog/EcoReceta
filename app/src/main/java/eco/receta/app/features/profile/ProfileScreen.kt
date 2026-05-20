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
import androidx.compose.material.icons.filled.*
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
private val Crema        = Color(0xFFF6EFE9)
private val MarronOscuro = Color(0xFF2C1A0E)
private val Dorado       = Color(0xFFC8922A)
private val Rojo         = Color(0xFFD94F3D)
private val TarjetaBg    = Color(0xFFFFF8F2)
private val CampoFondo   = Color(0xFFEDE8DF)
private val GrisTexto    = Color(0xFF8D8D8D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Crema,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo EcoReceta
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
                        fontSize = 22.sp
                    )
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Rojo
                        )
                    }
                }
            }
        },
        bottomBar = {
            EcoBottomNavBar(
                currentRoute   = Routes.PROFILE,
                onHomeClick    = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PROFILE) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                },
                onExploreClick = {
                    navController.navigate(Routes.EXPLORE) {
                        popUpTo(Routes.PROFILE) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                },
                onCreateClick  = {
                    navController.navigate(Routes.CREATE) {
                        popUpTo(Routes.PROFILE) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                },
                onProfileClick = {}
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Dorado) }
            return@Scaffold
        }

        val user = uiState.user ?: run {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Error cargando perfil", color = Rojo) }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── HERO: Avatar + nombre + badge ────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White, Crema)
                        )
                    )
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar con borde dorado
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Dorado, MarronOscuro))
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
                            if (user.fotoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = user.fotoUrl,
                                    contentDescription = "Foto",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = MarronOscuro,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Text(
                        user.nombreCompleto,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MarronOscuro,
                        textAlign = TextAlign.Center
                    )

                    // Badge con fondo coloreado
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = getBadgeBackgroundColor(user.badgeActual)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Outlined.EmojiEvents,
                                null,
                                tint = getBadgeTextColor(user.badgeActual),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                user.badgeActual.uppercase(),
                                color = getBadgeTextColor(user.badgeActual),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }

            // ── STATS CARD ───────────────────────────────────────────────
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                user.recetasCreadas.toString(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 52.sp
                            )
                            Text(
                                "RECETAS CREADAS",
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
                                .background(Color.White.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 28.sp)
                        }
                    }

                    if (uiState.recetasParaSiguienteRango > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Hacia: ${uiState.proximoBadge}",
                                    color = Color.White.copy(0.8f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    "${uiState.recetasParaSiguienteRango} más",
                                    color = Dorado,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            val progreso by animateFloatAsState(
                                uiState.progresoRango, label = "progress"
                            )
                            LinearProgressIndicator(
                                progress = { progreso },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Dorado,
                                trackColor = Color.White.copy(0.2f)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Dorado.copy(0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("⭐", fontSize = 14.sp)
                                Text(
                                    "¡Rango máximo alcanzado!",
                                    color = Dorado,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── MIS CREACIONES ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mis Creaciones",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MarronOscuro
                )
                TextButton(onClick = {
                    navController.navigate("${Routes.HOME}?filter=mis_recetas")
                }) {
                    Text("Ver todas", color = Dorado, fontWeight = FontWeight.SemiBold)
                }
            }

            val todasLasRecetas = uiState.recetasPublicas + uiState.recetasPrivadas
            if (todasLasRecetas.isEmpty()) {
                EmptyRecipesState(
                    onCreateClick = { navController.navigate(Routes.CREATE) }
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.recetasPublicas) { recipe ->
                        RecetaCard(
                            recipe = recipe,
                            badge = "🌍 PÚBLICA",
                            badgeColor = Dorado,
                            onClick = { navController.navigate("recipe_detail/${recipe.id}") }
                        )
                    }
                    items(uiState.recetasPrivadas) { recipe ->
                        RecetaCard(
                            recipe = recipe,
                            badge = "🔒 PRIVADA",
                            badgeColor = MarronOscuro,
                            onClick = { navController.navigate("recipe_detail/${recipe.id}") }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Diálogo logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "¿Cerrar sesión?",
                    fontWeight = FontWeight.Bold,
                    color = MarronOscuro
                )
            },
            text = {
                Text(
                    "¿Estás seguro? Tendrás que volver a iniciar sesión para acceder a tu despensa.",
                    color = GrisTexto
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rojo),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cerrar sesión", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancelar") }
            }
        )
    }
}

// ─── Componentes ──────────────────────────────────────────────────────────────

@Composable
private fun getBadgeBackgroundColor(badge: String): Color = when (badge) {
    "Chef Novato"         -> Color(0xFFF5E6D0)
    "Cocinero Aficionado" -> Color(0xFFE8F5E9)
    "Maestro Culinario"   -> Color(0xFFFFF9C4)
    else -> CampoFondo
}

@Composable
private fun getBadgeTextColor(badge: String): Color = when (badge) {
    "Chef Novato"         -> Color(0xFF8B5E3C)
    "Cocinero Aficionado" -> Color(0xFF2E7D32)
    "Maestro Culinario"   -> Color(0xFFF57F17)
    else -> MarronOscuro
}

@Composable
private fun EmptyRecipesState(onCreateClick: () -> Unit) {
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
                Text("🍳", fontSize = 32.sp)
                Text(
                    "Aún no has creado recetas",
                    color = GrisTexto,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "¡Comparte tu primer plato con la comunidad!",
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
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Crear mi primera receta", fontWeight = FontWeight.Bold)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(CampoFondo)
            ) {
                if (recipe.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("🍽", fontSize = 28.sp)
                    }
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(0.9f)
                ) {
                    Text(
                        badge,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    recipe.nombre,
                    fontWeight = FontWeight.Bold,
                    color = MarronOscuro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(3.dp))
                if (recipe.tiempoMinutos > 0) {
                    Text(
                        "⏱ ${recipe.tiempoMinutos} min",
                        color = GrisTexto,
                        fontSize = 11.sp
                    )
                }
                if (recipe.costoTotal > 0) {
                    Text(
                        "COP $${"%,.0f".format(recipe.costoTotal)}",
                        fontWeight = FontWeight.Bold,
                        color = Dorado,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}