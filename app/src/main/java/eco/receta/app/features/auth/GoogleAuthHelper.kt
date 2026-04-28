package eco.receta.app.features.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

// Resultado posible de un intento de login con Google
sealed class GoogleSignInResult {
    data class Success(val user: FirebaseUser) : GoogleSignInResult()
    data class Error(val message: String)      : GoogleSignInResult()
    object Cancelled                           : GoogleSignInResult()
}

class GoogleAuthHelper(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(webClientId: String): GoogleSignInResult {
        return try {

            // 1. Configurar la solicitud de Google
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // muestra todas las cuentas Google
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 2. Lanzar el selector de cuentas de Google
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // 3. Extraer el token de la credencial seleccionada
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential
                    .createFrom(credential.data)
                    .idToken

                // 4. Autenticar con Firebase usando el token de Google
                val firebaseCredential = GoogleAuthProvider
                    .getCredential(googleIdToken, null)

                val authResult = auth
                    .signInWithCredential(firebaseCredential)
                    .await()

                val user = authResult.user
                    ?: return GoogleSignInResult.Error("No se pudo obtener el usuario.")

                GoogleSignInResult.Success(user)

            } else {
                GoogleSignInResult.Error("Tipo de credencial no reconocido.")
            }

        } catch (e: GetCredentialCancellationException) {
            // El usuario cerró el selector conscientemente
            GoogleSignInResult.Cancelled
        } catch (e: GetCredentialException) {
            // Error técnico de Credential Manager (ej: falta configuración SHA-1 o Play Services)
            GoogleSignInResult.Error("Error de credenciales: ${e.message} (${e.javaClass.simpleName})")
        } catch (e: Exception) {
            GoogleSignInResult.Error(
                e.message ?: "Error desconocido al iniciar sesión con Google."
            )
        }
    }
}
