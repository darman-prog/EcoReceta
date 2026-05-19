package eco.receta.app.features.recipes.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eco.receta.app.core.components.EcoBottomNavBar
import eco.receta.app.core.components.SectionLabel
import eco.receta.app.core.components.VisibilidadOpcionAnimada
import eco.receta.app.core.navigation.Routes
import eco.receta.app.data.model.Visibilidad

// ─── Paleta EcoReceta ────────────────────────────────────────────────────────
private val Crema        = Color(0xFFF6EFE9)
val MarronOscuro = Color(0xFF2C1A0E)
val Dorado       = Color(0xFFC8922A)
val Rojo         = Color(0xFFD94F3D)
val TarjetaBg    = Color(0xFFFFF8F2)
val CampoFondo   = Color(0xFFEDE8DF)
val GrisTexto            = Color(0xFF8D8D8D)
val VerdeValido  = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    viewModel: CreateRecipeViewModel = viewModel(),
    navController: NavController,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToAddIngredients: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Animación de entrada — ocurre UNA sola vez ────────────────────────
    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { screenVisible = true }

    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "screenAlpha"
    )
    val screenOffset by animateFloatAsState(
        targetValue = if (screenVisible) 0f else 24f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "screenOffset"
    )

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onImagenSelected(it) } }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("¡Receta guardada con éxito! 🎉")
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

    // ── Validaciones visuales en tiempo real ──────────────────────────────
    val nombreValido = uiState.nombre.isNotBlank()
    val tiempoValido = uiState.tiempoMinutos.toIntOrNull()?.let { it > 0 } == true
    val tieneIngredientes = uiState.ingredientesSeleccionados.isNotEmpty()

    Scaffold(
        containerColor = Crema,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
                    Text(
                        text = "Crear Receta",
                        fontSize = 16.sp,
                        color = GrisTexto,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        },
        bottomBar = {
            EcoBottomNavBar(
                currentRoute   = Routes.CREATE,
                onHomeClick    = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.CREATE) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                },
                onExploreClick = {
                    navController.navigate(Routes.EXPLORE) {
                        popUpTo(Routes.CREATE) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                },
                onCreateClick  = {},
                onProfileClick = {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.CREATE) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                // ── Animación de entrada aplicada una sola vez ───────────
                .alpha(screenAlpha)
                .offset(y = screenOffset.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── 1. FOTO DEL PLATO ────────────────────────────────────────
            SectionLabel(icon = "📸", text = "Foto del plato")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CampoFondo)
                    .border(
                        width = 2.dp,
                        color = if (uiState.imagenUri != null) Dorado
                        else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
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
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                        shape = RoundedCornerShape(50),
                        color = MarronOscuro.copy(alpha = 0.8f)
                    ) {
                        Text(
                            "✏️ Cambiar",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AddPhotoAlternate,
                            null,
                            tint = Dorado,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            "Toca para subir foto",
                            color = MarronOscuro,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            "INSPIRACIÓN WAYUU",
                            color = GrisTexto,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }

            // ── 2. NOMBRE con validación visual ──────────────────────────
            SectionLabel(
                icon = "🍽",
                text = "Nombre del plato",
                isValid = nombreValido,
                showValidation = uiState.nombre.isNotEmpty()
            )
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                placeholder = { Text("Ej: Ajiaco Santafereño", color = GrisTexto, fontSize = 14.sp) },
                leadingIcon = { Text("✍️", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = {
                    // Ícono de validación animado
                    AnimatedVisibility(
                        visible = uiState.nombre.isNotEmpty(),
                        enter = scaleIn() + fadeIn(),
                        exit  = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = if (nombreValido) Icons.Default.CheckCircle
                            else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (nombreValido) VerdeValido else Rojo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CampoFondo,
                    focusedContainerColor   = CampoFondo,
                    unfocusedBorderColor    = when {
                        uiState.nombre.isEmpty() -> Color.Transparent
                        nombreValido             -> VerdeValido.copy(0.5f)
                        else                     -> Rojo.copy(0.5f)
                    },
                    focusedBorderColor = if (nombreValido) Dorado else Rojo
                )
            )

            // ── 3. DESCRIPCIÓN ───────────────────────────────────────────
            SectionLabel(icon = "📖", text = "Historia o descripción")
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                placeholder = {
                    Text("Cuéntanos el secreto de esta receta...", color = GrisTexto)
                },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(16.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CampoFondo,
                    focusedContainerColor   = CampoFondo,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Dorado
                )
            )

            // ── 4. DETALLES — Tiempo + Nivel con más aire ────────────────
            SectionLabel(icon = "⏱", text = "Detalles")

            // Tiempo
            OutlinedTextField(
                value = uiState.tiempoMinutos,
                onValueChange = viewModel::onTiempoChange,
                placeholder = { Text("Tiempo en minutos", color = GrisTexto, fontSize = 14.sp) },
                leadingIcon = { Text("⏱", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = uiState.tiempoMinutos.isNotEmpty(),
                        enter = scaleIn() + fadeIn(),
                        exit  = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = if (tiempoValido) Icons.Default.CheckCircle
                            else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (tiempoValido) VerdeValido else Rojo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CampoFondo,
                    focusedContainerColor   = CampoFondo,
                    unfocusedBorderColor    = when {
                        uiState.tiempoMinutos.isEmpty() -> Color.Transparent
                        tiempoValido                    -> VerdeValido.copy(0.5f)
                        else                            -> Rojo.copy(0.5f)
                    },
                    focusedBorderColor = if (tiempoValido) Dorado else Rojo
                )
            )

            Spacer(Modifier.height(4.dp))

            // Nivel
            var expandedNivel by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedNivel,
                onExpandedChange = { expandedNivel = it }
            ) {
                OutlinedTextField(
                    value = uiState.nivel,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Text("📊", fontSize = 16.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedNivel) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CampoFondo,
                        focusedContainerColor   = CampoFondo,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = Dorado
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedNivel,
                    onDismissRequest = { expandedNivel = false }
                ) {
                    listOf("Fácil" to "🟢", "Medio" to "🟡", "Difícil" to "🔴")
                        .forEach { (nivel, emoji) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(emoji, fontSize = 14.sp)
                                        Text(nivel, color = MarronOscuro)
                                    }
                                },
                                onClick = {
                                    viewModel.onNivelChange(nivel)
                                    expandedNivel = false
                                }
                            )
                        }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Porciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CampoFondo)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🍽", fontSize = 20.sp)
                    Column {
                        Text(
                            "Porciones",
                            fontWeight = FontWeight.Bold,
                            color = MarronOscuro,
                            fontSize = 14.sp
                        )
                        Text(
                            "¿Para cuántas personas?",
                            color = GrisTexto,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Botón −
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.porciones > 1) MarronOscuro
                                else MarronOscuro.copy(alpha = 0.25f)
                            )
                            .clickable(enabled = uiState.porciones > 1) {
                                viewModel.decrementarPorciones()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Número animado
                    Box(
                        modifier = Modifier.width(48.dp).height(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animación de cambio de número
                        key(uiState.porciones) {
                            val numAlpha by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = tween(200),
                                label = "numAlpha"
                            )
                            Text(
                                text = uiState.porciones.toString(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MarronOscuro,
                                modifier = Modifier.alpha(numAlpha)
                            )
                        }
                    }

                    // Botón +
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MarronOscuro)
                            .clickable { viewModel.incrementarPorciones() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Categoría
            var expandedCategoria by remember { mutableStateOf(false) }
            val categorias = listOf(
                "ACOMPAÑAMIENTOS" to "🍚",
                "SOPAS"           to "🍲",
                "BEBIDAS FRÍAS"   to "🥤",
                "ALMUERZOS"       to "🍽"
            )
            ExposedDropdownMenuBox(
                expanded = expandedCategoria,
                onExpandedChange = { expandedCategoria = it }
            ) {
                OutlinedTextField(
                    value = uiState.categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    leadingIcon = {
                        Text(
                            categorias.find { it.first == uiState.categoria }?.second ?: "🍴",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategoria) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CampoFondo,
                        focusedContainerColor   = CampoFondo,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = Dorado
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedCategoria,
                    onDismissRequest = { expandedCategoria = false }
                ) {
                    categorias.forEach { (nombre, emoji) ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 16.sp)
                                    Text(
                                        nombre,
                                        fontWeight = FontWeight.Medium,
                                        color = MarronOscuro
                                    )
                                }
                            },
                            onClick = {
                                viewModel.onCategoriaChange(nombre)
                                expandedCategoria = false
                            }
                        )
                    }
                }
            }

            // ── 5. INGREDIENTES con animación al añadir/eliminar ─────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(
                    icon = "🧺",
                    text = "Ingredientes",
                    isValid = tieneIngredientes,
                    showValidation = true
                )
                Button(
                    onClick = onNavigateToAddIngredients,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MarronOscuro),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(
                visible = uiState.ingredientesSeleccionados.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit  = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CampoFondo)
                        .border(2.dp, Rojo.copy(0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = Rojo.copy(0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Añade al menos un ingrediente",
                            color = GrisTexto,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Lista de ingredientes con animación entrada/salida
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.ingredientesSeleccionados.forEach { ing ->
                    key(ing.productoId) {
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { itemVisible = true }

                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = fadeIn(tween(300)) + slideInVertically(
                                initialOffsetY = { it / 2 }
                            )
                        ) {
                            IngredienteCard(
                                ingrediente = ing,
                                onRemove = { viewModel.removeIngrediente(ing.productoId) }
                            )
                        }
                    }
                }

                // Costo total — solo si hay ingredientes
                AnimatedVisibility(
                    visible = uiState.ingredientesSeleccionados.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit  = fadeOut() + shrinkVertically()
                ) {
                    val costoTotal = uiState.ingredientesSeleccionados.sumOf { it.precio }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MarronOscuro
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🛒", fontSize = 18.sp)
                                Column {
                                    Text(
                                        "COSTO TOTAL",
                                        color = Dorado,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    // Animación del número del costo
                                    val costoAnimado by animateFloatAsState(
                                        targetValue = costoTotal.toFloat(),
                                        animationSpec = tween(500),
                                        label = "costo"
                                    )
                                    Text(
                                        "COP $${"%,.0f".format(costoAnimado).replace(",", ".")}",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(0.15f)
                            ) {
                                Text(
                                    "${uiState.ingredientesSeleccionados.size} items",
                                    color = Color.White.copy(0.8f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── 6. VISIBILIDAD con animación de color ────────────────────
            SectionLabel(icon = "👁", text = "¿Quién puede ver esta receta?")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VisibilidadOpcionAnimada(
                    emoji = "🌍",
                    titulo = "Pública",
                    descripcion = "Todos pueden verla",
                    isSelected = uiState.visibilidad == Visibilidad.PUBLICA,
                    onClick = { viewModel.onVisibilidadChange(Visibilidad.PUBLICA) },
                    modifier = Modifier.weight(1f)
                )
                VisibilidadOpcionAnimada(
                    emoji = "🔒",
                    titulo = "Privada",
                    descripcion = "Solo tú la ves",
                    isSelected = uiState.visibilidad == Visibilidad.PRIVADA,
                    onClick = { viewModel.onVisibilidadChange(Visibilidad.PRIVADA) },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── 7. BOTÓN GUARDAR con feedback ────────────────────────────
            val puedeGuardar = nombreValido && tiempoValido && !uiState.isLoading
            var botonPresionado by remember { mutableStateOf(false) }

            val botonScale by animateFloatAsState(
                targetValue = if (botonPresionado) 0.96f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                ),
                label = "botonScale",
                finishedListener = { botonPresionado = false }
            )

            val botonColor by animateColorAsState(
                targetValue = when {
                    !puedeGuardar  -> Rojo.copy(alpha = 0.4f)
                    botonPresionado -> MarronOscuro
                    else            -> Rojo
                },
                animationSpec = tween(200),
                label = "botonColor"
            )

            Button(
                onClick = {
                    botonPresionado = true
                    viewModel.guardarReceta()
                },
                enabled = puedeGuardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .scale(botonScale),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = botonColor,
                    disabledContainerColor = Rojo.copy(alpha = 0.4f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Guardar Receta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Hint si hay campos sin llenar
            AnimatedVisibility(
                visible = !puedeGuardar && !uiState.isLoading,
                enter = fadeIn(),
                exit  = fadeOut()
            ) {
                Text(
                    text = when {
                        !nombreValido && !tiempoValido ->
                            "⚠️ Falta el nombre y el tiempo de preparación"
                        !nombreValido -> "⚠️ El nombre del plato es obligatorio"
                        !tiempoValido -> "⚠️ Ingresa el tiempo de preparación"
                        else -> ""
                    },
                    color = Rojo.copy(0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Componentes ──────────────────────────────────────────────────────────────
@Composable
private fun IngredienteCard(
    ingrediente: IngredienteSeleccionado,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícono del ingrediente
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CampoFondo, CampoFondo.copy(0.5f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🥬", fontSize = 20.sp)
                }
                Column {
                    Text(
                        ingrediente.nombre,
                        fontWeight = FontWeight.Bold,
                        color = MarronOscuro,
                        fontSize = 14.sp
                    )
                    Text(
                        "${ingrediente.cantidad} · ${ingrediente.unidad}",
                        color = GrisTexto,
                        fontSize = 12.sp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Dorado.copy(0.15f)
                ) {
                    Text(
                        "COP $${"%,.0f".format(ingrediente.precio)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Dorado,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = Rojo.copy(0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}