package eco.receta.app.features.auth

import android.util.Patterns

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object Validations {

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "El correo no puede estar vacío.")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(false, "Ingresa un correo electrónico válido.")
        }
        return ValidationResult(true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(false, "La contraseña no puede estar vacía.")
        }
        if (password.length < 6) {
            return ValidationResult(false, "La contraseña debe tener al menos 6 caracteres.")
        }
        return ValidationResult(true)
    }

    fun validateFullName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "El nombre no puede estar vacío.")
        }
        if (name.trim().length < 3) {
            return ValidationResult(false, "Ingresa tu nombre completo.")
        }
        return ValidationResult(true)
    }

    fun validatePasswordsMatch(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult(false, "Confirma tu contraseña.")
        }
        if (password != confirmPassword) {
            return ValidationResult(false, "Las contraseñas no coinciden.")
        }
        return ValidationResult(true)
    }
}