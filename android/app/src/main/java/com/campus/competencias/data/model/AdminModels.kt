/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/model/AdminModels.kt
 * Propósito: Cuerpos de escritura (POST/PATCH) que replican exactamente los campos
 * aceptados por el backend para cuentas, disciplinas, eventos, participantes,
 * equipos e inscripciones.
 *
 * Historias de usuario:
 * - HU-02 · Gestión de cuentas de usuario
 * - HU-06 · Gestión de disciplinas deportivas
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - HU-10 · Registro de participantes
 * - HU-11 · Gestión de equipos deportivos y tecnológicos
 * - HU-13 · Inscripción deportiva validada
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.data.model

import kotlinx.serialization.Serializable

/**
 * POST /api/usuarios y PATCH /api/usuarios/:id.
 * En creación la contraseña es obligatoria; en edición se omite si no cambia.
 */
@Serializable
data class UserInput(
    val nombre: String,
    val correo: String,
    val rol: String,
    val estado: String,
    val password: String? = null,
)

/** PATCH /api/usuarios/:id para activar o desactivar una cuenta sin tocar el resto. */
@Serializable
data class UserStatusPatch(
    val estado: String,
)

/** Categoría interna de una disciplina. El id solo se envía al editar una existente. */
@Serializable
data class CategoryInput(
    val id: String? = null,
    val nombre: String,
    val edadMin: Int? = null,
    val edadMax: Int? = null,
    val genero: String = "cualquiera",
)

/** POST /api/disciplinas y PATCH /api/disciplinas/:id. */
@Serializable
data class DisciplineInput(
    val nombre: String,
    val modalidades: List<String>,
    val formatos: List<String>,
    val categorias: List<CategoryInput>,
    val estado: String = "activo",
)

/**
 * Reglamento de un torneo. El backend deriva edadMin, edadMax y genero de la
 * categoría, por eso no se envían desde la app.
 */
@Serializable
data class RulesInput(
    val integrantesMin: Int,
    val integrantesMax: Int,
    val maxInscripciones: Int,
    val metrica: String,
    val unidad: String,
    val precision: Int,
    val orden: String,
    val fases: List<String>,
    val avance: String,
    val desempates: List<String>,
    val pruebas: List<String>,
    val requisitos: String,
    val permiteMultiplesEquipos: Boolean,
)

/** POST /api/torneos y PATCH /api/torneos/:id. */
@Serializable
data class TournamentInput(
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val disciplina: String,
    val categoria: String,
    val unidadInscripcion: String,
    val formato: String,
    val ciudad: String,
    val departamento: String,
    val inicio: String,
    val fin: String,
    val inscripcionInicio: String,
    val inscripcionFin: String,
    val cupos: Int,
    val responsable: String,
    val reglamento: RulesInput,
)

/** POST /api/hackatones y PATCH /api/hackatones/:id. */
@Serializable
data class HackathonInput(
    val nombre: String,
    val descripcion: String,
    val ciudad: String,
    val departamento: String,
    val modalidad: String,
    val inicio: String,
    val fin: String,
    val inscripcionInicio: String,
    val inscripcionFin: String,
    val entregaInicio: String,
    val entregaFin: String,
    val evaluacionInicio: String,
    val evaluacionFin: String,
    val cupos: Int,
    val integrantesMin: Int,
    val integrantesMax: Int,
    val proyectosPorEquipo: Int,
    val categorias: List<String>,
    val requisitos: String,
    val responsable: String,
)

/** POST /api/participantes y PATCH /api/participantes/:id. */
@Serializable
data class ParticipantInput(
    val documento: String,
    val nombre: String,
    val nacimiento: String,
    val genero: String,
    val usuario: String,
    val institucion: String,
    val categoria: String,
    val estado: String = "activo",
)

/**
 * POST /api/equipos y PATCH /api/equipos/:id.
 * Los campos nulos se omiten del JSON: un equipo tecnológico no envía
 * institución, disciplina, categoría ni modalidad, y uno deportivo no envía
 * hackatón ni categoría tecnológica.
 */
@Serializable
data class TeamInput(
    val nombre: String,
    val tipo: String,
    val responsable: String,
    val estado: String = "activo",
    val institucion: String? = null,
    val disciplina: String? = null,
    val categoria: String? = null,
    val modalidad: String? = null,
    val hackaton: String? = null,
    val categoriaTecnologica: String? = null,
    val integrantes: List<String> = emptyList(),
)

/** POST /api/inscripciones. */
@Serializable
data class RegistrationInput(
    val torneo: String,
    val modalidad: String,
    val competidor: String,
    val prueba: String = "",
)

/** PATCH /api/torneos/:id/estado y /api/hackatones/:id/estado. */
@Serializable
data class EventStateInput(
    val estado: String,
)
