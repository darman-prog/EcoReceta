package eco.receta.app.features.auth


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import eco.receta.app.core.components.AuthTextField
import eco.receta.app.ui.theme.EcoRecetaTheme

// ─── Colores extraídos del diseño ───────────────────────────────────────────
private val ColorPrimary      = Color(0xFFD94F3D) // Rojo botón principal
private val ColorGold         = Color(0xFFC8922A) // Amarillo dorado – links
private val ColorNavy         = Color(0xFF1B3A6B) // Azul oscuro – botones sociales
private val ColorFieldBg      = Color(0xFFF5F0F0) // Fondo de campos
private val ColorLabelText    = Color(0xFF8A8A8A) // Labels de campos
private val ColorBodyText     = Color(0xFF3D3D3D) // Texto secundario
private val ColorCardBg       = Color(0xFFFAFAFA) // Fondo tarjeta

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val state by viewModel.loginState.collectAsState()

    // Navegar cuando el login es exitoso
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.clearLoginSuccess()
            onLoginSuccess()
        }
    }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onLoginEmailChange,
        onPasswordChange = viewModel::onLoginPasswordChange,
        onLoginClick = viewModel::login,
        onNavigateToRegister = onNavigateToRegister
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ════════════════════════════════════════════════════════════════════
        // TODO: COLOCA TU IMAGEN DE FONDO AQUÍ (R.drawable.tu_imagen)
        // ════════════════════════════════════════════════════════════════════
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.45f).background(Color(0xFFF5C400)))
            Box(modifier = Modifier.fillMaxWidth().weight(0.30f).background(Color(0xFF003087)))
            Box(modifier = Modifier.fillMaxWidth().weight(0.25f).background(Color(0xFFCE1126)))
        }

        // ── Tarjeta central ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(28.dp))
                .background(ColorCardBg)
                .padding(horizontal = 28.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Título
            Text(
                text = "¡Bienvenido de vuelta!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa a tu despensa de recetas y descubre el sabor de Colombia.",
                fontSize = 14.sp,
                color = ColorBodyText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Campo: Correo ───────────────────────────────────────────────
            AuthTextField(
                label = "CORREO ELECTRÓNICO",
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "ejemplo@colombia.com",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = ColorLabelText) },
                keyboardType = KeyboardType.Email,
                errorMessage = state.emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo: Contraseña ───────────────────────────────────────────
            AuthTextField(
                label = "CONTRASEÑA",
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "••••••••",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColorLabelText) },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = ColorLabelText
                        )
                    }
                },
                errorMessage = state.passwordError
            )

            // Olvidé contraseña
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { /* TODO: recuperar contraseña */ }) {
                    Text("¿Olvidaste tu contraseña?", color = ColorGold, fontSize = 13.sp)
                }
            }

            // ── Error Firebase ──────────────────────────────────────────────
            state.authError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ── Botón principal ─────────────────────────────────────────────
            Button(
                onClick = onLoginClick,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ingresar →", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Separador ──────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                Text("  O continúa con  ", color = ColorLabelText, fontSize = 12.sp)
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Botones sociales ───────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SocialButton(label = "Google", modifier = Modifier.weight(1f)) {
                    // TODO: Google Sign-In
                }
                SocialButton(label = "iOS", modifier = Modifier.weight(1f)) {
                    // TODO: Apple Sign-In
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Enlace a registro ──────────────────────────────────────────
            Row {
                Text("¿Nuevo en la Despensa? ", color = ColorBodyText, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Crea una cuenta", color = ColorGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ─── Botón social reutilizable ──────────────────────────────────────────────
@Composable
private fun SocialButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = ColorNavy)
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LoginScreenPreview() {
    EcoRecetaTheme {
        LoginContent(
            state = LoginUiState(
                email = "chef@ecoreceta.com",
                password = "password123"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}
