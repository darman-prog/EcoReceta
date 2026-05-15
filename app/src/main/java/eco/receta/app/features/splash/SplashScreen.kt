package eco.receta.app.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlinx.coroutines.delay

// ─── Paleta EcoReceta ────────────────────────────────────────────────────────
private val MarronOscuro = Color(0xFF2C1A0E)
private val Dorado       = Color(0xFFC8922A)

private val Crema        = Color(0xFFF6EFE9)

private val DoradoClaro  = Color(0xFFE8B84B)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // ── Estados de animación ─────────────────────────────────────────────
    var animationStarted by remember { mutableStateOf(false) }

    // 1. Logo — scale de 0.3 a 1.0
    val logoScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    // 2. Logo — alpha de 0 a 1
    val logoAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing         = FastOutSlowInEasing
        ),
        label = "logoAlpha"
    )

    // 3. Subtítulo — aparece después del logo
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis    = 400,   // aparece 400ms después del logo
            easing         = FastOutSlowInEasing
        ),
        label = "subtitleAlpha"
    )

    // 4. Subtítulo — sube desde abajo
    val subtitleOffset by animateFloatAsState(
        targetValue = if (animationStarted) 0f else 20f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis    = 400,
            easing         = FastOutSlowInEasing
        ),
        label = "subtitleOffset"
    )

    // 5. Puntos decorativos — alpha
    val dotsAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis    = 700,
            easing         = FastOutSlowInEasing
        ),
        label = "dotsAlpha"
    )

    // ── Lógica de tiempo ─────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        animationStarted = true       // arranca animación
        delay(2800)                   // espera que termine
        onSplashFinished()            // navega
    }

    // ── UI ───────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MarronOscuro,
                        Color(0xFF3D2510),
                        Color(0xFF1A0E06)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Círculo decorativo de fondo ──────────────────────────────────
        Box(
            modifier = Modifier
                .size(280.dp)
                .alpha(0.07f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Dorado, Color.Transparent)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        // ── Contenido central ────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo EcoReceta
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        color      = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )) { append("Eco") }
                    withStyle(SpanStyle(
                        color      = DoradoClaro,
                        fontWeight = FontWeight.ExtraBold
                    )) { append("Receta") }
                },
                fontSize = 48.sp,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )

            Spacer(Modifier.height(12.dp))

            // Subtítulo
            Text(
                text = "El sabor de nuestra tierra en tu cocina.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .alpha(subtitleAlpha)
                    .offset(y = subtitleOffset.dp)
            )

            Spacer(Modifier.height(48.dp))

            // Puntos animados de carga
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(dotsAlpha)
            ) {
                repeat(3) { index ->
                    AnimatedDot(delayMillis = index * 200)
                }
            }
        }

        // ── Colombia flag strip en la parte inferior ─────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(subtitleAlpha)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.weight(1f).height(3.dp).background(Color(0xFFF5C400)))
                Box(Modifier.weight(1f).height(3.dp).background(Color(0xFF003087)))
                Box(Modifier.weight(1f).height(3.dp).background(Color(0xFFCE1126)))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Punto animado pulsante ───────────────────────────────────────────────────
@Composable
private fun AnimatedDot(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(600, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue  = 1.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(600, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(scale)
            .alpha(alpha)
            .background(
                color = Dorado,
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}