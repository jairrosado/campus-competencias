/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/events/EventDialogs.kt
 * Propósito: Formularios de creación y edición de torneos y hackatones, con fechas,
 * reglamento y referencias tomadas de los catálogos.
 *
 * Historias de usuario:
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.events

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.model.CatalogDto
import com.campus.competencias.data.model.HackathonDto
import com.campus.competencias.data.model.HackathonInput
import com.campus.competencias.data.model.RulesInput
import com.campus.competencias.data.model.TournamentDto
import com.campus.competencias.data.model.TournamentInput
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.ui.components.CampusDropdown
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.FormDialogShell
import com.campus.competencias.ui.components.FormGap
import com.campus.competencias.ui.components.FormHint
import com.campus.competencias.ui.components.FormSection
import com.campus.competencias.ui.components.FormTextField
import com.campus.competencias.ui.model.competitionFormatOptions
import com.campus.competencias.ui.model.compareInputDates
import com.campus.competencias.ui.model.hackathonModalityOptions
import com.campus.competencias.ui.model.inputDate
import com.campus.competencias.ui.model.inputToIso
import com.campus.competencias.ui.model.isValidInputDate
import com.campus.competencias.ui.model.isoToInput
import com.campus.competencias.ui.model.metricOptions
import com.campus.competencias.ui.model.optionLabel
import com.campus.competencias.ui.model.orderOptions
import com.campus.competencias.ui.model.participationModeLabel
import com.campus.competencias.ui.model.roleLabel
import com.campus.competencias.ui.model.tournamentTypeOptions

/**
 * HU-07 y HU-08 · Alta y edición de torneos.
 * El reglamento se envía completo porque el backend exige todos sus campos y lo
 * bloquea cuando el torneo deja de estar en borrador.
 */
