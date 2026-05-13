package eco.receta.app.features.recipes.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import eco.receta.app.core.components.EcoBottomNavBar
import eco.receta.app.core.navigation.Routes
import eco.receta.app.data.model.Visibilidad
import eco.receta.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    viewModel: CreateRecipeViewModel = viewModel(),
    navController: NavController,  // ← Solo recibir, SIN valor por defecto
    onNavigateToRoute: (String) -> Unit,
    onNavigateToAddIngredients: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()

    // Launcher para seleccionar imagen de galería
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImagenSelected(it) }
    }

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    //detail recipe
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        val ingrediente = savedStateHandle?.get<IngredienteSeleccionado>("ingrediente_seleccionado")
        ingrediente?.let {
            viewModel.addIngrediente(it)
            savedStateHandle.remove<IngredienteSeleccionado>("ingrediente_seleccionado")
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("¡Receta guardada con éxito!")
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Crear Receta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VerdeBosque
                        )
                        Text(
                            "Comparte el sabor de tu tierra con la comunidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisPiedra,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CremaClaro
                )
            )
        },
        bottomBar = {
                EcoBottomNavBar(
                    currentRoute = Routes.CREATE,

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
                        // Ya estamos en Crear, no hacer nada
                    },

                    onProfileClick = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(Routes.CREATE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════════════════
            // 1. SUBIR FOTO DEL PLATO
            // ═══════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F0E8))
                    .border(2.dp, Color(0xFFD4C8B0), RoundedCornerShape(16.dp))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imagenUri != null) {
                    AsyncImage(
                        model = uiState.imagenUri,
                        contentDescription = "Foto del plato",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            tint = VerdeBosque,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Subir foto del plato",
                            color = VerdeBosque,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "INSPIRACIÓN WAYUU",
                            color = GrisPiedra,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // 2. NOMBRE DEL PLATO
            // ═══════════════════════════════════════════════════════
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre del Plato") },
                placeholder = { Text("Ej. Ajiaco Santafereño") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeBosque,
                    focusedLabelColor = VerdeBosque
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════════════════
            // 3. DESCRIPCIÓN / HISTORIA
            // ═══════════════════════════════════════════════════════
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Historia o Descripción") },
                placeholder = { Text("Cuéntanos el secreto de esta receta...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeBosque,
                    focusedLabelColor = VerdeBosque
                ),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════════════════
            // 4. METADATOS (Tiempo, Porciones, Nivel)
            // ═══════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.tiempoMinutos,
                    onValueChange = viewModel::onTiempoChange,
                    label = { Text("Tiempo") },
                    placeholder = { Text("45 min") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selector de nivel
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.nivel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nivel") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Fácil", "Medio", "Difícil").forEach { nivel ->
                        DropdownMenuItem(
                            text = { Text(nivel) },
                            onClick = {
                                viewModel.onNivelChange(nivel)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // 5. INGREDIENTES
            // ═══════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ingredientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VerdeBosque
                )
                Button(
                    onClick = onNavigateToAddIngredients,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de ingredientes seleccionados
            if (uiState.ingredientesSeleccionados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F0E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Aún no has añadido ingredientes",
                        color = GrisPiedra,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                val costoTotal = uiState.ingredientesSeleccionados.sumOf { it.precio }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.ingredientesSeleccionados.forEach { ing ->
                        IngredienteSeleccionadoCard(
                            ingrediente = ing,
                            onRemove = { viewModel.removeIngrediente(ing.productoId) }
                        )
                    }

                    // Costo total de ingredientes
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Costo Total Ingredientes",
                            fontWeight = FontWeight.Bold,
                            color = GrisPiedra
                        )
                        Text(
                            "COP $${"%,.0f".format(costoTotal)}",
                            fontWeight = FontWeight.Bold,
                            color = VerdeBosque
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // 6. TOGGLE PÚBLICO / PRIVADO
            // ═══════════════════════════════════════════════════════
            Text(
                "¿Hacer pública o privada la receta?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = VerdeBosque,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE8E0D5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VisibilidadToggleButton(
                    text = "PÚBLICA",
                    isSelected = uiState.visibilidad == Visibilidad.PUBLICA,
                    onClick = { viewModel.onVisibilidadChange(Visibilidad.PUBLICA) },
                    modifier = Modifier.weight(1f)
                )
                VisibilidadToggleButton(
                    text = "PRIVADA",
                    isSelected = uiState.visibilidad == Visibilidad.PRIVADA,
                    onClick = { viewModel.onVisibilidadChange(Visibilidad.PRIVADA) },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = when (uiState.visibilidad) {
                    Visibilidad.PUBLICA -> "Todos en la comunidad podrán ver esta receta"
                    Visibilidad.PRIVADA -> "Solo tú podrás ver esta receta en tu perfil"
                },
                style = MaterialTheme.typography.bodySmall,
                color = GrisPiedra,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ═══════════════════════════════════════════════════════
            // 7. BOTÓN GUARDAR RECETA
            // ═══════════════════════════════════════════════════════
            Button(
                    onClick = { viewModel.guardarReceta() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Guardar Receta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// COMPONENTES AUXILIARES
// ═════════════════════════════════════════════════════════════════

@Composable
private fun VisibilidadToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) VerdeBosque else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else GrisPiedra,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun IngredienteSeleccionadoCard(
    ingrediente: IngredienteSeleccionado,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0E8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ingrediente.nombre,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3D3D3D)
                )
                Text(
                    "${ingrediente.cantidad} ${ingrediente.unidad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisPiedra
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "COP $${"%,.0f".format(ingrediente.precio)}",
                    fontWeight = FontWeight.Bold,
                    color = VerdeBosque
                )
                TextButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Text("Eliminar", fontSize = 12.sp)
                }
            }
        }
    }
}