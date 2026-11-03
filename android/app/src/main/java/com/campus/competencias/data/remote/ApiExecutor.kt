/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/remote/ApiExecutor.kt
 * Propósito: Ejecuta llamadas Retrofit y traduce los errores del backend a ApiException.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
package com.campus.competencias.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiException(
    val status: Int,
    val code: String? = null,
    override val message: String,
    val fields: Map<String, String> = emptyMap(),
) : IOException(message)

/**
 * Ejecuta una llamada Retrofit y traduce el sobre de error del backend:
 * { "error": { "code", "message", "fields" } }.
 */
class ApiExecutor(
    private val json: Json,
    private val sessionManager: SessionManager,
) {

    suspend fun <T> execute(
        expireOn401: Boolean = true,
        request: suspend () -> T,
    ): T {
        return try {
            request()
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            val status = error.code()
            val body = runCatching { error.response()?.errorBody()?.string() }.getOrNull()
            val parsed = body
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<com.campus.competencias.data.model.ApiErrorEnvelope>(it) }.getOrNull() }
            if (status == 401 && expireOn401) sessionManager.onUnauthorized()
            throw ApiException(
                status = status,
                code = parsed?.error?.code,
                message = parsed?.error?.message ?: defaultMessage(status),
                fields = parsed?.error?.fields.orEmpty(),
            )
        } catch (error: SocketTimeoutException) {
            throw ApiException(0, "TIMEOUT", "El servidor tardó demasiado en responder. Intenta nuevamente.")
        } catch (error: UnknownHostException) {
            throw ApiException(0, "NETWORK", networkMessage())
        } catch (error: IOException) {
            throw ApiException(0, "NETWORK", networkMessage())
        } catch (error: SerializationException) {
            throw ApiException(0, "PARSE", "La respuesta del servidor no tiene el formato esperado.")
        }
    }

    private fun networkMessage() =
        "No pudimos conectar con el servidor. Verifica que la API esté encendida y que la dirección configurada sea correcta."

    private fun defaultMessage(status: Int) = when (status) {
        401 -> "Inicia sesión para continuar."
        403 -> "No tienes permiso para realizar esta acción."
        404 -> "El recurso ya no está disponible."
        409 -> "La operación entra en conflicto con datos existentes."
        429 -> "Demasiados intentos. Espera un momento."
        in 500..599 -> "No se pudo completar la operación. Intenta nuevamente."
        else -> "No se pudo completar la solicitud."
    }
}
