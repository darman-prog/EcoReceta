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
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eco.receta.app.core.components.EcoBottomNavBar
import eco.receta.app.core.navigation.Routes
import eco.receta.app.data.model.Recipe
import eco.receta.app.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,  // ← Ya lo recibes
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "EcoReceta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VerdeBosque
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CremaClaro
                )
            )
        },
        bottomBar = {
            EcoBottomNavBar(
                currentRoute = Routes.PROFILE,

                onHomeClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.CREATE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },

                onExploreClick = {
                    navController.navigate(Routes.EXPLORE) {
                        popUpTo(Routes.CREATE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },

                onCreateClick = {
                    navController.navigate(Routes.CREATE){
                        popUpTo(Routes.CREATE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },

                onProfileClick = {}
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VerdeBosque)
            }
            return@Scaffold
        }

        val user = uiState.user
        if (user == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error cargando perfil", color = GrisPiedra)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // 1. AVATAR (dinámico: foto de Google o placeholder)
            // ═══════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8E0D5)),
                contentAlignment = Alignment.Center
            ) {
                if (user.fotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.fotoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = VerdeBosque,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════════════════
            // 2. NOMBRE Y BADGE REALES (reemplaza lo estático del Figma)
            // ═══════════════════════════════════════════════════════
            Text(
                text = user.nombreCompleto,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3D3D3D),
                textAlign = TextAlign.Center
            )

            // Badge con ícono de trofeo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = getBadgeColor(user.badgeActual),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = user.badgeActual,
                    style = MaterialTheme.typography.labelLarge,
                    color = getBadgeColor(user.badgeActual),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // 3. CARD DE STATS CON CONTADOR REAL Y PROGRESO
            // ═══════════════════════════════════════════════════════
            StatsCard(
                recetasCreadas = user.recetasCreadas,
                badgeActual = user.badgeActual,
                proximoBadge = uiState.proximoBadge,
                recetasFaltantes = uiState.recetasParaSiguienteRango,
                progreso = uiState.progresoRango
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ═══════════════════════════════════════════════════════
            // 4. MIS CREACIONES
            // ═══════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mis Creaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3D3D3D)
                )
                TextButton(
                    onClick = {
                        navController.navigate("${Routes.EXPLORE}?filter=mis_recetas")
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = VerdeBosque)
                ) {
                    Text("Ver todas", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val todasLasRecetas = uiState.recetasPublicas + uiState.recetasPrivadas

            if (todasLasRecetas.isEmpty()) {
                EmptyRecipesState()
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.recetasPublicas) { recipe ->
                        MiRecetaCard(
                            recipe = recipe,
                            badge = "COMUNIDAD",
                            onClick = {
                                navController.navigate("recipe_detail/${recipe.id}")
                            }
                        )
                    }
                    items(uiState.recetasPrivadas) { recipe ->
                        MiRecetaCard(
                            recipe = recipe,
                            badge = "PRIVADA",
                            badgeColor = Color(0xFF8D6E63),
                            onClick = {
                                navController.navigate("recipe_detail/${recipe.id}")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ═══════════════════════════════════════════════════════
            // 5. CERRAR SESIÓN
            // ═══════════════════════════════════════════════════════
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD32F2F)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD32F2F))
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Cerrar Sesión",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Diálogo logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("¿Estás seguro de que deseas salir de tu despensa?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// COMPONENTES ESPECÍFICOS DE STATS Y BADGES
// ═════════════════════════════════════════════════════════════════

@Composable
private fun StatsCard(
    recetasCreadas: Int,
    badgeActual: String,
    proximoBadge: String,
    recetasFaltantes: Int,
    progreso: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeBosque),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Fila superior: Número grande + info del badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = recetasCreadas.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 40.sp
                    )
                    Text(
                        text = "RECETAS CREADAS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp
                    )
                }

                // Badge actual con fondo destacado
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = badgeActual.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Barra de progreso hacia siguiente rango
            if (recetasFaltantes > 0) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progreso hacia $proximoBadge",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$recetasFaltantes recetas más",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ProgressBar animada
                    val animatedProgress by animateFloatAsState(
                        targetValue = progreso,
                        label = "progress"
                    )

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFFD700),
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                }
            } else {
                // Usuario en rango máximo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¡Has alcanzado el rango máximo!",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// COLORES DE BADGES
// ═════════════════════════════════════════════════════════════════
@Composable
private fun getBadgeColor(badge: String): Color = when (badge) {
    "Chef Novato" -> Color(0xFF8D6E63)
    "Cocinero Aficionado" -> Color(0xFF4CAF50)
    "Maestro Culinario" -> Color(0xFFFFD700)
    else -> VerdeBosque
}

@Composable
private fun EmptyRecipesState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F0E8)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = GrisPiedra,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Aún no has creado recetas",
                color = GrisPiedra
            )
            Text(
                "¡Ve a CREAR y comparte tu primer plato!",
                color = GrisPiedra,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MiRecetaCard(
    recipe: Recipe,
    badge: String,
    badgeColor: Color = VerdeBosque,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFFF5F0E8))
            ) {
                if (recipe.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = GrisPiedra,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.9f))
                        .align(Alignment.TopStart)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        badge,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.nombre,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3D3D3D),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (recipe.tiempoMinutos > 0) "${recipe.tiempoMinutos} min" else "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisPiedra
                )
                if (recipe.costoTotal > 0) {
                    Text(
                        text = "COP $${"%,.0f".format(recipe.costoTotal)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeBosque
                    )
                }
            }
        }
    }
}