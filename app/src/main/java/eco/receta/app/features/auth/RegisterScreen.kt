package eco.receta.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import eco.receta.app.core.components.AuthTextField
import eco.receta.app.ui.theme.EcoRecetaTheme

// ─── Sistema de Diseño (Consistencia con Login) ──────────────────────────────
private val ColorPrimary   = Color(0xFFD94F3D)
private val ColorGold      = Color(0xFFC8922A)
private val ColorBodyText  = Color(0xFF3D3D3D)
private val ColorCardBg    = Color(0xFFFFFFFF)
private val ColorLabelText = Color(0xFF555555) // Mejora de Accesibilidad

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val state by viewModel.registerState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.clearRegisterSuccess()
            onRegisterSuccess()
        }
    }

    RegisterContent(
        state = state,
        onFullNameChange = viewModel::onRegisterFullNameChange,
        onEmailChange = viewModel::onRegisterEmailChange,
        onPasswordChange = viewModel::onRegisterPasswordChange,
        onConfirmPasswordChange = viewModel::onRegisterConfirmPasswordChange,
        onRegisterClick = viewModel::register,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun RegisterContent(
    state: RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── ColoresBanderaColombia ────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.45f).background(Color(0xFFF5C400)))
            Box(modifier = Modifier.fillMaxWidth().weight(0.30f).background(Color(0xFF003087)))
            Box(modifier = Modifier.fillMaxWidth().weight(0.25f).background(Color(0xFFCE1126)))
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f)))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Tarjeta de Registro (Affordance) ─────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(ColorCardBg)
                    .padding(horizontal = 28.dp, vertical = 36.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = buildAnnotatedString {
                        append("Eco")
                        withStyle(SpanStyle(color = ColorGold, fontWeight = FontWeight.ExtraBold)) {
                            append("Receta")
                        }
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Únete a la comunidad más grande de amantes de la cocina colombiana.",
                    fontSize = 14.sp,
                    color = ColorBodyText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Campos de Entrada de datos (Consistencia) ─────────────────────────
                AuthTextField(
                    label = "NOMBRE COMPLETO",
                    value = state.fullName,
                    onValueChange = onFullNameChange,
                    placeholder = "Ej: Gabriel García",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = ColorLabelText) },
                    errorMessage = state.fullNameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    label = "CORREO ELECTRÓNICO",
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = "tu@correo.com",
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = ColorLabelText) },
                    keyboardType = KeyboardType.Email,
                    errorMessage = state.emailError
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    label = "CONTRASEÑA",
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = "Mínimo 6 caracteres",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColorLabelText) },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    errorMessage = state.passwordError
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    label = "CONFIRMAR CONTRASEÑA",
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = "Repite tu contraseña",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColorLabelText) },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    errorMessage = state.confirmPasswordError
                )

                Spacer(modifier = Modifier.height(12.dp))

                state.authError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRegisterClick,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text("Unirme ahora →", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Ya eres parte? ", color = ColorBodyText, fontSize = 14.sp)
                    TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                        Text("Inicia sesión", color = ColorGold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RegisterScreenPreview() {
    EcoRecetaTheme {
        RegisterContent(
            state = RegisterUiState(
                fullName = "Gabriel García",
                email = "gabriel@macondo.com"
            ),
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}