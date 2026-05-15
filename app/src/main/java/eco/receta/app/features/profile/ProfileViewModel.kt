package eco.receta.app.features.profile

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val user: User? = null,                    // Datos reales del usuario
    val recetasPrivadas: List<Recipe> = emptyList(),
    val recetasPublicas: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,

    // ═══════════════════════════════════════════════════════════
    // DERIVADOS DEL SISTEMA DE STATS (calculados en el ViewModel)
    // ═══════════════════════════════════════════════════════════
    val proximoBadge: String = "",
    val recetasParaSiguienteRango: Int = 0,
    val progresoRango: Float = 0f              // 0.0f a 1.0f para progress bar
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
            // 1. Cargar datos del usuario (con stats reales)
            val userResult = userRepository.getUserData(uid)

            userResult.onSuccess { user ->
                // Calcular progreso hacia siguiente badge
                val (proximoBadge, faltantes) = UserRepository.getProximoBadge(user.recetasCreadas)
                val progreso = calcularProgreso(user.recetasCreadas)

                _uiState.value = _uiState.value.copy(
                    user = user,
                    proximoBadge = proximoBadge,
                    recetasParaSiguienteRango = faltantes,
                    progresoRango = progreso
                )
            }.onFailure {
                // Si no existe el documento de usuario, crearlo
                crearUsuarioSiNoExiste(currentUser.displayName, currentUser.email, uid)
            }

            // 2. Combinar recetas públicas y privadas
            combine(
                recipeRepository.getRecetasPrivadas(),
                recipeRepository.getRecetasComunidad()
            ) { privadas, comunidad ->
                val publicasDelUsuario = comunidad.filter { it.autorID == uid }

                _uiState.value.copy(
                    recetasPrivadas = privadas,
                    recetasPublicas = publicasDelUsuario,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULAR PROGRESO VISUAL (0.0 a 1.0)
    // ═══════════════════════════════════════════════════════════
    private fun calcularProgreso(recetas: Int): Float = when {
        recetas <= 5 -> recetas / 5f
        recetas in 6..15 -> (recetas - 5) / 10f
        else -> 1f // Ya es Maestro Culinario
    }

    // ═══════════════════════════════════════════════════════════
    // CREAR USUARIO NUEVO SI NO EXISTE EN FIRESTORE
    // ═══════════════════════════════════════════════════════════
    private fun crearUsuarioSiNoExiste(nombre: String?, email: String?, uid: String) {
        viewModelScope.launch {
            val nombreFinal = auth.currentUser?.displayName
                ?.takeIf { it.isNotBlank() }
                ?: nombre
                ?: "Usuario EcoReceta"

            userRepository.crearUsuarioEnFirestore(
                uid = uid,
                nombre = nombreFinal ?: "Usuario EcoReceta",
                email = email ?: ""
            )
            // Recargar datos
            loadProfileData()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FORZAR RECÁLCULO DE BADGE (útil para debug o sincronización)
    // ═══════════════════════════════════════════════════════════
    fun recalcularBadge() {
        val uid = auth.currentUser?.uid ?: return
        val recetasTotales = _uiState.value.recetasPrivadas.size +
                _uiState.value.recetasPublicas.size

        viewModelScope.launch {
            userRepository.actualizarBadge(uid, recetasTotales)
                .onSuccess { loadProfileData() }
        }
    }
    fun guardarRecetaEnPerfil(recetaId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // 1. Marcar como guardada en el array del usuario (comportamiento original)
                db.collection("usuarios").document(uid)
                    .update("recetasGuardadas", FieldValue.arrayUnion(recetaId))
                    .await()

                // 2. Leer la receta pública y copiarla a recetas_privadas del usuario
                //    para que aparezca en "Mi Recetario" en el HomeScreen
                val docPublico = db.collection("recetas_publicas")
                    .document(recetaId)
                    .get()
                    .await()

                if (docPublico.exists()) {
                    val datos = docPublico.data ?: return@launch
                    // Guardamos en recetas_privadas con el mismo ID para evitar duplicados
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