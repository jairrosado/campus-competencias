/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/model/InstitutionModels.kt
 * Propósito: DTO de institución y cuerpos de creación/edición usados por la app Android.
 *
 * Historias de usuario:
 * - HU-04 · Registro y consulta de instituciones
 * - HU-05 · Edición, eliminación y desactivación de instituciones
 * - AN-02 · Consulta de instituciones en Android
 * - AN-03 · Gestión de instituciones en Android
 */
package com.campus.competencias.data.model

import kotlinx.serialization.Serializable

/**
 * publicInstitution(): id, nombre, ciudad, departamento, tipo, estado,
 * createdAt y updatedAt. El campo interno "clave" nunca llega al cliente.
 */
@Serializable
data class InstitutionDto(
    val id: String = "",
    val nombre: String = "",
    val ciudad: String = "",
    val departamento: String = "",
    val tipo: String = "",
    val estado: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

/**
 * POST /api/instituciones y PUT /api/instituciones/:id.
 * PUT exige exactamente los cinco campos editables.
 */
@Serializable
data class InstitutionInput(
    val nombre: String,
    val ciudad: String,
    val departamento: String,
    val tipo: String,
    val estado: String,
)

@Serializable
data class InstitutionPatch(
    val estado: String,
)
