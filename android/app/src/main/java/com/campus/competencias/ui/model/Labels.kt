/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/model/Labels.kt
 * Propósito: Traducción de códigos de la API a etiquetas legibles en español.
 *
 * Historias de usuario:
 * - HU-12 · Catálogo de apoyo y navegación por rol
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class EventType(val label: String, val singular: String) {
    TOURNAMENTS("Torneos", "Torneo"),
    HACKATHONS("Hackatones", "Hackatón"),
}

enum class RecordKind(
    val title: String,
    val eyebrow: String,
    val description: String,
    val supportsSearch: Boolean = true,
    val supportsStatus: Boolean = true,
) {
    DISCIPLINES(
        title = "Disciplinas",
        eyebrow = "CATÁLOGO DEPORTIVO",
        description = "Modalidades, formatos y categorías disponibles.",
    ),
    PARTICIPANTS(
        title = "Participantes",
        eyebrow = "PERSONAS",
        description = "Deportistas y perfiles vinculados a las instituciones.",
    ),
    TEAMS(
        title = "Equipos",
        eyebrow = "EQUIPOS",
        description = "Equipos deportivos y tecnológicos registrados.",
    ),
    REGISTRATIONS(
        title = "Inscripciones",
        eyebrow = "COMPETENCIAS",
        description = "Competidores confirmados en cada torneo.",
        supportsSearch = false,
        supportsStatus = false,
    ),
}

enum class StatusTone {
    SUCCESS,
    WARNING,
    INFO,
    DANGER,
    NEUTRAL,
    PURPLE,
}

fun roleLabel(value: String): String = when (value) {
    "administrador" -> "Administrador"
    "organizador" -> "Organizador"
    "participante" -> "Participante"
    "jurado" -> "Jurado"
    else -> value.replaceFirstChar { it.uppercase() }
}

fun institutionTypeLabel(value: String): String = when (value) {
    "colegio" -> "Colegio"
    "universidad" -> "Universidad"
    "instituto" -> "Instituto"
    "otro" -> "Otra institución"
    else -> value
}

// HU-12 · Estados legibles
fun statusLabel(value: String): String = when (value) {
    "activo" -> "Activo"
    "inactivo" -> "Inactivo"
    "borrador" -> "Borrador"
    "inscripciones" -> "Inscripciones"
    "en_curso" -> "En curso"
    "finalizado" -> "Finalizado"
    "cancelado" -> "Cancelado"
    "entrega" -> "Entrega de proyectos"
    "evaluacion" -> "Evaluación"
    "publicado" -> "Publicado"
    "confirmada" -> "Confirmada"
    else -> value.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

fun statusTone(value: String): StatusTone = when (value) {
    "activo", "confirmada", "publicado", "finalizado" -> StatusTone.SUCCESS
    "inscripciones", "en_curso" -> StatusTone.INFO
    "borrador", "inactivo" -> StatusTone.NEUTRAL
    "cancelado" -> StatusTone.DANGER
    "entrega", "evaluacion" -> StatusTone.PURPLE
    else -> StatusTone.NEUTRAL
}

fun participationModeLabel(value: String): String = when (value) {
    "equipo" -> "Equipo"
    "individual" -> "Individual"
    "relevo" -> "Relevo"
    else -> value
}

fun competitionFormatLabel(value: String): String = when (value) {
    "liga" -> "Liga"
    "grupos" -> "Grupos"
    "eliminacion" -> "Eliminación"
    "pruebas" -> "Pruebas"
    "rondas" -> "Rondas"
    else -> value
}

fun tournamentTypeLabel(value: String): String = when (value) {
    "intercolegiados" -> "Intercolegiados"
    "universitario" -> "Universitario"
    else -> value
}

fun teamTypeLabel(value: String): String = when (value) {
    "deportivo" -> "Deportivo"
    "tecnologico" -> "Tecnológico"
    else -> value
}

fun modalityLabel(value: String): String = when (value) {
    "presencial" -> "Presencial"
    "virtual" -> "Virtual"
    "hibrida" -> "Híbrida"
    else -> value
}

fun genderLabel(value: String): String = when (value) {
    "cualquiera" -> "Sin restricción"
    "femenino" -> "Femenino"
    "masculino" -> "Masculino"
    "otro" -> "Otro"
    else -> value
}

fun metricLabel(value: String): String = when (value) {
    "goles" -> "Goles"
    "puntos" -> "Puntos"
    "sets" -> "Sets"
    "tiempo" -> "Tiempo"
    "distancia" -> "Distancia"
    "otra" -> "Otra"
    else -> value
}

fun orderLabel(value: String): String = when (value) {
    "ascendente" -> "Menor resultado primero"
    "descendente" -> "Mayor resultado primero"
    else -> value
}

fun ageRangeLabel(min: Int?, max: Int?): String = when {
    min != null && max != null -> "$min a $max años"
    min != null -> "Desde $min años"
    max != null -> "Hasta $max años"
    else -> "Sin límite de edad"
}

fun initials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return "?"
    return words.take(2).joinToString("") { word -> word.first().uppercase() }
}

fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "Sin fecha"
    return runCatching {
        val zoned = ZonedDateTime.ofInstant(Instant.parse(value), ZoneId.systemDefault())
        zoned.format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", LOCALE))
    }.getOrDefault(value)
}

fun formatDateOnly(value: String?): String {
    if (value.isNullOrBlank()) return "Sin fecha"
    return runCatching {
        LocalDate.parse(value.take(10)).format(DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE))
    }.getOrDefault(value)
}

fun formatDateRange(start: String?, end: String?): String {
    val from = formatDateTime(start)
    val to = formatDateTime(end)
    return when {
        start.isNullOrBlank() && end.isNullOrBlank() -> "Sin fecha"
        start.isNullOrBlank() -> "Hasta $to"
        end.isNullOrBlank() -> "Desde $from"
        else -> "$from — $to"
    }
}

/**
 * El backend solo guarda "inscripciones" como estado; la UI distingue si la
 * ventana ya abrió o cerró usando las fechas que ya vienen en el JSON.
 */
fun registrationPhase(status: String, start: String, end: String): String {
    if (status != "inscripciones") return statusLabel(status)
    val now = Instant.now()
    val from = runCatching { Instant.parse(start) }.getOrNull()
    val to = runCatching { Instant.parse(end) }.getOrNull()
    return when {
        from != null && now.isBefore(from) -> "Inscripciones próximas"
        to != null && now.isAfter(to) -> "Inscripciones cerradas"
        else -> "Inscripciones abiertas"
    }
}

private val LOCALE: Locale = Locale.forLanguageTag("es-CO")
