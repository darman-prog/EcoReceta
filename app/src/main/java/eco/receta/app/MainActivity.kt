package eco.receta.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import eco.receta.app.core.navigation.AppNavHost
import eco.receta.app.ui.theme.EcoRecetaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite que la app ocupe toda la pantalla (detrás de la barra de estado)
        enableEdgeToEdge()

        setContent {
            EcoRecetaTheme {

                val navController = rememberNavController()

                AppNavHost(navController = navController)
            }
        }
    }
}