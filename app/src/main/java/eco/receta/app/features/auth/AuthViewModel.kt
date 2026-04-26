package eco.receta.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ---------- UI State ----------

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val authError: String? = null,
    val isSuccess: Boolean = false
)

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val authError: String? = null,
    val isSuccess: Boolean = false
)

// ---------- ViewModel ----------

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    // --- Login field updates ---

    fun onLoginEmailChange(value: String) {
        _loginState.update { it.copy(email = value, emailError = null, authError = null) }
    }

    fun onLoginPasswordChange(value: String) {
        _loginState.update { it.copy(password = value, passwordError = null, authError = null) }
    }

    // --- Register field updates ---

    fun onRegisterFullNameChange(value: String) {
        _registerState.update { it.copy(fullName = value, fullNameError = null) }
    }

    fun onRegisterEmailChange(value: String) {
        _registerState.update { it.copy(email = value, emailError = null, authError = null) }
    }

    fun onRegisterPasswordChange(value: String) {
        _registerState.update { it.copy(password = value, passwordError = null) }
    }

    fun onRegisterConfirmPasswordChange(value: String) {
        _registerState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    // --- Login Action ---

    fun login() {
        val state = _loginState.value

        val emailResult = Validations.validateEmail(state.email)
        val passwordResult = Validations.validatePassword(state.password)

        if (!emailResult.isValid || !passwordResult.isValid) {
            _loginState.update {
                it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, authError = null) }
            try {
                auth.signInWithEmailAndPassword(state.email.trim(), state.password).await()
                _loginState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _loginState.update {
                    it.copy(isLoading = false, authError = "Correo o contraseña incorrectos.")
                }
            } catch (e: Exception) {
                _loginState.update {
                    it.copy(isLoading = false, authError = "Ocurrió un error. Intenta de nuevo.")
                }
            }
        }
    }

    // --- Register Action ---

    fun register() {
        val state = _registerState.value

        val nameResult = Validations.validateFullName(state.fullName)
        val emailResult = Validations.validateEmail(state.email)
        val passwordResult = Validations.validatePassword(state.password)
        val matchResult = Validations.validatePasswordsMatch(state.password, state.confirmPassword)

        if (!nameResult.isValid || !emailResult.isValid ||
            !passwordResult.isValid || !matchResult.isValid
        ) {
            _registerState.update {
                it.copy(
                    fullNameError = nameResult.errorMessage,
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage,
                    confirmPasswordError = matchResult.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, authError = null) }
            try {
                auth.createUserWithEmailAndPassword(state.email.trim(), state.password).await()
                _registerState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: FirebaseAuthUserCollisionException) {
                _registerState.update {
                    it.copy(isLoading = false, authError = "Este correo ya está registrado.")
                }
            } catch (e: FirebaseAuthWeakPasswordException) {
                _registerState.update {
                    it.copy(isLoading = false, authError = "La contraseña es demasiado débil.")
                }
            } catch (e: Exception) {
                _registerState.update {
                    it.copy(isLoading = false, authError = "Ocurrió un error. Intenta de nuevo.")
                }
            }
        }
    }

    fun clearLoginSuccess() = _loginState.update { it.copy(isSuccess = false) }
    fun clearRegisterSuccess() = _registerState.update { it.copy(isSuccess = false) }
}