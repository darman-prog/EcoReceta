package eco.receta.app.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import eco.receta.app.data.model.Recipe
import eco.receta.app.data.model.User
import eco.receta.app.data.repository.RecipeRepository
import eco.receta.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val user: User? = null,
    val recetasPrivadas: List<Recipe> = emptyList(),
    val recetasPublicas: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val proximoBadge: String = "",
    val recetasParaSiguienteRango: Int = 0,
    val progresoRango: Float = 0f
)

class ProfileViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        val currentUser = auth.currentUser ?: run {
            _uiState.value = ProfileUiState(isLoading = false, error = "No hay sesión activa")
            return
        }

        val uid = currentUser.uid

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val privadas = recipeRepository.getRecetasPrivadas()
                val comunidad = recipeRepository.getRecetasComunidad()
                val publicasDelUsuario = comunidad.filter { it.autorID == uid }

                Log.d("ProfileVM", "Privadas: ${privadas.size}, Públicas: ${publicasDelUsuario.size}")

                val recetasTotales = privadas.size + publicasDelUsuario.size

                val userResult = userRepository.getUserData(uid)

                userResult.onSuccess { user ->
                    if (user.recetasCreadas != recetasTotales) {
                        Log.d("ProfileVM", "Sincronizando: ${user.recetasCreadas} -> $recetasTotales")
                        userRepository.actualizarContadorRecetas(uid, recetasTotales)
                        userRepository.actualizarBadge(uid, recetasTotales)
                    }
                }

                val userActualizado = userRepository.getUserData(uid).getOrNull()

                userActualizado?.let { user ->
                    val (proximoBadge, faltantes) = UserRepository.getProximoBadge(user.recetasCreadas)
                    val progreso = calcularProgreso(user.recetasCreadas)

                    _uiState.update {
                        it.copy(
                            user = user,
                            recetasPrivadas = privadas,
                            recetasPublicas = publicasDelUsuario,
                            proximoBadge = proximoBadge,
                            recetasParaSiguienteRango = faltantes,
                            progresoRango = progreso,
                            isLoading = false
                        )
                    }
                } ?: run {
                    crearUsuarioSiNoExiste(currentUser.displayName, currentUser.email, uid)
                }

            } catch (e: Exception) {
                Log.e("ProfileVM", "Error: ${e.message}")
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    private fun calcularProgreso(recetas: Int): Float = when {
        recetas <= 5 -> recetas / 5f
        recetas in 6..15 -> (recetas - 5) / 10f
        else -> 1f
    }

    private fun crearUsuarioSiNoExiste(nombre: String?, email: String?, uid: String) {
        viewModelScope.launch {
            val nombreFinal = auth.currentUser?.displayName
                ?.takeIf { it.isNotBlank() }
                ?: nombre
                ?: "Usuario EcoReceta"

            userRepository.crearUsuarioEnFirestore(
                uid = uid,
                nombre = nombreFinal,
                email = email ?: ""
            )
            loadProfileData()
        }
    }

    fun recalcularBadge() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val privadas = recipeRepository.getRecetasPrivadas()
                val comunidad = recipeRepository.getRecetasComunidad()
                val publicasDelUsuario = comunidad.filter { it.autorID == uid }
                val recetasTotales = privadas.size + publicasDelUsuario.size

                userRepository.actualizarBadge(uid, recetasTotales)
                    .onSuccess { loadProfileData() }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun guardarRecetaEnPerfil(recetaId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(uid)
                    .update("recetasGuardadas", FieldValue.arrayUnion(recetaId))
                    .await()

                val docPublico = db.collection("recetas_publicas")
                    .document(recetaId)
                    .get()
                    .await()

                if (docPublico.exists()) {
                    val datos = docPublico.data ?: return@launch
                    db.collection("usuarios")
                        .document(uid)
                        .collection("recetas_privadas")
                        .document(recetaId)
                        .set(datos)
                        .await()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}