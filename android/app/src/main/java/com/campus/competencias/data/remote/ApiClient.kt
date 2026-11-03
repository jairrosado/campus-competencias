/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/remote/ApiClient.kt
 * Propósito: Retrofit + OkHttp: URL base dinámica, cookie persistente, CSRF y JSON de kotlinx.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
package com.campus.competencias.data.remote

import com.campus.competencias.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

// HU-01 y AN-01 · URL dinámica, cookie y CSRF
object ApiClient {

    fun create(
        baseUrlProvider: () -> String,
        sessionManager: SessionManager,
        json: Json,
    ): CampusApi {
        val logging = HttpLoggingInterceptor().apply {
            // BASIC evita registrar contraseñas, cookies o tokens CSRF en logcat.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .cookieJar(sessionManager.cookieJar)
            .addInterceptor(BaseUrlInterceptor(baseUrlProvider))
            .addInterceptor(CsrfInterceptor(sessionManager))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()

        // Retrofit exige una URL al construir; el interceptor la reemplaza en cada
        // solicitud para que el usuario pueda cambiar de servidor sin reiniciar.
        val seedUrl = baseUrlProvider().toHttpUrlOrNull() ?: ServerSettings.EMULATOR_URL.toHttpUrlOrNull()!!

        return Retrofit.Builder()
            .baseUrl(seedUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CampusApi::class.java)
    }
}

/**
 * Reescribe esquema, host y puerto de cada solicitud con la dirección vigente.
 * Se conserva la ruta relativa que arma Retrofit para no tocar el contrato HTTP.
 */
private class BaseUrlInterceptor(
    private val baseUrlProvider: () -> String,
) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val base = baseUrlProvider().toHttpUrlOrNull() ?: return chain.proceed(request)
        val redirected = request.url.newBuilder()
            .scheme(base.scheme)
            .host(base.host)
            .port(base.port)
            .build()
        return chain.proceed(request.newBuilder().url(redirected).build())
    }
}

private class CsrfInterceptor(
    private val sessionManager: SessionManager,
) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val method = request.method
        val token = sessionManager.csrfToken
        val secured = method != "GET" && method != "HEAD" && method != "OPTIONS"
        return if (secured && !token.isNullOrBlank()) {
            chain.proceed(
                request.newBuilder()
                    .header("X-CSRF-Token", token)
                    .build(),
            )
        } else {
            chain.proceed(request)
        }
    }
}