@Composable
fun TournamentFormDialog(
    tournament: TournamentDto?,
    catalogs: CatalogDto,
    currentUser: UserDto,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (TournamentInput) -> Unit,
) {
    val key = tournament?.id ?: "new"

    val disciplineOptions = catalogs.disciplinas
        .filter { it.estado == "activo" || it.id == tournament?.disciplina }
        .map { it.id to it.nombre }

    val ownerOptions = catalogs.usuarios
        .filter { candidate ->
            candidate.rol in EVENT_OWNER_ROLES &&
                (candidate.estado == "activo" || candidate.id == tournament?.responsable) &&
                (currentUser.rol == "administrador" || candidate.id == currentUser.id)
        }
        .map { it.id to "${it.nombre} (${roleLabel(it.rol)})" }

    var nombre by rememberSaveable(key) { mutableStateOf(tournament?.nombre.orEmpty()) }
    var descripcion by rememberSaveable(key) { mutableStateOf(tournament?.descripcion.orEmpty()) }
    var tipo by rememberSaveable(key) {
        mutableStateOf(tournament?.tipo?.takeIf { it.isNotBlank() } ?: "intercolegiados")
    }
    var disciplinaId by rememberSaveable(key) {
        mutableStateOf(tournament?.disciplina ?: disciplineOptions.firstOrNull()?.first.orEmpty())
    }
    val discipline = catalogs.disciplinas.firstOrNull { it.id == disciplinaId }
    val categoryOptions = discipline?.categorias
        .orEmpty()
        .mapNotNull { category -> category.id?.let { it to category.nombre } }
    var categoria by rememberSaveable(key) {
        mutableStateOf(tournament?.categoria ?: categoryOptions.firstOrNull()?.first.orEmpty())
    }
    var unidadInscripcion by rememberSaveable(key) {
        mutableStateOf(
            tournament?.unidadInscripcion
                ?: discipline?.modalidades?.firstOrNull().orEmpty(),
        )
    }
    var formato by rememberSaveable(key) {
        mutableStateOf(tournament?.formato ?: discipline?.formatos?.firstOrNull().orEmpty())
    }
    var ciudad by rememberSaveable(key) { mutableStateOf(tournament?.ciudad.orEmpty()) }
    var departamento by rememberSaveable(key) { mutableStateOf(tournament?.departamento.orEmpty()) }
    var cupos by rememberSaveable(key) {
        mutableStateOf(tournament?.cupos?.takeIf { it > 0 }?.toString() ?: "64")
    }
    var responsable by rememberSaveable(key) {
        mutableStateOf(
            tournament?.responsable
                ?: currentUser.id.takeIf { id -> ownerOptions.any { it.first == id } }
                ?: ownerOptions.firstOrNull()?.first.orEmpty(),
        )
    }

    var inscripcionInicio by rememberSaveable(key) {
        mutableStateOf(
            tournament?.let { isoToInput(it.inscripcionInicio) }.orEmpty()
                .ifBlank { inputDate(0) },
        )
    }
    var inscripcionFin by rememberSaveable(key) {
        mutableStateOf(
            tournament?.let { isoToInput(it.inscripcionFin) }.orEmpty()
                .ifBlank { inputDate(7, 18) },
        )
    }
    var inicio by rememberSaveable(key) {
        mutableStateOf(
            tournament?.let { isoToInput(it.inicio) }.orEmpty()
                .ifBlank { inputDate(10) },
        )
    }
    var fin by rememberSaveable(key) {
        mutableStateOf(
            tournament?.let { isoToInput(it.fin) }.orEmpty()
                .ifBlank { inputDate(20, 18) },
        )
    }

    var reglaMin by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.integrantesMin?.takeIf { it > 0 }?.toString() ?: "1")
    }
    var reglaMax by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.integrantesMax?.takeIf { it > 0 }?.toString() ?: "5")
    }
    var maxInscripciones by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.maxInscripciones?.takeIf { it > 0 }?.toString() ?: "1")
    }
    var metrica by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.metrica?.takeIf { it.isNotBlank() } ?: "goles")
    }
    var reglaUnidad by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.unidad?.takeIf { it.isNotBlank() } ?: "goles")
    }
    var precision by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.precision?.toInt()?.toString() ?: "0")
    }
    var orden by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.orden?.takeIf { it.isNotBlank() } ?: "descendente")
    }
    var fases by rememberSaveable(key) {
        mutableStateOf(
            tournament?.reglamento?.fases
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(", ")
                ?: "Fase regular",
        )
    }
    var avance by rememberSaveable(key) {
        mutableStateOf(
            tournament?.reglamento?.avance?.takeIf { it.isNotBlank() }
                ?: "Clasifica el primero de cada grupo.",
        )
    }
    var desempates by rememberSaveable(key) {
        mutableStateOf(
            tournament?.reglamento?.desempates
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(", ")
                ?: "Diferencia de goles",
        )
    }
    var pruebas by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.pruebas?.joinToString(", ").orEmpty())
    }
    var requisitos by rememberSaveable(key) {
        mutableStateOf(tournament?.reglamento?.requisitos.orEmpty())
    }
    var permiteMultiples by rememberSaveable(key) {
        mutableStateOf((tournament?.reglamento?.permiteMultiplesEquipos ?: false).toString())
    }
    var localError by rememberSaveable(key) { mutableStateOf<String?>(null) }

    val modalityOptions = discipline?.modalidades.orEmpty().map {
        it to participationModeLabel(it)
    }
    val formatOptions = discipline?.formatos.orEmpty().map {
        it to optionLabel(competitionFormatOptions, it)
    }
    val individual = unidadInscripcion == "individual"

    fun submit() {
        val name = nombre.trim()
        val description = descripcion.trim()
        val city = ciudad.trim()
        val department = departamento.trim()
        val cuposValue = cupos.trim().toIntOrNull()
        val minValue = reglaMin.trim().toIntOrNull()
        val maxValue = reglaMax.trim().toIntOrNull()
        val maxInscValue = maxInscripciones.trim().toIntOrNull()
        val precisionValue = precision.trim().toIntOrNull()
        val fasesList = splitValues(fases)
        val desempatesList = splitValues(desempates)
        val pruebasList = splitValues(pruebas)
        val dates = listOf(inscripcionInicio, inscripcionFin, inicio, fin)
        val datesValid = dates.all(::isValidInputDate)

        val validation = when {
            name.length !in 2..150 -> "Escribe el nombre del torneo (2 a 150 caracteres)."
            description.length > 2000 -> "La descripción no puede superar 2000 caracteres."
            city.length !in 2..100 -> "Escribe la ciudad del torneo."
            department.length !in 2..100 -> "Escribe el departamento del torneo."
            disciplinaId.isBlank() -> "Selecciona la disciplina."
            categoryOptions.isEmpty() -> "La disciplina no tiene categorías registradas."
            categoria.isBlank() -> "Selecciona la categoría."
            unidadInscripcion.isBlank() -> "Selecciona la modalidad de inscripción."
            formato.isBlank() -> "Selecciona el formato de competencia."
            responsable.isBlank() -> "Selecciona el responsable del torneo."
            cuposValue == null || cuposValue !in 1..10000 -> "Los cupos deben estar entre 1 y 10000."
            !datesValid -> "Completa las fechas con el formato AAAA-MM-DD HH:mm."
            minValue == null || minValue !in 1..100 ->
                "Los integrantes mínimos deben estar entre 1 y 100."
            maxValue == null || maxValue !in (minValue ?: 1)..100 ->
                "Los integrantes máximos deben ser mayores o iguales al mínimo."
            individual && (minValue != 1 || maxValue != 1) ->
                "La modalidad individual exige exactamente un integrante."
            maxInscValue == null || maxInscValue !in 1..30 ->
                "Las inscripciones por competidor deben estar entre 1 y 30."
            metrica.isBlank() -> "Selecciona la métrica."
            reglaUnidad.length !in 1..40 -> "Escribe la unidad de la métrica."
            precisionValue == null || precisionValue !in 0..6 ->
                "Los decimales deben estar entre 0 y 6."
            orden.isBlank() -> "Selecciona el orden de los resultados."
            fasesList.isEmpty() -> "Agrega al menos una fase."
            fasesList.size > 30 || fasesList.any { it.length > 150 } || hasDuplicates(fasesList) ->
                "Revisa las fases: máximo 30 valores de 150 caracteres, sin repetir."
            avance.length !in 1..1000 -> "Describe el criterio de avance."
            desempatesList.isEmpty() -> "Agrega al menos un desempate."
            desempatesList.size > 30 || desempatesList.any { it.length > 150 } ||
                hasDuplicates(desempatesList) ->
                "Revisa los desempates: máximo 30 valores de 150 caracteres, sin repetir."
            formato == "pruebas" && pruebasList.isEmpty() ->
                "El formato por pruebas exige al menos una prueba."
            pruebasList.size > 30 || pruebasList.any { it.length > 150 } ||
                hasDuplicates(pruebasList) ->
                "Revisa las pruebas: máximo 30 valores de 150 caracteres, sin repetir."
            requisitos.trim().length > 2000 -> "Los requisitos no pueden superar 2000 caracteres."
            else -> null
        }
        if (validation != null) {
            localError = validation
            return
        }

        if (compareInputDates(inscripcionInicio, inscripcionFin) > 0 ||
            compareInputDates(inscripcionFin, inicio) > 0 ||
            compareInputDates(inicio, fin) > 0
        ) {
            localError = "Ordena las fechas: inscripciones, luego inicio y fin del torneo."
            return
        }

        val isoInscripcionInicio = inputToIso(inscripcionInicio)
        val isoInscripcionFin = inputToIso(inscripcionFin)
        val isoInicio = inputToIso(inicio)
        val isoFin = inputToIso(fin)
        if (isoInscripcionInicio == null || isoInscripcionFin == null ||
            isoInicio == null || isoFin == null
        ) {
            localError = "No pudimos interpretar una de las fechas."
            return
        }

        localError = null
        onConfirm(
            TournamentInput(
                nombre = name,
                descripcion = description,
                tipo = tipo,
                disciplina = disciplinaId,
                categoria = categoria,
                unidadInscripcion = unidadInscripcion,
                formato = formato,
                ciudad = city,
                departamento = department,
                inicio = isoInicio,
                fin = isoFin,
                inscripcionInicio = isoInscripcionInicio,
                inscripcionFin = isoInscripcionFin,
                cupos = cuposValue ?: 1,
                responsable = responsable,
                reglamento = RulesInput(
                    integrantesMin = if (individual) 1 else (minValue ?: 1),
                    integrantesMax = if (individual) 1 else (maxValue ?: 1),
                    maxInscripciones = maxInscValue ?: 1,
                    metrica = metrica,
                    unidad = reglaUnidad.trim(),
                    precision = precisionValue ?: 0,
                    orden = orden,
                    fases = fasesList,
                    avance = avance.trim(),
                    desempates = desempatesList,
                    pruebas = pruebasList,
                    requisitos = requisitos.trim(),
                    permiteMultiplesEquipos = permiteMultiples == "true",
                ),
            ),
        )
    }

    FormDialogShell(
        title = if (tournament == null) "Nuevo torneo" else "Editar torneo",
        subtitle = "Fechas, cupos, responsables y reglamento deportivo.",
        icon = Icons.Rounded.EmojiEvents,
        confirmLabel = if (tournament == null) "Registrar" else "Guardar cambios",
        isSaving = isSaving,
        error = localError ?: error,
        onDismiss = onDismiss,
        onConfirm = ::submit,
    ) {
        if (disciplineOptions.isEmpty()) {
            Text(
                text = "No hay disciplinas activas. Crea una disciplina antes de registrar torneos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(10.dp))
        }
        if (tournament != null && tournament.estado != "borrador") {
            FormHint(
                "Este torneo ya abrió inscripciones: el reglamento y el formato " +
                    "quedan bloqueados por el backend.",
            )
            Spacer(Modifier.height(10.dp))
        }

        FormSection("Datos del torneo")
        FormTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                localError = null
            },
            label = "Nombre",
            placeholder = "Ej. Intercolegiados 2026",
        )
        FormGap()
        FormTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = "Descripción",
            placeholder = "Describe el torneo (opcional)",
            singleLine = false,
            minLines = 2,
        )
        FormGap()
        FormHint("Tipo de torneo")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = tournamentTypeOptions,
            selected = tipo,
            onSelect = { tipo = it ?: "intercolegiados" },
        )
        FormGap()
        CampusDropdown(
            label = "Disciplina",
            options = disciplineOptions,
            selected = disciplinaId.takeIf { it.isNotBlank() },
            onSelect = { value ->
                disciplinaId = value
                val selected = catalogs.disciplinas.firstOrNull { it.id == value }
                categoria = selected?.categorias?.firstOrNull()?.id.orEmpty()
                unidadInscripcion = selected?.modalidades?.firstOrNull().orEmpty()
                formato = selected?.formatos?.firstOrNull().orEmpty()
                localError = null
            },
        )
        FormGap()
        CampusDropdown(
            label = "Categoría",
            options = categoryOptions,
            selected = categoria.takeIf { it.isNotBlank() },
            onSelect = {
                categoria = it
                localError = null
            },
        )
        FormGap()
        FormHint("Modalidad de inscripción")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = modalityOptions,
            selected = unidadInscripcion.takeIf { it.isNotBlank() },
            onSelect = { value ->
                unidadInscripcion = value.orEmpty()
                if (value == "individual") {
                    reglaMin = "1"
                    reglaMax = "1"
                }
                localError = null
            },
        )
        FormGap()
        FormHint("Formato de competencia")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = formatOptions,
            selected = formato.takeIf { it.isNotBlank() },
            onSelect = {
                formato = it.orEmpty()
                localError = null
            },
        )

        Spacer(Modifier.height(20.dp))
        FormSection("Fechas", "Formato local AAAA-MM-DD HH:mm; se envía en ISO 8601.")
        FormTextField(
            value = inscripcionInicio,
            onValueChange = {
                inscripcionInicio = it
                localError = null
            },
            label = "Apertura de inscripciones",
            placeholder = "2026-11-01 08:00",
        )
        FormGap()
        FormTextField(
            value = inscripcionFin,
            onValueChange = {
                inscripcionFin = it
                localError = null
            },
            label = "Cierre de inscripciones",
            placeholder = "2026-11-08 18:00",
        )
        FormGap()
        FormTextField(
            value = inicio,
            onValueChange = {
                inicio = it
                localError = null
            },
            label = "Inicio del torneo",
            placeholder = "2026-11-10 08:00",
        )
        FormGap()
        FormTextField(
            value = fin,
            onValueChange = {
                fin = it
                localError = null
            },
            label = "Fin del torneo",
            placeholder = "2026-11-20 18:00",
        )

        Spacer(Modifier.height(20.dp))
        FormSection("Logística")
        FormTextField(
            value = ciudad,
            onValueChange = {
                ciudad = it
                localError = null
            },
            label = "Ciudad",
            placeholder = "Ej. Bogotá",
        )
        FormGap()
        FormTextField(
            value = departamento,
            onValueChange = {
                departamento = it
                localError = null
            },
            label = "Departamento",
            placeholder = "Ej. Cundinamarca",
        )
        FormGap()
        FormTextField(
            value = cupos,
            onValueChange = {
                cupos = it
                localError = null
            },
            label = "Cupos",
            placeholder = "64",
            keyboardType = KeyboardType.Number,
        )
        FormGap()
        CampusDropdown(
            label = "Responsable",
            options = ownerOptions,
            selected = responsable.takeIf { it.isNotBlank() },
            onSelect = {
                responsable = it
                localError = null
            },
        )
        if (ownerOptions.isEmpty()) {
            FormHint("No hay administradores u organizadores activos para asignar.")
        }

        Spacer(Modifier.height(20.dp))
        FormSection(
            title = "Reglamento deportivo",
            subtitle = "El backend deriva edad y género de la categoría seleccionada.",
        )
        Row {
            FormTextField(
                value = reglaMin,
                onValueChange = {
                    reglaMin = it
                    localError = null
                },
                label = "Integrantes mín.",
                keyboardType = KeyboardType.Number,
                enabled = !individual,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(9.dp))
            FormTextField(
                value = reglaMax,
                onValueChange = {
                    reglaMax = it
                    localError = null
                },
                label = "Integrantes máx.",
                keyboardType = KeyboardType.Number,
                enabled = !individual,
                modifier = Modifier.weight(1f),
            )
        }
        if (individual) {
            FormHint("La modalidad individual fija un solo integrante.")
        }
        FormGap()
        Row {
            FormTextField(
                value = maxInscripciones,
                onValueChange = {
                    maxInscripciones = it
                    localError = null
                },
                label = "Inscripciones por competidor",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(9.dp))
            FormTextField(
                value = precision,
                onValueChange = {
                    precision = it
                    localError = null
                },
                label = "Decimales",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }
        FormGap()
        CampusDropdown(
            label = "Métrica",
            options = metricOptions,
            selected = metrica.takeIf { it.isNotBlank() },
            onSelect = {
                metrica = it
                localError = null
            },
        )
        FormGap()
        FormTextField(
            value = reglaUnidad,
            onValueChange = {
                reglaUnidad = it
                localError = null
            },
            label = "Unidad de la métrica",
            placeholder = "Ej. goles, puntos, segundos",
        )
        FormGap()
        FormHint("Orden de los resultados")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = orderOptions,
            selected = orden,
            onSelect = { orden = it ?: "descendente" },
        )
        FormGap()
        FormTextField(
            value = fases,
            onValueChange = {
                fases = it
                localError = null
            },
            label = "Fases",
            placeholder = "Fase regular, Semifinal, Final",
            singleLine = false,
        )
        FormHint("Separa cada fase con una coma.")
        FormGap()
        FormTextField(
            value = avance,
            onValueChange = {
                avance = it
                localError = null
            },
            label = "Criterio de avance",
            singleLine = false,
            minLines = 2,
        )
        FormGap()
        FormTextField(
            value = desempates,
            onValueChange = {
                desempates = it
                localError = null
            },
            label = "Desempates",
            placeholder = "Diferencia de goles, Goles a favor",
            singleLine = false,
        )
        FormHint("Separa cada desempate con una coma.")
        FormGap()
        FormTextField(
            value = pruebas,
            onValueChange = {
                pruebas = it
                localError = null
            },
            label = "Pruebas",
            placeholder = "100 metros, Salto largo",
            singleLine = false,
        )
        FormHint("Obligatorio cuando el formato es Pruebas. Sepáralas con comas.")
        FormGap()
        FormTextField(
            value = requisitos,
            onValueChange = { requisitos = it },
            label = "Requisitos",
            placeholder = "Documentos o condiciones de participación",
            singleLine = false,
            minLines = 2,
        )
        FormGap()
        FormHint("Equipos múltiples por competidor")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = listOf("false" to "No permitidos", "true" to "Permitidos"),
            selected = permiteMultiples,
            onSelect = { permiteMultiples = it ?: "false" },
        )
    }
}

