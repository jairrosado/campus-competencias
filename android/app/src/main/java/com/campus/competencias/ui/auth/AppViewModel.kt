/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/auth/AppViewModel.kt
 * Propósito: Estado de arranque, setup, login, logout, sesión y cambio de servidor.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
package com.campus.competencias.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.remote.ApiException
import com.campus.competencias.data.remote.SessionManager
import com.campus.competencias.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppPhase {
    LOADING,
    SETUP,
    LOGIN,
    READY,
    CONNECTION_ERROR,
}

data class AppUiState(
    val phase: AppPhase = AppPhase.LOADING,
    val user: UserDto? = null,
    val isSubmitting: Boolean = false,
    val formError: String? = null,
    val connectionError: String? = null,
)

class AppViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(AppUiState())
    val state = _state.asStateFlow()

    init {
        observeSessionExpiry()
        bootstrap()
    }

    // HU-01 y AN-01 · Arranque: setup, sesión o login
    fun bootstrap() {
        _state.update {
            it.copy(
                phase = AppPhase.LOADING,
                connectionError = null,
                formError = null,
            )
        }
        viewModelScope.launch {
            try {
                val setup = authRepository.setupStatus()
                if (setup.needsSetup && setup.setupAllowed) {
                    _state.update { it.copy(phase = AppPhase.SETUP, user = null) }
                    return@launch
                }
                val session = authRepository.currentSession()
                if (session == null) {
                    _state.update { it.copy(phase = AppPhase.LOGIN, user = null) }
                } else {
                    _state.update {
                        it.copy(
                            phase = AppPhase.READY,
                            user = session.user,
                            formError = null,
                        )
                    }
                }
            } catch (error: ApiException) {
                _state.update {
                    it.copy(
                        phase = AppPhase.CONNECTION_ERROR,
                        connectionError = error.message,
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        phase = AppPhase.CONNECTION_ERROR,
                        connectionError = error.message ?: "No pudimos iniciar la aplicación.",
                    )
                }
            }
        }
    }

    // HU-01 · Inicio de sesión
    fun login(correo: String, password: String) {
        if (correo.isBlank() || password.isBlank()) {
            _state.update { it.copy(formError = "Ingresa tu correo y tu contraseña.") }
            return
        }
        submit {
            val session = authRepository.login(correo, password)
            _state.update {
                it.copy(
                    phase = AppPhase.READY,
                    user = session.user,
                    formError = null,
                )
            }
        }
    }

    // HU-01 · Administrador inicial
    fun createAdministrator(
        nombre: String,
        correo: String,
        password: String,
        confirmation: String,
    ) {
        val validation = when {
            nombre.trim().length < 2 -> "Escribe tu nombre completo."
            !correo.trim().matches(EMAIL_PATTERN) -> "Ingresa un correo válido."
            password.length < 10 -> "La contraseña debe tener al menos 10 caracteres."
            password != confirmation -> "Las contraseñas deben coincidir."
            else -> null
        }
        if (validation != null) {
            _state.update { it.copy(formError = validation) }
            return
        }
        submit {
            val session = authRepository.createAdministrator(nombre, correo, password)
            _state.update {
                it.copy(
                    phase = AppPhase.READY,
                    user = session.user,
                    formError = null,
                )
            }
        }
    }

    // HU-01 y AN-05 · Cierre de sesión
    fun logout() {
        if (_state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {
                // La sesión local ya se cerró en el repositorio.
            } finally {
                _state.update {
                    it.copy(
                        phase = AppPhase.LOGIN,
                        user = null,
                        isSubmitting = false,
                        formError = null,
                    )
                }
            }
        }
    }

    fun clearFormError() {
        if (_state.value.formError != null) {
            _state.update { it.copy(formError = null) }
        }
    }

    private fun submit(operation: suspend () -> Unit) {
        if (_state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            try {
                operation()
            } catch (error: ApiException) {
                _state.update { it.copy(formError = error.message) }
            } catch (error: Exception) {
                _state.update {
                    it.copy(formError = error.message ?: "No se pudo completar la solicitud.")
                }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun observeSessionExpiry() {
        viewModelScope.launch {
            sessionManager.sessionExpired.collect {
                if (_state.value.phase == AppPhase.READY) {
                    _state.update {
                        it.copy(
                            phase = AppPhase.LOGIN,
                            user = null,
                            formError = "Tu sesión expiró. Vuelve a iniciar sesión.",
                        )
                    }
                }
            }
        }
    }

    companion object {
        private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

        fun factory(
            authRepository: AuthRepository,
            sessionManager: SessionManager,
        ) = viewModelFactory {
            initializer {
                AppViewModel(authRepository, sessionManager)
            }
        }
    }
}
