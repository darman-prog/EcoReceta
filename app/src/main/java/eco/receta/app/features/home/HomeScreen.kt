package eco.receta.app.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import eco.receta.app.data.model.Recipe
import eco.receta.app.ui.theme.EcoRecetaTheme

// ─── Colores del Figma ───────────────────────────────────────────────────────
private val ColorCream      = Color(0xFFFAF3EE)   // fondo general
private val ColorDarkBrown  = Color(0xFF2C1A0E)   // navbar inferior + textos fuertes
private val ColorRed        = Color(0xFFD94F3D)   // precio destacado + badge
private val ColorGold       = Color(0xFFC8922A)   // "Ver todo", íconos activos
private val ColorBodyText   = Color(0xFF5C4033)   // texto secundario
private val ColorFieldBg    = Color(0xFFEDE8DF)   // fondo barra de búsqueda
private val ColorCardBg     = Color(0xFFFFF8F2)   // fondo tarjetas lista

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToExplore: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    HomeContent(
        state = state,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onNavigateToExplore = onNavigateToExplore,
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToProfile = onNavigateToProfile,
        onRecipeClick = onRecipeClick
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    Scaffold(
        containerColor = ColorCream,
        bottomBar = {
            EcoBottomNavBar(
                currentRoute = "home",
                onHomeClick = {},
                onExploreClick = onNavigateToExplore,
                onCreateClick = onNavigateToCreate,
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

            // ── TopBar ──────────────────────────────────────────────────
            item {
                HomeTopBar()
            }

            // ── Título ──────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "¿Qué hay en la",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorDarkBrown,
                        lineHeight = 36.sp
                    )
                    Text(
                        text = "despensa hoy?",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorDarkBrown,
                        lineHeight = 36.sp
                    )
                }
            }

            // ── Barra de búsqueda ────────────────────────────────────────
            item {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Tarjeta destacada ────────────────────────────────────────
            item {
                state.featuredRecipe?.let { recipe ->
                    FeaturedRecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            // ── Encabezado sección ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explora Sabores Locales",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkBrown
                    )
                    TextButton(onClick = onNavigateToExplore) {
                        Text(
                            text = "Ver todo",
                            color = ColorGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ── Lista de recetas locales ─────────────────────────────────
            items(state.localRecipes) { recipe ->
                LocalRecipeItem(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ─── TopBar ──────────────────────────────────────────────────────────────────
@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "EcoReceta",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorDarkBrown
        )
        // Foto de perfil
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(ColorFieldBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Perfil",
                tint = ColorBodyText,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─── Barra de búsqueda ────────────────────────────────────────────────────────
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Busca ingredientes o recetas...",
                color = Color(0xE18A7D70),
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
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = ColorFieldBg,
            focusedContainerColor = ColorFieldBg,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = ColorGold
        )
    )
}

// ─── Tarjeta destacada (grande) ───────────────────────────────────────────────
@Composable
private fun FeaturedRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF3D2010)) // placeholder oscuro
            )

            // Gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC000000)),
                            startY = 80f
                        )
                    )
            )

            // Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(50),
                color = ColorRed
            ) {
                Text(
                    text = "$${"%,.0f".format(recipe.totalCost).replace(",", ".")}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Texto
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.name,
                    color = Color.White,
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
                        text = "⏱ ${recipe.timeMinutes} min",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "🍴 ${recipe.level}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ─── Tarjeta de lista (pequeña) ───────────────────────────────────────────────
@Composable
private fun LocalRecipeItem(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF8B5E3C))
            )

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = "${recipe.region}  •  ${recipe.category}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorBodyText.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recipe.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkBrown,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$${"%,.0f".format(recipe.totalCost).replace(",", ".")}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorRed
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = ColorGold
                )
            }
        }
    }
}

// ─── Bottom Navigation Bar ───────────────────────────────────────────────────
@Composable
fun EcoBottomNavBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = ColorDarkBrown,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onHomeClick,
            icon = { Text("🏠", fontSize = 20.sp) },
            label = {
                Text(
                    text = "INICIO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = ColorGold,
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "explore",
            onClick = onExploreClick,
            icon = { Text("🧭", fontSize = 20.sp) },
            label = {
                Text(
                    text = "EXPLORAR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = ColorGold,
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "create",
            onClick = onCreateClick,
            icon = { Text("🍴", fontSize = 20.sp) },
            label = {
                Text(
                    text = "CREAR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = ColorGold,
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onProfileClick,
            icon = { Text("👤", fontSize = 20.sp) },
            label = {
                Text(
                    text = "PERFIL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = ColorGold,
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun HomeScreenPreview() {
    val mockRecipes = listOf(
        Recipe(
            id = "1",
            name = "Bandeja Paisa Tradicional",
            timeMinutes = 45,
            level = "Fácil",
            totalCost = 12500.0,
            region = "ANTIOQUIA",
            category = "TRADICIONAL"
        ),
        Recipe(
            id = "2",
            name = "Ajiaco Santafereño",
            timeMinutes = 60,
            level = "Medio",
            totalCost = 18000.0,
            region = "BOGOTÁ",
            category = "GOURMET"
        ),
        Recipe(
            id = "3",
            name = "Arepa de Chócolo",
            timeMinutes = 20,
            level = "Fácil",
            totalCost = 6500.0,
            region = "ANTIOQUIA",
            category = "SNACK"
        )
    )

    EcoRecetaTheme {
        HomeContent(
            state = HomeUiState(
                featuredRecipe = mockRecipes.first(),
                localRecipes = mockRecipes.drop(1)
            ),
            onSearchQueryChange = {},
            onNavigateToExplore = {},
            onNavigateToCreate = {},
            onNavigateToProfile = {},
            onRecipeClick = {}
        )
    }
}
