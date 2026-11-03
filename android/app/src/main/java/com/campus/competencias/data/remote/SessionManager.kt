/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/remote/SessionManager.kt
 * Propósito: Mantiene el csrfToken y notifica cuando la sesión deja de ser válida.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
package com.campus.competencias.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Mantiene el token CSRF de la sesión y avisa cuando la cookie deja de ser válida.
 *
 * La API exige X-CSRF-Token en POST/PUT/PATCH/DELETE. El token se obtiene de
 * /api/auth/login, /api/setup/administrador o /api/auth/me.
 */
// HU-01 · csrfToken y expiración de sesión
class SessionManager(
    val cookieJar: SessionCookieJar,
) {
    @Volatile
    var csrfToken: String? = null
        private set

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired = _sessionExpired.asSharedFlow()

    fun updateCsrf(token: String) {
        csrfToken = token.ifBlank { null }
    }

    fun clearSession() {
        csrfToken = null
        cookieJar.clear()
    }

    fun onUnauthorized() {
        clearSession()
        _sessionExpired.tryEmit(Unit)
    }
}
