/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/model/FormOptions.kt
 * Propósito: Opciones válidas de los formularios, alineadas con los enums del backend.
 *
 * Historias de usuario:
 * - HU-06 · Gestión de disciplinas deportivas
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - HU-10 · Registro de participantes
 * - HU-11 · Gestión de equipos deportivos y tecnológicos
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.model

// HU-06 · Modalidades y formatos que acepta una disciplina.
val participationModeOptions: List<Pair<String, String>> = listOf(
    "equipo" to "Equipo",
    "individual" to "Individual",
    "relevo" to "Relevo",
)

val competitionFormatOptions: List<Pair<String, String>> = listOf(
    "liga" to "Liga",
    "grupos" to "Grupos",
    "eliminacion" to "Eliminación",
    "pruebas" to "Pruebas",
    "rondas" to "Rondas",
)

val categoryGenderOptions: List<Pair<String, String>> = listOf(
    "cualquiera" to "Sin restricción",
    "femenino" to "Femenino",
    "masculino" to "Masculino",
)

val participantGenderOptions: List<Pair<String, String>> = listOf(
    "femenino" to "Femenino",
    "masculino" to "Masculino",
    "otro" to "Otro",
)

val activeStateOptions: List<Pair<String, String>> = listOf(
    "activo" to "Activo",
    "inactivo" to "Inactivo",
)

// HU-07 · Tipos de torneo.
val tournamentTypeOptions: List<Pair<String, String>> = listOf(
    "intercolegiados" to "Intercolegiados",
    "universitario" to "Universitario",
)

// HU-09 · Modalidades de hackatón.
val hackathonModalityOptions: List<Pair<String, String>> = listOf(
    "presencial" to "Presencial",
    "virtual" to "Virtual",
    "hibrida" to "Híbrida",
)

// HU-08 · Métricas y orden del reglamento.
val metricOptions: List<Pair<String, String>> = listOf(
    "goles" to "Goles",
    "puntos" to "Puntos",
    "sets" to "Sets",
    "tiempo" to "Tiempo",
    "distancia" to "Distancia",
    "otra" to "Otra",
)

val orderOptions: List<Pair<String, String>> = listOf(
    "ascendente" to "Menor primero",
    "descendente" to "Mayor primero",
)

// HU-11 · Tipos de equipo.
val teamTypeOptions: List<Pair<String, String>> = listOf(
    "deportivo" to "Deportivo",
    "tecnologico" to "Tecnológico",
)

fun optionLabel(options: List<Pair<String, String>>, value: String): String =
    options.firstOrNull { it.first == value }?.second ?: value
