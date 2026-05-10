package eco.receta.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import eco.receta.app.core.navigation.AppNavHost
import eco.receta.app.ui.theme.EcoRecetaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()

        setContent {
            EcoRecetaTheme {
                val navController = rememberNavController()

                AppNavHost(
                    navController  = navController,
                    startRoute     = if (auth.currentUser != null) "home" else "login"
                )
            }
        }
    }
}