/**
 * HU-09 · Alta y edición de hackatones.
 * Las categorías tecnológicas son texto libre y se envían como arreglo.
 */
@Composable
fun HackathonFormDialog(
    hackathon: HackathonDto?,
    catalogs: CatalogDto,
    currentUser: UserDto,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (HackathonInput) -> Unit,
) {
    val key = hackathon?.id ?: "new"

    val ownerOptions = catalogs.usuarios
        .filter { candidate ->
            candidate.rol in EVENT_OWNER_ROLES &&
                (candidate.estado == "activo" || candidate.id == hackathon?.responsable) &&
                (currentUser.rol == "administrador" || candidate.id == currentUser.id)
        }
        .map { it.id to "${it.nombre} (${roleLabel(it.rol)})" }

    var nombre by rememberSaveable(key) { mutableStateOf(hackathon?.nombre.orEmpty()) }
    var descripcion by rememberSaveable(key) { mutableStateOf(hackathon?.descripcion.orEmpty()) }
    var ciudad by rememberSaveable(key) { mutableStateOf(hackathon?.ciudad.orEmpty()) }
    var departamento by rememberSaveable(key) { mutableStateOf(hackathon?.departamento.orEmpty()) }
    var modalidad by rememberSaveable(key) {
        mutableStateOf(hackathon?.modalidad?.takeIf { it.isNotBlank() } ?: "presencial")
    }
    var cupos by rememberSaveable(key) {
        mutableStateOf(hackathon?.cupos?.takeIf { it > 0 }?.toString() ?: "20")
    }
    var integrantesMin by rememberSaveable(key) {
        mutableStateOf(hackathon?.integrantesMin?.takeIf { it > 0 }?.toString() ?: "1")
    }
    var integrantesMax by rememberSaveable(key) {
        mutableStateOf(hackathon?.integrantesMax?.takeIf { it > 0 }?.toString() ?: "5")
    }
    var proyectosPorEquipo by rememberSaveable(key) {
        mutableStateOf(hackathon?.proyectosPorEquipo?.takeIf { it > 0 }?.toString() ?: "1")
    }
    var categorias by rememberSaveable(key) {
        mutableStateOf(hackathon?.categorias?.joinToString(", ").orEmpty())
    }
    var requisitos by rememberSaveable(key) { mutableStateOf(hackathon?.requisitos.orEmpty()) }
    var responsable by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.responsable
                ?: currentUser.id.takeIf { id -> ownerOptions.any { it.first == id } }
                ?: ownerOptions.firstOrNull()?.first.orEmpty(),
        )
    }

    var inscripcionInicio by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.let { isoToInput(it.inscripcionInicio) }.orEmpty()
                .ifBlank { inputDate(0) },
        )
    }
    var inscripcionFin by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.let { isoToInput(it.inscripcionFin) }.orEmpty()
                .ifBlank { inputDate(7, 18) },
        )
    }
    var inicio by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.let { isoToInput(it.inicio) }.orEmpty().ifBlank { inputDate(10) },
        )
    }
    var entregaInicio by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.let { isoToInput(it.entregaInicio) }.orEmpty().ifBlank { inputDate(11) },
        )
    }
    var entregaFin by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.let { isoToInput(it.entregaFin) }.orEmpty().ifBlank { inputDate(16, 18) },
        )
    }
    var evaluacionInicio by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.let { isoToInput(it.evaluacionInicio) }.orEmpty().ifBlank { inputDate(17) },
        )
    }
    var evaluacionFin by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.let { isoToInput(it.evaluacionFin) }.orEmpty().ifBlank { inputDate(19, 18) },
        )
    }
    var fin by rememberSaveable(key) {
        mutableStateOf(
            hackathon?.let { isoToInput(it.fin) }.orEmpty().ifBlank { inputDate(20, 18) },
        )
    }
    var localError by rememberSaveable(key) { mutableStateOf<String?>(null) }

    fun submit() {
        val name = nombre.trim()
        val description = descripcion.trim()
        val city = ciudad.trim()
        val department = departamento.trim()
        val cuposValue = cupos.trim().toIntOrNull()
        val minValue = integrantesMin.trim().toIntOrNull()
        val maxValue = integrantesMax.trim().toIntOrNull()
        val projectsValue = proyectosPorEquipo.trim().toIntOrNull()
        val categories = splitValues(categorias)
        val dates = listOf(
            inscripcionInicio,
            inscripcionFin,
            inicio,
            entregaInicio,
            entregaFin,
            evaluacionInicio,
            evaluacionFin,
            fin,
        )
        val datesValid = dates.all(::isValidInputDate)

        val validation = when {
            name.length !in 2..150 -> "Escribe el nombre del hackatón (2 a 150 caracteres)."
            description.isEmpty() || description.length > 2000 ->
                "Describe el hackatón (máximo 2000 caracteres)."
            city.length !in 2..100 -> "Escribe la ciudad del hackatón."
            department.length !in 2..100 -> "Escribe el departamento del hackatón."
            responsable.isBlank() -> "Selecciona el responsable del hackatón."
            cuposValue == null || cuposValue !in 1..10000 ->
                "Los cupos deben estar entre 1 y 10000."
            minValue == null || minValue !in 1..100 ->
                "Los integrantes mínimos deben estar entre 1 y 100."
            maxValue == null || maxValue !in (minValue ?: 1)..100 ->
                "Los integrantes máximos deben ser mayores o iguales al mínimo."
            projectsValue == null || projectsValue !in 1..10 ->
                "Los proyectos por equipo deben estar entre 1 y 10."
            categories.isEmpty() -> "Agrega al menos una categoría tecnológica."
            categories.size > 30 || categories.any { it.length > 150 } || hasDuplicates(categories) ->
                "Revisa las categorías: máximo 30 valores de 150 caracteres, sin repetir."
            requisitos.trim().length > 2000 -> "Los requisitos no pueden superar 2000 caracteres."
            !datesValid -> "Completa las fechas con el formato AAAA-MM-DD HH:mm."
            else -> null
        }
        if (validation != null) {
            localError = validation
            return
        }

        val ordered = listOf(
            inscripcionInicio,
            inscripcionFin,
            inicio,
            entregaInicio,
            entregaFin,
            evaluacionInicio,
            evaluacionFin,
            fin,
        )
        if (ordered.zipWithNext().any { (first, second) -> compareInputDates(first, second) > 0 }) {
            localError = "Ordena las fechas: inscripciones, inicio, entrega, evaluación y fin."
            return
        }

        val isoDates = ordered.map(::inputToIso)
        if (isoDates.any { it == null }) {
            localError = "No pudimos interpretar una de las fechas."
            return
        }

        localError = null
        onConfirm(
            HackathonInput(
                nombre = name,
                descripcion = description,
                ciudad = city,
                departamento = department,
                modalidad = modalidad,
                inicio = isoDates[2] ?: return,
                fin = isoDates[7] ?: return,
                inscripcionInicio = isoDates[0] ?: return,
                inscripcionFin = isoDates[1] ?: return,
                entregaInicio = isoDates[3] ?: return,
                entregaFin = isoDates[4] ?: return,
                evaluacionInicio = isoDates[5] ?: return,
                evaluacionFin = isoDates[6] ?: return,
                cupos = cuposValue ?: return,
                integrantesMin = minValue ?: return,
                integrantesMax = maxValue ?: return,
                proyectosPorEquipo = projectsValue ?: return,
                categorias = categories,
                requisitos = requisitos.trim(),
                responsable = responsable,
            ),
        )
    }

    FormDialogShell(
        title = if (hackathon == null) "Nuevo hackatón" else "Editar hackatón",
        subtitle = "Fechas de entrega y evaluación, cupos y categorías tecnológicas.",
        icon = Icons.Rounded.TaskAlt,
        confirmLabel = if (hackathon == null) "Registrar" else "Guardar cambios",
        isSaving = isSaving,
        error = localError ?: error,
        onDismiss = onDismiss,
        onConfirm = ::submit,
    ) {
        if (hackathon != null && hackathon.estado !in EDITABLE_EVENT_STATES) {
            FormHint("Este hackatón está cerrado y conserva su información.")
            Spacer(Modifier.height(10.dp))
        }

        FormSection("Datos del hackatón")
        FormTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                localError = null
            },
            label = "Nombre",
            placeholder = "Ej. Hackatón Campus 2026",
        )
        FormGap()
        FormTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                localError = null
            },
            label = "Descripción",
            placeholder = "Reto, enfoque y objetivos del hackatón",
            singleLine = false,
            minLines = 3,
        )
        FormGap()
        FormTextField(
            value = ciudad,
            onValueChange = {
                ciudad = it
                localError = null
            },
            label = "Ciudad",
            placeholder = "Ej. Medellín",
        )
        FormGap()
        FormTextField(
            value = departamento,
            onValueChange = {
                departamento = it
                localError = null
            },
            label = "Departamento",
            placeholder = "Ej. Antioquia",
        )
        FormGap()
        FormHint("Modalidad")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = hackathonModalityOptions,
            selected = modalidad,
            onSelect = { modalidad = it ?: "presencial" },
        )
        FormGap()
        FormTextField(
            value = cupos,
            onValueChange = {
                cupos = it
                localError = null
            },
            label = "Cupos de equipos",
            keyboardType = KeyboardType.Number,
        )
        FormGap()
        Row {
            FormTextField(
                value = integrantesMin,
                onValueChange = {
                    integrantesMin = it
                    localError = null
                },
                label = "Integrantes mín.",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(9.dp))
            FormTextField(
                value = integrantesMax,
                onValueChange = {
                    integrantesMax = it
                    localError = null
                },
                label = "Integrantes máx.",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }
        FormGap()
        FormTextField(
            value = proyectosPorEquipo,
            onValueChange = {
                proyectosPorEquipo = it
                localError = null
            },
            label = "Proyectos por equipo",
            keyboardType = KeyboardType.Number,
        )
        FormGap()
        FormTextField(
            value = categorias,
            onValueChange = {
                categorias = it
                localError = null
            },
            label = "Categorías tecnológicas",
            placeholder = "Salud, Educación, Fintech",
            singleLine = false,
        )
        FormHint("Separa cada categoría con una coma. Deben existir antes de crear equipos.")
        FormGap()
        CampusDropdown(
            label = "Responsable",
            options = ownerOptions,
            selected = responsable.takeIf { it.isNotBlank() },
            onSelect = {
                responsable = it
                localError = null
            },
        )
        if (ownerOptions.isEmpty()) {
            FormHint("No hay administradores u organizadores activos para asignar.")
        }
        FormGap()
        FormTextField(
            value = requisitos,
            onValueChange = { requisitos = it },
            label = "Requisitos",
            placeholder = "Condiciones de participación (opcional)",
            singleLine = false,
            minLines = 2,
        )

        Spacer(Modifier.height(20.dp))
        FormSection("Fechas", "Formato local AAAA-MM-DD HH:mm; se envía en ISO 8601.")
        FormTextField(
            value = inscripcionInicio,
            onValueChange = {
                inscripcionInicio = it
                localError = null
            },
            label = "Apertura de inscripciones",
        )
        FormGap()
        FormTextField(
            value = inscripcionFin,
            onValueChange = {
                inscripcionFin = it
                localError = null
            },
            label = "Cierre de inscripciones",
        )
        FormGap()
        FormTextField(
            value = inicio,
            onValueChange = {
                inicio = it
                localError = null
            },
            label = "Inicio del hackatón",
        )
        FormGap()
        FormTextField(
            value = entregaInicio,
            onValueChange = {
                entregaInicio = it
                localError = null
            },
            label = "Inicio de entregas",
        )
        FormGap()
        FormTextField(
            value = entregaFin,
            onValueChange = {
                entregaFin = it
                localError = null
            },
            label = "Fin de entregas",
        )
        FormGap()
        FormTextField(
            value = evaluacionInicio,
            onValueChange = {
                evaluacionInicio = it
                localError = null
            },
            label = "Inicio de evaluación",
        )
        FormGap()
        FormTextField(
            value = evaluacionFin,
            onValueChange = {
                evaluacionFin = it
                localError = null
            },
            label = "Fin de evaluación",
        )
        FormGap()
        FormTextField(
            value = fin,
            onValueChange = {
                fin = it
                localError = null
            },
            label = "Fin del hackatón",
        )
    }
}

private val EVENT_OWNER_ROLES = setOf("administrador", "organizador")
private val EDITABLE_EVENT_STATES = setOf("borrador", "inscripciones", "en_curso", "entrega", "evaluacion")

private fun splitValues(value: String): List<String> =
    value.split(',', '\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

private fun hasDuplicates(values: List<String>): Boolean =
    values.map { it.lowercase() }.toSet().size != values.size
