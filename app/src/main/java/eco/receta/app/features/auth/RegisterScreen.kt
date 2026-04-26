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

// ─── Colores (misma paleta que LoginScreen) ──────────────────────────────────
private val ColorPrimary   = Color(0xFFD94F3D)
private val ColorGold      = Color(0xFFC8922A)
private val ColorBodyText  = Color(0xFF3D3D3D)
private val ColorCardBg    = Color(0xFFFAFAFA)
private val ColorLabelText = Color(0xFF8A8A8A)

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

        // ════════════════════════════════════════════════════════════════════
        // TODO: COLOCA TU IMAGEN DE FONDO AQUÍ (R.drawable.tu_imagen)
        // ════════════════════════════════════════════════════════════════════
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.45f).background(Color(0xFFF5C400)))
            Box(modifier = Modifier.fillMaxWidth().weight(0.30f).background(Color(0xFF003087)))
            Box(modifier = Modifier.fillMaxWidth().weight(0.25f).background(Color(0xFFCE1126)))
        }

        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Encabezado sobre la tarjeta ─────────────────────────────────
            Text(
                text = "EcoReceta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "El sabor de nuestra tierra en tu cocina.",
                fontSize = 13.sp,
                color = ColorBodyText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Tarjeta ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(ColorCardBg)
                    .padding(horizontal = 28.dp, vertical = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Título con estilo
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = ColorGold, fontWeight = FontWeight.Bold)) {
                            append("Únete")
                        }
                        append(" a EcoReceta")
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Crea tu cuenta para guardar tus recetas favoritas.",
                    fontSize = 14.sp,
                    color = ColorBodyText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Campo: Nombre ───────────────────────────────────────────
                AuthTextField(
                    label = "NOMBRE COMPLETO",
                    value = state.fullName,
                    onValueChange = onFullNameChange,
                    placeholder = "Ej: Gabriel García",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = ColorLabelText) },
                    errorMessage = state.fullNameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Campo: Correo ───────────────────────────────────────────
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

                // ── Campo: Contraseña ───────────────────────────────────────
                AuthTextField(
                    label = "CONTRASEÑA",
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = "••••••••",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColorLabelText) },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    errorMessage = state.passwordError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Campo: Confirmar contraseña ─────────────────────────────
                AuthTextField(
                    label = "CONFIRMAR CONTRASEÑA",
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = "••••••••",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColorLabelText) },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    errorMessage = state.confirmPasswordError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Error Firebase ──────────────────────────────────────────
                state.authError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Botón principal ─────────────────────────────────────────
                Button(
                    onClick = onRegisterClick,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 8.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Unirme a la Despensa →", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Enlace a login ──────────────────────────────────────────
                Row {
                    Text("¿Ya tienes una cuenta? ", color = ColorBodyText, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
                    TextButton(
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Inicia sesión aquí", color = ColorGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
