/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/repository/AuthRepository.kt
 * Propósito: Repositorio de setup, login, sesión actual y logout.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
package com.campus.competencias.data.repository

import com.campus.competencias.data.model.LoginRequest
import com.campus.competencias.data.model.SessionDto
import com.campus.competencias.data.model.SetupAdminRequest
import com.campus.competencias.data.model.SetupStatusDto
import com.campus.competencias.data.remote.ApiException
import com.campus.competencias.data.remote.ApiExecutor
import com.campus.competencias.data.remote.CampusApi
import com.campus.competencias.data.remote.SessionManager

class AuthRepository(
    private val api: CampusApi,
    private val executor: ApiExecutor,
    private val sessionManager: SessionManager,
) {

    suspend fun setupStatus(): SetupStatusDto =
        executor.execute { api.setupStatus() }.data

    /**
     * Al volver a abrir la app la cookie puede seguir vigente. /auth/me devuelve
     * un CSRF nuevo para esa sesión. Un 401 aquí es normal y no debe expulsar
     * al usuario de forma abrupta: simplemente significa que debe iniciar sesión.
     */
    // HU-01 · Reanudar sesión
    suspend fun currentSession(): SessionDto? {
        return try {
            val session = executor.execute(expireOn401 = false) { api.me() }.data
            sessionManager.updateCsrf(session.csrfToken)
            session
        } catch (error: ApiException) {
            if (error.status == 401) null else throw error
        }
    }

    // HU-01 y AN-01 · Login
    suspend fun login(correo: String, password: String): SessionDto {
        val session = executor.execute { api.login(LoginRequest(correo.trim(), password)) }.data
        sessionManager.updateCsrf(session.csrfToken)
        return session
    }

    // HU-01 · Administrador inicial
    suspend fun createAdministrator(nombre: String, correo: String, password: String): SessionDto {
        val session = executor.execute {
            api.setupAdministrator(SetupAdminRequest(nombre.trim(), correo.trim(), password))
        }.data
        sessionManager.updateCsrf(session.csrfToken)
        return session
    }

    // HU-01 · Logout
    suspend fun logout() {
        try {
            executor.execute { api.logout() }
        } finally {
            // Aunque falle la red, la sesión local debe quedar cerrada.
            sessionManager.clearSession()
        }
    }
}
