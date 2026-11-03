/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/remote/SessionCookieJar.kt
 * Propósito: CookieJar persistente para conservar campus_session entre solicitudes y reinicios.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
package com.campus.competencias.data.remote

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * El backend usa una cookie de sesión HttpOnly llamada "campus_session".
 * OkHttp necesita un CookieJar para conservarla entre solicitudes, y aquí
 * además se guarda en SharedPreferences para sobrevivir al cierre de la app.
 */
class SessionCookieJar(context: Context) : CookieJar {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val lock = Any()
    private val cookies = linkedMapOf<String, StoredCookie>()

    init {
        restore()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(lock) {
            cookies.forEach { cookie ->
                if (cookie.expiresAt <= System.currentTimeMillis()) {
                    this.cookies.remove(cookie.name)
                } else {
                    this.cookies[cookie.name] = StoredCookie(url.toString(), cookie.toString())
                }
            }
            persist()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            val valid = cookies.values.mapNotNull { stored ->
                stored.url.toHttpUrlOrNull()?.let { origin -> Cookie.parse(origin, stored.raw) }
            }.filter { it.expiresAt > now && it.matches(url) }
            return valid
        }
    }

    fun clear() {
        synchronized(lock) {
            cookies.clear()
            preferences.edit().clear().apply()
        }
    }

    private fun persist() {
        val editor = preferences.edit().clear()
        cookies.forEach { (name, stored) -> editor.putString(name, "${stored.url}\u0000${stored.raw}") }
        editor.apply()
    }

    private fun restore() {
        preferences.all.forEach { (name, value) ->
            val raw = value as? String ?: return@forEach
            val separator = raw.indexOf('\u0000')
            if (separator <= 0) return@forEach
            val url = raw.substring(0, separator)
            val cookie = raw.substring(separator + 1)
            if (url.isNotBlank() && cookie.isNotBlank()) {
                cookies[name] = StoredCookie(url, cookie)
            }
        }
    }

    private data class StoredCookie(
        val url: String,
        val raw: String,
    )

    private companion object {
        const val PREFS_NAME = "campus_session_cookies"
    }
}
