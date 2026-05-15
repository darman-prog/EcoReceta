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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import eco.receta.app.core.components.EcoBottomNavBar
import eco.receta.app.core.navigation.Routes
import eco.receta.app.data.model.Visibilidad

// ─── Paleta EcoReceta ────────────────────────────────────────────────────────
private val Crema        = Color(0xFFF6EFE9)
private val MarronOscuro = Color(0xFF2C1A0E)
private val Dorado       = Color(0xFFC8922A)
private val Rojo         = Color(0xFFD94F3D)
private val TarjetaBg    = Color(0xFFFFF8F2)
private val CampoFondo   = Color(0xFFEDE8DF)
val GrisTexto    = Color(0xFF8D8D8D)

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

    Scaffold(
        containerColor = Crema,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // ── TopBar con logo EcoReceta ────────────────────────────────
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
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
                        else CampoFondo.copy(alpha = 0f),
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
                    // Badge editar
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
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
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
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

            // ── 2. NOMBRE DEL PLATO ──────────────────────────────────────
            SectionLabel(icon = "🍽", text = "Nombre del plato")
            EcoTextField(
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                placeholder = "Ej: Ajiaco Santafereño",
                leadingEmoji = "✍️"
            )

            // ── 3. HISTORIA / DESCRIPCIÓN ────────────────────────────────
            SectionLabel(icon = "📖", text = "Historia o descripción")
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                placeholder = { Text("Cuéntanos el secreto de esta receta...", color = GrisTexto) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(16.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CampoFondo,
                    focusedContainerColor   = CampoFondo,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Dorado
                )
            )

            // ── 4. METADATOS ─────────────────────────────────────────────
            SectionLabel(icon = "⏱", text = "Detalles")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EcoTextField(
                    value = uiState.tiempoMinutos,
                    onValueChange = viewModel::onTiempoChange,
                    placeholder = "Tiempo (min)",
                    leadingEmoji = "⏱",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                // Selector de nivel
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.nivel,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Text("📊", fontSize = 16.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CampoFondo,
                            focusedContainerColor   = CampoFondo,
                            unfocusedBorderColor    = Color.Transparent,
                            focusedBorderColor      = Dorado
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Fácil", "Medio", "Difícil").forEach { nivel ->
                            DropdownMenuItem(
                                text = { Text(nivel) },
                                onClick = { viewModel.onNivelChange(nivel); expanded = false }
                            )
                        }
                    }
                }
            }



            // ── Selector de porciones ────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CampoFondo)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📜", fontSize = 18.sp)
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

                // Controles - / número / +
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Botón decrementar
                    IconButton(
                        onClick = viewModel::decrementarPorciones,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.porciones > 1) MarronOscuro
                                else MarronOscuro.copy(alpha = 0.3f)
                            )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Reducir porciones",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Número actual
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.porciones.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MarronOscuro
                        )
                    }

                    // Botón incrementar
                    IconButton(
                        onClick = viewModel::incrementarPorciones,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MarronOscuro)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar porciones",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Selector de categoría ────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))

            var expandedCategoria by remember { mutableStateOf(false) }

            val categorias = listOf(
                "ACOMPAÑAMIENTOS",
                "SOPAS",
                "BEBIDAS FRÍAS",
                "ALMUERZOS"
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
                    leadingIcon = { Text("🍽", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
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
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = categoria,
                                    fontWeight = FontWeight.Medium,
                                    color = MarronOscuro
                                )
                            },
                            onClick = {
                                viewModel.onCategoriaChange(categoria)
                                expandedCategoria = false
                            },
                            leadingIcon = {
                                Text(
                                    text = when (categoria) {
                                        "ACOMPAÑAMIENTOS" -> "🍚"
                                        "SOPAS"           -> "🍲"
                                        "BEBIDAS FRÍAS"   -> "🥤"
                                        "ALMUERZOS"       -> "🍽"
                                        else              -> "🍴"
                                    },
                                    fontSize = 16.sp
                                )
                            }
                        )
                    }
                }
            }


            // ── 5. INGREDIENTES ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(icon = "🧺", text = "Ingredientes")
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

            if (uiState.ingredientesSeleccionados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CampoFondo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Aún no has añadido ingredientes",
                        color = GrisTexto,
                        fontSize = 14.sp
                    )
                }
            } else {
                val costoTotal = uiState.ingredientesSeleccionados.sumOf { it.precio }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.ingredientesSeleccionados.forEach { ing ->
                        IngredienteCard(
                            ingrediente = ing,
                            onRemove = { viewModel.removeIngrediente(ing.productoId) }
                        )
                    }
                    // Costo total
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛒", fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "COSTO TOTAL",
                                        color = Dorado,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "COP $${"%,.0f".format(costoTotal)}",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Text(
                                "${uiState.ingredientesSeleccionados.size} items",
                                color = Color.White.copy(0.6f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── 6. VISIBILIDAD ───────────────────────────────────────────
            SectionLabel(icon = "👁", text = "¿Quién puede ver esta receta?")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VisibilidadOpcion(
                    emoji = "🌍",
                    titulo = "Pública",
                    descripcion = "Todos pueden verla",
                    isSelected = uiState.visibilidad == Visibilidad.PUBLICA,
                    onClick = { viewModel.onVisibilidadChange(Visibilidad.PUBLICA) },
                    modifier = Modifier.weight(1f)
                )
                VisibilidadOpcion(
                    emoji = "🔒",
                    titulo = "Privada",
                    descripcion = "Solo tú la ves",
                    isSelected = uiState.visibilidad == Visibilidad.PRIVADA,
                    onClick = { viewModel.onVisibilidadChange(Visibilidad.PRIVADA) },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── 7. GUARDAR ───────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { viewModel.guardarReceta() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Rojo)
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
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Componentes auxiliares ───────────────────────────────────────────────────

@Composable
private fun SectionLabel(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Text(
            text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MarronOscuro
        )
    }
}

@Composable
private fun EcoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingEmoji: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = GrisTexto, fontSize = 14.sp) },
        leadingIcon = { Text(leadingEmoji, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = CampoFondo,
            focusedContainerColor   = CampoFondo,
            unfocusedBorderColor    = Color.Transparent,
            focusedBorderColor      = Dorado
        )
    )
}

@Composable
private fun VisibilidadOpcion(
    emoji: String,
    titulo: String,
    descripcion: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MarronOscuro else CampoFondo,
        border = if (isSelected) null
        else androidx.compose.foundation.BorderStroke(
            1.5.dp, CampoFondo
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(
                titulo,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MarronOscuro,
                fontSize = 14.sp
            )
            Text(
                descripcion,
                color = if (isSelected) Color.White.copy(0.7f) else GrisTexto,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun IngredienteCard(
    ingrediente: IngredienteSeleccionado,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CampoFondo),
                    contentAlignment = Alignment.Center
                ) { Text("🍴", fontSize = 18.sp) }
                Column {
                    Text(
                        ingrediente.nombre,
                        fontWeight = FontWeight.SemiBold,
                        color = MarronOscuro,
                        fontSize = 14.sp
                    )
                    Text(
                        "${ingrediente.cantidad} ${ingrediente.unidad}",
                        color = GrisTexto,
                        fontSize = 12.sp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "COP $${"%,.0f".format(ingrediente.precio)}",
                    fontWeight = FontWeight.Bold,
                    color = Dorado,
                    fontSize = 13.sp
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = Rojo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}