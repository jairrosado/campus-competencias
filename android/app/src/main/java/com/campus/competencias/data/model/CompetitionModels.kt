/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/model/CompetitionModels.kt
 * Propósito: DTO de disciplinas, torneos, hackatones, participantes, equipos e inscripciones.
 *
 * Historias de usuario:
 * - HU-06 · Gestión de disciplinas deportivas
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - HU-10 · Registro de participantes
 * - HU-11 · Gestión de equipos deportivos y tecnológicos
 * - HU-13 · Inscripción deportiva validada
 */
package com.campus.competencias.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String? = null,
    val nombre: String = "",
    val edadMin: Int? = null,
    val edadMax: Int? = null,
    val genero: String = "cualquiera",
)

@Serializable
data class DisciplineDto(
    val id: String = "",
    val nombre: String = "",
    val modalidades: List<String> = emptyList(),
    val formatos: List<String> = emptyList(),
    val categorias: List<CategoryDto> = emptyList(),
    val estado: String = "activo",
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class RulesDto(
    val integrantesMin: Int = 0,
    val integrantesMax: Int = 0,
    val maxInscripciones: Int = 0,
    val metrica: String = "",
    val unidad: String = "",
    val precision: Double = 0.0,
    val orden: String = "",
    val fases: List<String> = emptyList(),
    val avance: String = "",
    val desempates: List<String> = emptyList(),
    val pruebas: List<String> = emptyList(),
    val edadMin: Int? = null,
    val edadMax: Int? = null,
    val genero: String = "",
    val requisitos: String = "",
    val permiteMultiplesEquipos: Boolean = false,
)

@Serializable
data class TournamentDto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val disciplina: String = "",
    val categoria: String = "",
    val categoriaNombre: String = "",
    val unidadInscripcion: String = "",
    val formato: String = "",
    val ciudad: String = "",
    val departamento: String = "",
    val inicio: String = "",
    val fin: String = "",
    val inscripcionInicio: String = "",
    val inscripcionFin: String = "",
    val cupos: Int = 0,
    val responsable: String = "",
    val estado: String = "borrador",
    val reglamento: RulesDto = RulesDto(),
    val reglamentoVersion: Int = 1,
    val creadoPor: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // Campos enriquecidos por enriched() en competition-routes.js
    val disciplinaNombre: String = "",
    val responsableNombre: String = "",
    val inscritos: Int = 0,
)

@Serializable
data class HackathonDto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ciudad: String = "",
    val departamento: String = "",
    val modalidad: String = "",
    val inicio: String = "",
    val fin: String = "",
    val inscripcionInicio: String = "",
    val inscripcionFin: String = "",
    val entregaInicio: String = "",
    val entregaFin: String = "",
    val evaluacionInicio: String = "",
    val evaluacionFin: String = "",
    val cupos: Int = 0,
    val integrantesMin: Int = 0,
    val integrantesMax: Int = 0,
    val proyectosPorEquipo: Int = 0,
    val categorias: List<String> = emptyList(),
    val requisitos: String = "",
    val responsable: String = "",
    val estado: String = "borrador",
    val creadoPor: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // Campo enriquecido por enriched()
    val responsableNombre: String = "",
)

@Serializable
data class ParticipantDto(
    val id: String = "",
    val documento: String = "",
    val nombre: String = "",
    val nacimiento: String = "",
    val genero: String = "",
    val usuario: String = "",
    val institucion: String = "",
    val categoria: String = "",
    val estado: String = "activo",
    val creadoPor: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // Campos enriquecidos por enriched()
    val usuarioNombre: String = "",
    val institucionNombre: String = "",
)

@Serializable
data class TeamDto(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val responsable: String = "",
    val estado: String = "activo",
    val institucion: String = "",
    val disciplina: String = "",
    val categoria: String = "",
    val categoriaNombre: String = "",
    val modalidad: String = "",
    val hackaton: String = "",
    val categoriaTecnologica: String = "",
    val integrantes: List<String> = emptyList(),
    val creadoPor: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // Campos enriquecidos por enriched()
    val institucionNombre: String = "",
    val disciplinaNombre: String = "",
    val responsableNombre: String = "",
    val hackatonNombre: String = "",
)

@Serializable
data class SquadMemberDto(
    val id: String? = null,
    val participante: String = "",
    val nombre: String = "",
    val institucion: String = "",
)

@Serializable
data class RegistrationDto(
    val id: String = "",
    val torneo: String = "",
    val modalidad: String = "",
    val competidor: String = "",
    val institucion: String = "",
    val prueba: String = "",
    val estado: String = "confirmada",
    val creadoPor: String = "",
    val plantilla: List<SquadMemberDto> = emptyList(),
    val reglamentoVersion: Int = 1,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // Campos enriquecidos por enriched()
    val torneoNombre: String = "",
    val competidorNombre: String = "",
    val institucionNombre: String = "",
)

/**
 * GET /api/catalogos devuelve los siete catálogos en un solo objeto.
 * Algunas colecciones se proyectan con .select(...), por eso los DTO usan
 * valores por defecto: los campos no proyectados simplemente llegan ausentes.
 */
@Serializable
data class CatalogDto(
    val instituciones: List<InstitutionDto> = emptyList(),
    val disciplinas: List<DisciplineDto> = emptyList(),
    val usuarios: List<UserDto> = emptyList(),
    val participantes: List<ParticipantDto> = emptyList(),
    val equipos: List<TeamDto> = emptyList(),
    val hackatones: List<HackathonDto> = emptyList(),
    val torneos: List<TournamentDto> = emptyList(),
)
