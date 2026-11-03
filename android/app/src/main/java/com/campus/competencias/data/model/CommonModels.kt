/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/model/CommonModels.kt
 * Propósito: Sobre genérico { data }, error { error } y DTO comunes de paginación y borrado.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - HU-02 · Gestión de cuentas de usuario
 * - HU-12 · Catálogo de apoyo y navegación por rol
 */
package com.campus.competencias.data.model

import kotlinx.serialization.Serializable

/**
 * La API responde siempre con un sobre { "data": ... }.
 * No se modifica el contrato existente del backend.
 */
@Serializable
data class ApiEnvelope<T>(
    val data: T,
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorBody = ApiErrorBody(),
)

@Serializable
data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null,
    val fields: Map<String, String> = emptyMap(),
)

/**
 * Forma común de los listados paginados:
 * { items, total, page, pages, summary }.
 *
 * summary cambia según el recurso (cities en instituciones, admins en usuarios,
 * active en competencias), por eso todos sus contadores son opcionales.
 */
@Serializable
data class PageDto<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 1,
    val summary: SummaryDto = SummaryDto(),
)

@Serializable
data class SummaryDto(
    val total: Int = 0,
    val active: Int = 0,
    val cities: Int = 0,
    val admins: Int = 0,
)

@Serializable
data class MessageDto(
    val message: String = "",
)

@Serializable
data class DeletedDto(
    val id: String = "",
    val message: String = "",
)
