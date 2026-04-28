package eco.receta.app.features.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import eco.receta.app.R
import eco.receta.app.core.components.AuthTextField
import eco.receta.app.ui.theme.EcoRecetaTheme


// ─── Sistema de Diseño (UI Kit) ───────────────────────────────────────────
private val ColorPrimary      = Color(0xFFD94F3D) // CTA Principal
private val ColorGold         = Color(0xFFC8922A) // Énfasis secundario
private val ColorSocialBg     = Color(0xE2FFFDFD)
private val ColorLabelText    = Color(0xFF555555) // Accesibilidad: Contraste WCAG
private val ColorBodyText     = Color(0xFF3D3D3D)
private val ColorCardBg       = Color(0xFFFFFFFF)

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val state by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)

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
        onGoogleClick = {
            val activity = context.findActivity()
            if (activity != null) {
                viewModel.signInWithGoogle(activity, webClientId)
            } else {
                viewModel.signInWithGoogle(context, webClientId)
            }
        },
        onNavigateToRegister = onNavigateToRegister
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .background(Color(0xCEF5C400)))
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(0.30f)
                .background(Color(0xFF003087)))
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
                .background(Color(0xFFCE1126)))
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.05f)))

        // ── Tarjeta de Login ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(32.dp))
                .background(ColorCardBg)
                .padding(horizontal = 28.dp, vertical = 40.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "¡Bienvenido!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    append("Descubre el sabor de Colombia en cada receta con ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Eco")
                        withStyle(SpanStyle(color = ColorGold)) {
                            append("Receta")
                        }
                    }
                    append(".")
                },
                fontSize = 15.sp,
                color = ColorBodyText,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                label = "CORREO ELECTRÓNICO",
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "ejemplo@eco.com",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = ColorLabelText) },
                keyboardType = KeyboardType.Email,
                errorMessage = state.emailError
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthTextField(
                label = "CONTRASEÑA",
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "••••••••",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColorLabelText) },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = ColorLabelText
                        )
                    }
                },
                errorMessage = state.passwordError
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { }) {
                    Text("¿Olvidaste tu contraseña?", color = ColorGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            state.authError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onLoginClick,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Text("Ingresar ahora", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFF939090))
                Text("  o usa  ", color = ColorLabelText, fontSize = 12.sp)
                Divider(modifier = Modifier.weight(1f), color = Color(0xFF939090))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SocialButton(
                    label = if (state.isGoogleLoading) "Cargando..." else "Google",
                    modifier = Modifier.weight(1f),
                    icon = {
                        if (state.isGoogleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = ColorPrimary
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Google",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    onClick = onGoogleClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta? ", color = ColorBodyText, fontSize = 14.sp)
                TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                    Text("Regístrate aquí", color = ColorGold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SocialButton(
    label: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    // Cambio a estilo con color suave para mantener jerarquía visual
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorSocialBg,
            contentColor = Color.Black
        ),
        border = BorderStroke(2.dp, Color(0xFFE0E0E0)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

/**
 * Función de ayuda para obtener la Activity desde el Context.
 * Indispensable para que CredentialManager pueda inflar su UI.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LoginScreenPreview() {
    EcoRecetaTheme {
        LoginContent(
            state = LoginUiState(email = "chef@ecoreceta.com"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onGoogleClick = {},
            onNavigateToRegister = {}
        )
    }
}
