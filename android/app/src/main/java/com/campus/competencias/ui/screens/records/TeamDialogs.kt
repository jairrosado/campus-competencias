/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/records/TeamDialogs.kt
 * Propósito: Formulario de alta y edición de equipos deportivos y tecnológicos.
 *
 * Historias de usuario:
 * - HU-11 · Gestión de equipos deportivos y tecnológicos
 * - HU-13 · Inscripción deportiva validada
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.records

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.model.CatalogDto
import com.campus.competencias.data.model.TeamDto
import com.campus.competencias.data.model.TeamInput
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.ui.components.CampusDropdown
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.FormDialogShell
import com.campus.competencias.ui.components.FormGap
import com.campus.competencias.ui.components.FormHint
import com.campus.competencias.ui.components.FormSection
import com.campus.competencias.ui.components.FormTextField
import com.campus.competencias.ui.components.MultiSelectChips
import com.campus.competencias.ui.model.activeStateOptions
import com.campus.competencias.ui.model.optionLabel
import com.campus.competencias.ui.model.participationModeLabel
import com.campus.competencias.ui.model.roleLabel
import com.campus.competencias.ui.model.teamTypeOptions

@Composable
// HU-11 · Alta y edición de equipos
fun TeamFormDialog(
    team: TeamDto?,
    catalogs: CatalogDto,
    currentUser: UserDto,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (TeamInput) -> Unit,
) {
    val key = team?.id ?: "new"
    val isParticipant = currentUser.rol == "participante"

    val ownerOptions = catalogs.usuarios
        .filter { user ->
            user.rol in OWNER_ROLES &&
                (user.estado == "activo" || user.id == team?.responsable) &&
                (!isParticipant || user.id == currentUser.id)
        }
        .map { it.id to "${it.nombre} (${roleLabel(it.rol)})" }

    val institutionOptions = catalogs.instituciones
        .filter { it.estado == "activo" || it.id == team?.institucion }
        .map { it.id to it.nombre }

    val disciplineOptions = catalogs.disciplinas
        .filter { it.estado == "activo" || it.id == team?.disciplina }
        .map { it.id to it.nombre }

    val hackathonOptions = catalogs.hackatones
        .filter { it.estado !in BLOCKED_HACKATHON_STATES || it.id == team?.hackaton }
        .map { it.id to it.nombre }

    val participantOptions = catalogs.participantes
        .filter { it.estado == "activo" || it.id in team?.integrantes.orEmpty() }
        .map {
            it.id to listOf(it.nombre, it.categoria)
                .filter { value -> value.isNotBlank() }
                .joinToString(" · ")
        }

    var tipo by rememberSaveable(key) {
        mutableStateOf(team?.tipo?.takeIf { it.isNotBlank() } ?: "deportivo")
    }
    var nombre by rememberSaveable(key) { mutableStateOf(team?.nombre.orEmpty()) }
    var responsable by rememberSaveable(key) {
        mutableStateOf(
            team?.responsable
                ?: currentUser.id.takeIf { id -> ownerOptions.any { it.first == id } }
                ?: ownerOptions.firstOrNull()?.first.orEmpty(),
        )
    }
    var estado by rememberSaveable(key) {
        mutableStateOf(team?.estado?.takeIf { it.isNotBlank() } ?: "activo")
    }
    var miembros by remember(key) {
        mutableStateOf(team?.integrantes?.toSet() ?: emptySet())
    }
    var institucion by rememberSaveable(key) {
        mutableStateOf(team?.institucion ?: institutionOptions.firstOrNull()?.first.orEmpty())
    }
    var disciplinaId by rememberSaveable(key) {
        mutableStateOf(team?.disciplina ?: disciplineOptions.firstOrNull()?.first.orEmpty())
    }
    val initialDiscipline = disciplineOptions.firstOrNull { it.first == disciplinaId }?.first
        ?.let { id -> catalogs.disciplinas.firstOrNull { it.id == id } }
    var categoria by rememberSaveable(key) {
        mutableStateOf(
            team?.categoria
                ?: initialDiscipline?.categorias?.firstOrNull()?.id.orEmpty(),
        )
    }
    var modalidad by rememberSaveable(key) {
        mutableStateOf(
            team?.modalidad
                ?: initialDiscipline?.modalidades?.firstOrNull { it != "individual" }.orEmpty(),
        )
    }
    var hackaton by rememberSaveable(key) {
        mutableStateOf(team?.hackaton ?: hackathonOptions.firstOrNull()?.first.orEmpty())
    }
    var categoriaTecnologica by rememberSaveable(key) {
        mutableStateOf(
            team?.categoriaTecnologica
                ?: catalogs.hackatones.firstOrNull { it.id == (team?.hackaton ?: hackathonOptions.firstOrNull()?.first) }
                    ?.categorias
                    ?.firstOrNull()
                    .orEmpty(),
        )
    }
    var localError by rememberSaveable(key) { mutableStateOf<String?>(null) }

    val selectedDiscipline = catalogs.disciplinas.firstOrNull { it.id == disciplinaId }
    val categoryOptions = selectedDiscipline?.categorias
        ?.map { it.id.orEmpty() to it.nombre }
        ?.filter { it.first.isNotBlank() }
        .orEmpty()
    val modalityOptions = selectedDiscipline?.modalidades
        ?.filter { it != "individual" }
        ?.map { it to participationModeLabel(it) }
        .orEmpty()
    val selectedHackathon = catalogs.hackatones.firstOrNull { it.id == hackaton }
    val technologicalCategories = selectedHackathon?.categorias.orEmpty()
    val isDeportivo = tipo == "deportivo"

    fun submit() {
        val trimmedName = nombre.trim()
        // El backend solo reevalida la institución cuando cambian la plantilla o
        // las referencias; replicamos esa regla para no bloquear datos históricos.
        val rosterChanged = team == null ||
            team.institucion != institucion ||
            team.integrantes.toSet() != miembros
        val validation = when {
            trimmedName.length !in 2..120 -> "Escribe el nombre del equipo (2 a 120 caracteres)."
            responsable.isBlank() -> "Selecciona el responsable del equipo."
            miembros.isEmpty() -> "Selecciona al menos un integrante."
            miembros.size > 100 -> "Un equipo no puede tener más de 100 integrantes."
            isDeportivo && institucion.isBlank() -> "Selecciona la institución del equipo."
            isDeportivo && disciplinaId.isBlank() -> "Selecciona la disciplina."
            isDeportivo && categoria.isBlank() -> "Selecciona la categoría."
            isDeportivo && modalidad.isBlank() -> "Selecciona la modalidad de equipo."
            isDeportivo && rosterChanged && catalogs.participantes
                .filter { it.id in miembros }
                .any { it.institucion != institucion } ->
                "Todos los integrantes deben pertenecer a la institución del equipo."
            !isDeportivo && hackaton.isBlank() -> "Selecciona el hackatón de referencia."
            !isDeportivo && categoriaTecnologica.isBlank() -> "Selecciona la categoría tecnológica."
            !isDeportivo && selectedHackathon != null &&
                (miembros.size < selectedHackathon.integrantesMin || miembros.size > selectedHackathon.integrantesMax) ->
                "El hackatón exige entre ${selectedHackathon.integrantesMin} y " +
                    "${selectedHackathon.integrantesMax} integrantes."
            else -> null
        }
        localError = validation
        if (validation == null) {
            onConfirm(
                TeamInput(
                    nombre = trimmedName,
                    tipo = tipo,
                    responsable = responsable,
                    estado = estado,
                    institucion = institucion.takeIf { isDeportivo },
                    disciplina = disciplinaId.takeIf { isDeportivo },
                    categoria = categoria.takeIf { isDeportivo },
                    modalidad = modalidad.takeIf { isDeportivo },
                    hackaton = hackaton.takeIf { !isDeportivo },
                    categoriaTecnologica = categoriaTecnologica.takeIf { !isDeportivo },
                    integrantes = miembros.toList(),
                ),
            )
        }
    }

    FormDialogShell(
        title = if (team == null) "Nuevo equipo" else "Editar equipo",
        subtitle = if (isDeportivo) {
            "Equipo deportivo con institución, disciplina y categoría."
        } else {
            "Equipo tecnológico asociado a un hackatón."
        },
        icon = Icons.Rounded.Groups,
        confirmLabel = if (team == null) "Registrar" else "Guardar cambios",
        isSaving = isSaving,
        error = localError ?: error,
        onDismiss = onDismiss,
        onConfirm = ::submit,
    ) {
        FormSection("Datos del equipo")
        FormTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                localError = null
            },
            label = "Nombre del equipo",
            placeholder = "Ej. Los Tigres",
        )
        FormGap()
        Text(
            text = "Tipo de equipo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = teamTypeOptions,
            selected = tipo,
            onSelect = { value ->
                if (team == null) {
                    tipo = value ?: "deportivo"
                    localError = null
                }
            },
        )
        if (team != null) {
            FormHint("El tipo de un equipo existente no se puede cambiar.")
        }
        FormGap()
        CampusDropdown(
            label = "Responsable",
            options = ownerOptions,
            selected = responsable.takeIf { it.isNotBlank() },
            onSelect = {
                responsable = it
                localError = null
            },
            enabled = !isParticipant,
        )
        if (isParticipant) {
            FormHint("Como participante, solo puedes gestionar tus propios equipos.")
        }
        FormGap()
        Text(
            text = "Estado",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = activeStateOptions,
            selected = estado,
            onSelect = { estado = it ?: "activo" },
        )

        Spacer(Modifier.height(20.dp))
        FormSection(
            title = "Integrantes",
            subtitle = "Selecciona los participantes que forman el equipo.",
        )
        if (participantOptions.isEmpty()) {
            Text(
                text = "No hay participantes disponibles. Regístralos primero en la sección Participantes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(8.dp))
        }
        MultiSelectChips(
            options = participantOptions,
            selected = miembros,
            onToggle = { value ->
                miembros = if (value in miembros) miembros - value else miembros + value
                localError = null
            },
        )
        FormHint("${miembros.size} integrante(s) seleccionado(s).")

        Spacer(Modifier.height(20.dp))
        if (isDeportivo) {
            FormSection(
                title = "Referencias deportivas",
                subtitle = "Institución, disciplina, categoría y modalidad.",
            )
            CampusDropdown(
                label = "Institución",
                options = institutionOptions,
                selected = institucion.takeIf { it.isNotBlank() },
                onSelect = {
                    institucion = it
                    localError = null
                },
            )
            FormGap()
            CampusDropdown(
                label = "Disciplina",
                options = disciplineOptions,
                selected = disciplinaId.takeIf { it.isNotBlank() },
                onSelect = { value ->
                    disciplinaId = value
                    val discipline = catalogs.disciplinas.firstOrNull { it.id == value }
                    categoria = discipline?.categorias?.firstOrNull()?.id.orEmpty()
                    modalidad = discipline?.modalidades?.firstOrNull { it != "individual" }.orEmpty()
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
            Text(
                text = "Modalidad de equipo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(7.dp))
            FilterChips(
                options = modalityOptions,
                selected = modalidad,
                onSelect = {
                    modalidad = it.orEmpty()
                    localError = null
                },
            )
            FormHint(
                "Modalidad actual: ${optionLabel(modalityOptions, modalidad)}.",
            )
        } else {
            FormSection(
                title = "Referencias tecnológicas",
                subtitle = "Hackatón y categoría con la que participa el equipo.",
            )
            CampusDropdown(
                label = "Hackatón",
                options = hackathonOptions,
                selected = hackaton.takeIf { it.isNotBlank() },
                onSelect = { value ->
                    hackaton = value
                    categoriaTecnologica = catalogs.hackatones
                        .firstOrNull { it.id == value }
                        ?.categorias
                        ?.firstOrNull()
                        .orEmpty()
                    localError = null
                },
            )
            FormGap()
            Text(
                text = "Categoría tecnológica",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(7.dp))
            if (technologicalCategories.isEmpty()) {
                FormHint("El hackatón seleccionado no tiene categorías registradas.")
            } else {
                FilterChips(
                    options = technologicalCategories.map { it to it },
                    selected = categoriaTecnologica,
                    onSelect = {
                        categoriaTecnologica = it.orEmpty()
                        localError = null
                    },
                )
            }
            selectedHackathon?.let { event ->
                FormHint(
                    "Plantilla exigida: ${event.integrantesMin} a ${event.integrantesMax} integrantes.",
                )
            }
        }
    }
}

private val OWNER_ROLES = setOf("administrador", "organizador", "participante")
private val BLOCKED_HACKATHON_STATES = setOf("cancelado", "publicado")
