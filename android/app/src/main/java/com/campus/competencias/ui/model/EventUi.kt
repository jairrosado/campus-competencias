/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/model/EventUi.kt
 * Propósito: Modelo de presentación de eventos: fechas, estados y textos visibles.
 *
 * Historias de usuario:
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.model

import com.campus.competencias.data.model.DisciplineDto
import com.campus.competencias.data.model.HackathonDto
import com.campus.competencias.data.model.ParticipantDto
import com.campus.competencias.data.model.RegistrationDto
import com.campus.competencias.data.model.RulesDto
import com.campus.competencias.data.model.TeamDto
import com.campus.competencias.data.model.TournamentDto

data class DetailItem(
    val label: String,
    val value: String,
)

/**
 * Modelo de presentación derivado del JSON. No reemplaza a los DTO: los DTO
 * siguen siendo el contrato exacto con el backend y aquí solo se preparan
 * textos y datos para las tarjetas.
 */
data class EventCardData(
    val id: String,
    val type: EventType,
    val name: String,
    val description: String,
    val status: String,
    val statusText: String,
    val location: String,
    val startsAt: String,
    val endsAt: String,
    val registrationStart: String,
    val registrationEnd: String,
    val capacity: Int,
    val registered: Int?,
    val discipline: String,
    val category: String,
    val organizer: String,
    val modality: String,
    val tags: List<String>,
    val details: List<DetailItem>,
    val rules: RulesDto? = null,
    val requirements: String? = null,
    val categories: List<String> = emptyList(),
    val teamRange: String? = null,
    val projectsPerTeam: Int? = null,
)

// HU-07 y HU-08 · Torneo a tarjeta
fun TournamentDto.toCard(): EventCardData {
    val eventDetails = listOf(
        DetailItem("Inscripciones", formatDateRange(inscripcionInicio, inscripcionFin)),
        DetailItem("Evento", formatDateRange(inicio, fin)),
        DetailItem("Cupos", "$inscritos de $cupos inscritos"),
        DetailItem("Disciplina", disciplinaNombre.ifBlank { "Sin disciplina" }),
        DetailItem("Categoría", categoriaNombre.ifBlank { "Sin categoría" }),
        DetailItem("Responsable", responsableNombre.ifBlank { "Sin responsable" }),
        DetailItem("Métrica", metricLabel(reglamento.metrica).ifBlank { "Sin métrica" }),
        DetailItem("Reglamento", "Versión ${reglamentoVersion}"),
    )
    return EventCardData(
        id = id,
        type = EventType.TOURNAMENTS,
        name = nombre,
        description = descripcion,
        status = estado,
        statusText = registrationPhase(estado, inscripcionInicio, inscripcionFin),
        location = listOf(ciudad, departamento).filter { it.isNotBlank() }.joinToString(", "),
        startsAt = inicio,
        endsAt = fin,
        registrationStart = inscripcionInicio,
        registrationEnd = inscripcionFin,
        capacity = cupos,
        registered = inscritos,
        discipline = disciplinaNombre,
        category = categoriaNombre,
        organizer = responsableNombre,
        modality = participationModeLabel(unidadInscripcion),
        tags = listOfNotNull(
            tournamentTypeLabel(tipo).takeIf { tipo.isNotBlank() },
            participationModeLabel(unidadInscripcion).takeIf { unidadInscripcion.isNotBlank() },
            competitionFormatLabel(formato).takeIf { formato.isNotBlank() },
        ),
        details = eventDetails,
        rules = reglamento,
    )
}

// HU-09 · Hackatón a tarjeta
fun HackathonDto.toCard(): EventCardData {
    val details = listOf(
        DetailItem("Inscripciones", formatDateRange(inscripcionInicio, inscripcionFin)),
        DetailItem("Evento", formatDateRange(inicio, fin)),
        DetailItem("Entregas", formatDateRange(entregaInicio, entregaFin)),
        DetailItem("Evaluación", formatDateRange(evaluacionInicio, evaluacionFin)),
        DetailItem("Cupos", "$cupos equipos"),
        DetailItem("Integrantes", "$integrantesMin a $integrantesMax por equipo"),
        DetailItem("Proyectos", "$proyectosPorEquipo por equipo"),
        DetailItem("Responsable", responsableNombre.ifBlank { "Sin responsable" }),
        DetailItem("Modalidad", modalityLabel(modalidad).ifBlank { "Sin modalidad" }),
    )
    return EventCardData(
        id = id,
        type = EventType.HACKATHONS,
        name = nombre,
        description = descripcion,
        status = estado,
        statusText = registrationPhase(estado, inscripcionInicio, inscripcionFin),
        location = listOf(ciudad, departamento).filter { it.isNotBlank() }.joinToString(", "),
        startsAt = inicio,
        endsAt = fin,
        registrationStart = inscripcionInicio,
        registrationEnd = inscripcionFin,
        capacity = cupos,
        registered = null,
        discipline = "",
        category = "",
        organizer = responsableNombre,
        modality = modalityLabel(modalidad),
        tags = listOfNotNull(
            "Hackatón",
            modalityLabel(modalidad).takeIf { modalidad.isNotBlank() },
        ),
        details = details,
        requirements = requisitos.takeIf { it.isNotBlank() },
        categories = categorias,
        teamRange = "$integrantesMin – $integrantesMax integrantes",
        projectsPerTeam = proyectosPorEquipo,
    )
}

