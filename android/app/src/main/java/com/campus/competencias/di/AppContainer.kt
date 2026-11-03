/**
 * Archivo: android/app/src/main/java/com/campus/competencias/di/AppContainer.kt
 * Propósito: Grafo de dependencias manual: ajustes de servidor, sesión, API, repositorios y ViewModels.
 *
 * Historias de usuario:
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.di

import android.content.Context
import com.campus.competencias.data.remote.ApiClient
import com.campus.competencias.data.remote.ApiExecutor
import com.campus.competencias.data.remote.ServerSettings
import com.campus.competencias.data.remote.SessionCookieJar
import com.campus.competencias.data.remote.SessionManager
import com.campus.competencias.data.repository.AuthRepository
import com.campus.competencias.data.repository.CampusRepository
import kotlinx.serialization.json.Json

/**
 * Inyección de dependencias manual, sin frameworks adicionales.
 * Un único grafo por proceso; los ViewModels reciben solo lo que necesitan.
 */
class AppContainer(context: Context) {

    private val serverSettings = ServerSettings(context.applicationContext)

    /** Dirección vigente de la API; se lee cada vez que la interfaz la muestra. */
    val apiBaseUrl: String
        get() = serverSettings.baseUrl

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    val cookieJar = SessionCookieJar(context.applicationContext)

    val sessionManager = SessionManager(cookieJar)

    private val api = ApiClient.create(
        baseUrlProvider = { serverSettings.baseUrl },
        sessionManager = sessionManager,
        json = json,
    )

    private val executor = ApiExecutor(json, sessionManager)

    val authRepository = AuthRepository(api, executor, sessionManager)

    val campusRepository = CampusRepository(api, executor)

    /**
     * Guarda la nueva dirección y descarta la sesión anterior: la cookie
     * pertenece al servidor anterior y no debe viajar al nuevo.
     */
    fun changeServer(baseUrl: String) {
        serverSettings.baseUrl = baseUrl
        sessionManager.clearSession()
    }
}
