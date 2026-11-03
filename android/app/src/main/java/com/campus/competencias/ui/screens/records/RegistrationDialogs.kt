/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/records/RegistrationDialogs.kt
 * Propósito: Formulario para inscribir un participante o equipo en un torneo con
 * inscripciones abiertas.
 *
 * Historias de usuario:
 * - HU-13 · Inscripción deportiva validada
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.records

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.model.CatalogDto
import com.campus.competencias.data.model.RegistrationInput
import com.campus.competencias.data.model.TournamentDto
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.ui.components.CampusDropdown
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.FormDialogShell
import com.campus.competencias.ui.components.FormGap
import com.campus.competencias.ui.components.FormHint
import com.campus.competencias.ui.components.FormSection
import com.campus.competencias.ui.model.formatDateRange
import com.campus.competencias.ui.model.participationModeLabel
import com.campus.competencias.ui.model.statusLabel
import java.time.Instant

@Composable
// HU-13 · Inscripción de un competidor
fun RegistrationFormDialog(
    catalogs: CatalogDto,
    currentUser: UserDto,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (RegistrationInput) -> Unit,
) {
    fun manages(tournament: TournamentDto?): Boolean =
        currentUser.rol == "administrador" ||
            (currentUser.rol == "organizador" && tournament?.responsable == currentUser.id)

    fun participantOptions(tournament: TournamentDto?): List<Pair<String, String>> {
        if (tournament == null || tournament.unidadInscripcion != "individual") return emptyList()
        val managesEvent = manages(tournament)
        return catalogs.participantes
            .filter { it.estado == "activo" && (managesEvent || it.usuario == currentUser.id) }
            .map { it.id to listOf(it.nombre, it.categoria).filter { text -> text.isNotBlank() }.joinToString(" · ") }
    }

    fun teamOptions(tournament: TournamentDto?): List<Pair<String, String>> {
        if (tournament == null || tournament.unidadInscripcion == "individual") return emptyList()
        val managesEvent = manages(tournament)
        return catalogs.equipos
            .filter { team ->
                team.tipo == "deportivo" &&
                    team.estado == "activo" &&
                    team.disciplina == tournament.disciplina &&
                    team.categoria == tournament.categoria &&
                    team.modalidad == tournament.unidadInscripcion &&
                    (managesEvent || team.responsable == currentUser.id)
            }
            .map { it.id to it.nombre }
    }

    val tournamentOptions = catalogs.torneos
        .filter { it.estado == "inscripciones" && registrationOpen(it) }
        .map { it.id to it.nombre }

    var torneoId by rememberSaveable("registration") {
        mutableStateOf(tournamentOptions.firstOrNull()?.first.orEmpty())
    }
    val tournament = catalogs.torneos.firstOrNull { it.id == torneoId }
    val individual = tournament?.unidadInscripcion == "individual"
    val competitorOptions = if (individual) participantOptions(tournament) else teamOptions(tournament)
    var competidor by rememberSaveable("registration") {
        mutableStateOf(competitorOptions.firstOrNull()?.first.orEmpty())
    }
    var prueba by rememberSaveable("registration") {
        mutableStateOf(tournament?.reglamento?.pruebas?.firstOrNull().orEmpty())
    }
    var localError by rememberSaveable("registration") { mutableStateOf<String?>(null) }

    fun submit() {
        val currentTournament = catalogs.torneos.firstOrNull { it.id == torneoId }
        val validation = when {
            currentTournament == null -> "Selecciona un torneo con inscripciones abiertas."
            competidor.isBlank() -> if (currentTournament.unidadInscripcion == "individual") {
                "Selecciona el participante a inscribir."
            } else {
                "Selecciona el equipo a inscribir."
            }
            currentTournament.reglamento.pruebas.isNotEmpty() && prueba.isBlank() ->
                "Selecciona una prueba del torneo."
            else -> null
        }
        localError = validation
        if (validation == null && currentTournament != null) {
            onConfirm(
                RegistrationInput(
                    torneo = currentTournament.id,
                    modalidad = currentTournament.unidadInscripcion,
                    competidor = competidor,
                    prueba = prueba,
                ),
            )
        }
    }

    FormDialogShell(
        title = "Nueva inscripción",
        subtitle = "Inscribe un participante o equipo en un torneo abierto.",
        icon = Icons.Rounded.HowToReg,
        confirmLabel = "Inscribir",
        isSaving = isSaving,
        error = localError ?: error,
        onDismiss = onDismiss,
        onConfirm = ::submit,
    ) {
        if (tournamentOptions.isEmpty()) {
            Text(
                text = "No hay torneos con inscripciones abiertas en este momento. " +
                    "Abre inscripciones desde Eventos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(10.dp))
        }

        FormSection("Torneo")
        CampusDropdown(
            label = "Torneo",
            options = tournamentOptions,
            selected = torneoId.takeIf { it.isNotBlank() },
            placeholder = "Selecciona un torneo",
            onSelect = { value ->
                torneoId = value
                val selected = catalogs.torneos.firstOrNull { it.id == value }
                val options = if (selected?.unidadInscripcion == "individual") {
                    participantOptions(selected)
                } else {
                    teamOptions(selected)
                }
                competidor = options.firstOrNull()?.first.orEmpty()
                prueba = selected?.reglamento?.pruebas?.firstOrNull().orEmpty()
                localError = null
            },
        )

        tournament?.let { selected ->
            FormGap()
            Text(
                text = "Modalidad: ${participationModeLabel(selected.unidadInscripcion)} · " +
                    "Categoría: ${selected.categoriaNombre.ifBlank { "Sin categoría" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Inscripciones: ${formatDateRange(selected.inscripcionInicio, selected.inscripcionFin)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Cupos: ${selected.cupos} · Estado: ${statusLabel(selected.estado)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FormHint(
                "Plantilla permitida: ${selected.reglamento.integrantesMin} a " +
                    "${selected.reglamento.integrantesMax} integrantes.",
            )
        }

        Spacer(Modifier.height(18.dp))
        FormSection(if (individual) "Participante" else "Equipo")
        val label = if (individual) "Participante" else "Equipo"
        CampusDropdown(
            label = label,
            options = competitorOptions,
            selected = competidor.takeIf { it.isNotBlank() },
            placeholder = "Selecciona un competidor",
            onSelect = {
                competidor = it
                localError = null
            },
        )
        if (competitorOptions.isEmpty() && tournament != null) {
            FormHint(
                if (individual) {
                    "No hay participantes elegibles para este torneo."
                } else {
                    "No hay equipos deportivos que coincidan con disciplina, categoría y modalidad."
                },
            )
        }

        tournament?.reglamento?.pruebas?.takeIf { it.isNotEmpty() }?.let { pruebas ->
            Spacer(Modifier.height(16.dp))
            FormSection("Prueba")
            FilterChips(
                options = pruebas.map { it to it },
                selected = prueba.takeIf { it.isNotBlank() },
                onSelect = {
                    prueba = it.orEmpty()
                    localError = null
                },
            )
        }
    }
}

/**
 * El backend solo acepta inscripciones dentro de la ventana declarada por el
 * torneo; la app oculta los torneos cuya ventana ya cerró o aún no abrió.
 */
private fun registrationOpen(tournament: TournamentDto): Boolean {
    val now = Instant.now()
    val start = runCatching { Instant.parse(tournament.inscripcionInicio) }.getOrNull()
    val end = runCatching { Instant.parse(tournament.inscripcionFin) }.getOrNull()
    return (start == null || !now.isBefore(start)) && (end == null || !now.isAfter(end))
}