data class RecordCardData(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String? = null,
    val status: String,
    val statusText: String,
    val tags: List<String>,
    val lines: List<String>,
)

fun DisciplineDto.toRecordCard(): RecordCardData {
    val categoryLine = categorias.take(4).joinToString(" · ") { category ->
        listOf(
            category.nombre,
            ageRangeLabel(category.edadMin, category.edadMax),
            genderLabel(category.genero),
        ).filter { it.isNotBlank() }.joinToString(" / ")
    }.let { text ->
        if (categorias.size > 4) "$text · +${categorias.size - 4} más" else text
    }
    return RecordCardData(
        id = id,
        title = nombre,
        subtitle = "${modalidades.size} modalidades · ${formatos.size} formatos · ${categorias.size} categorías",
        status = estado,
        statusText = statusLabel(estado),
        tags = modalidades.map(::participationModeLabel) + formatos.map(::competitionFormatLabel),
        lines = listOfNotNull(
            "Categorías: $categoryLine".takeIf { categoryLine.isNotBlank() },
        ),
    )
}

fun ParticipantDto.toRecordCard(): RecordCardData = RecordCardData(
    id = id,
    title = nombre,
    subtitle = institucionNombre.ifBlank { "Sin institución" },
    status = estado,
    statusText = statusLabel(estado),
    tags = listOfNotNull(
        genderLabel(genero).takeIf { genero.isNotBlank() },
        categoria.takeIf { it.isNotBlank() },
    ),
    lines = listOfNotNull(
        "Documento: $documento".takeIf { documento.isNotBlank() },
        "Nacimiento: ${formatDateOnly(nacimiento)}".takeIf { nacimiento.isNotBlank() },
        "Cuenta: $usuarioNombre".takeIf { usuarioNombre.isNotBlank() },
    ),
)

// HU-11 · Equipo a tarjeta
fun TeamDto.toRecordCard(): RecordCardData {
    val context = listOf(institucionNombre, disciplinaNombre).filter { it.isNotBlank() }.joinToString(" · ")
    return RecordCardData(
        id = id,
        title = nombre,
        subtitle = context.ifBlank { "Sin institución asociada" },
        status = estado,
        statusText = statusLabel(estado),
        tags = listOfNotNull(
            teamTypeLabel(tipo).takeIf { tipo.isNotBlank() },
            participationModeLabel(modalidad).takeIf { modalidad.isNotBlank() },
            categoriaNombre.takeIf { it.isNotBlank() },
        ),
        lines = listOfNotNull(
            "Integrantes: ${integrantes.size}".takeIf { integrantes.isNotEmpty() },
            "Categoría tecnológica: $categoriaTecnologica".takeIf { categoriaTecnologica.isNotBlank() },
            "Hackatón: $hackatonNombre".takeIf { hackatonNombre.isNotBlank() },
            "Responsable: $responsableNombre".takeIf { responsableNombre.isNotBlank() },
        ),
    )
}

// HU-13 · Inscripción a tarjeta
fun RegistrationDto.toRecordCard(): RecordCardData = RecordCardData(
    id = id,
    title = competidorNombre.ifBlank { "Competidor no disponible" },
    subtitle = torneoNombre.ifBlank { "Torneo no disponible" },
    status = estado,
    statusText = statusLabel(estado),
    tags = listOfNotNull(
        participationModeLabel(modalidad).takeIf { modalidad.isNotBlank() },
        statusLabel(estado),
    ),
    lines = listOfNotNull(
        "Prueba: $prueba".takeIf { prueba.isNotBlank() },
        "Institución: $institucionNombre".takeIf { institucionNombre.isNotBlank() },
        "Plantilla: ${plantilla.size} integrantes".takeIf { plantilla.isNotEmpty() },
        "Reglamento: versión $reglamentoVersion",
    ),
)
