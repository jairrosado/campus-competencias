/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/model/AuthModels.kt
 * Propósito: DTO de usuario, sesión, login, setup y errores de autenticación.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
package com.campus.competencias.data.model

import kotlinx.serialization.Serializable

/**
 * Objeto público de usuario devuelto por publicUser():
 * { id, nombre, correo, rol, estado }.
 */
@Serializable
data class UserDto(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    val estado: String = "",
)

@Serializable
data class SessionDto(
    val user: UserDto = UserDto(),
    val csrfToken: String = "",
)

@Serializable
data class SetupStatusDto(
    val needsSetup: Boolean = false,
    val setupAllowed: Boolean = false,
)

@Serializable
data class LoginRequest(
    val correo: String,
    val password: String,
)

@Serializable
data class SetupAdminRequest(
    val nombre: String,
    val correo: String,
    val password: String,
)
