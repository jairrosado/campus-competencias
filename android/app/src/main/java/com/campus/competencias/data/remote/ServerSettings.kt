/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/remote/ServerSettings.kt
 * Propósito: Guarda y valida la URL del servidor para cambiar entre emulador, localhost y celular.
 *
 * Historias de usuario:
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.data.remote

import android.content.Context
import com.campus.competencias.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Dirección de la API elegida por el usuario en este dispositivo.
 *
 * Por defecto se usa la API publicada en Render, así que la app funciona en
 * cualquier celular o emulador con internet sin encender nada en el equipo.
 * El emulador (10.0.2.2) y la IP de red local quedan disponibles para
 * desarrollo. Guardar la dirección permite cambiar de escenario sin recompilar.
 */
// AN-01 y AN-05 · URL del servidor
class ServerSettings(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var baseUrl: String
        get() = normalize(
            preferences.getString(KEY_BASE_URL, null)
                ?: BuildConfig.API_BASE_URL.takeIf(String::isNotBlank)
                ?: defaultBaseUrl(),
        )
        set(value) {
            preferences.edit().putString(KEY_BASE_URL, normalize(value)).apply()
        }

    companion object {
        /** API + web desplegadas en Render: funciona desde cualquier red. */
        const val CLOUD_URL = "https://campus-competencias-api.onrender.com/"
        const val EMULATOR_URL = "http://10.0.2.2:3001/"
        const val DEVICE_URL = "http://192.168.80.40:3001/"

        private const val PREFS_NAME = "campus_server_settings"
        private const val KEY_BASE_URL = "base_url"
        private val SCHEME_PATTERN = Regex("^https?://", RegexOption.IGNORE_CASE)

        /**
         * Sin configuración previa se usa la nube. El emulador y la IP local
         * siguen disponibles desde el diálogo de servidor para desarrollo.
         */
        fun defaultBaseUrl(): String = CLOUD_URL

        /**
         * Acepta "192.168.1.10:3001", "http://192.168.1.10:3001" o una URL completa.
         * Devuelve siempre la raíz del servidor con barra final.
         */
        fun normalize(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return trimmed
            val candidate = if (SCHEME_PATTERN.containsMatchIn(trimmed)) trimmed else "http://$trimmed"
            val url = candidate.toHttpUrlOrNull() ?: return trimmed
            return url.newBuilder()
                .encodedPath("/")
                .query(null)
                .fragment(null)
                .build()
                .toString()
        }

        /** Devuelve el mensaje de error o null cuando la dirección sirve. */
        fun validate(raw: String): String? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return "Escribe la dirección del servidor."
            val candidate = if (SCHEME_PATTERN.containsMatchIn(trimmed)) trimmed else "http://$trimmed"
            val url = candidate.toHttpUrlOrNull()
                ?: return "La dirección no es válida. Ejemplo: http://192.168.1.10:3001"
            if (url.scheme != "http" && url.scheme != "https") {
                return "Usa http:// o https://."
            }
            if (url.host.isBlank()) return "La dirección debe incluir el equipo o la IP."
            if (url.encodedPath != "/") return "Usa solo la dirección del servidor, sin rutas."
            if (url.query != null || url.fragment != null) {
                return "La dirección no debe incluir parámetros ni fragmentos."
            }
            return null
        }

        /** En un celular, localhost apunta al propio teléfono y nunca al backend. */
        fun isLoopback(raw: String): Boolean {
            val host = normalize(raw).toHttpUrlOrNull()?.host ?: return false
            return host == "localhost" || host == "127.0.0.1" || host == "::1"
        }
    }
}
