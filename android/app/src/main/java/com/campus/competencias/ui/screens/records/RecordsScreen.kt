/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/records/RecordsScreen.kt
 * Propósito: Listados y formularios de disciplinas, participantes, equipos e inscripciones.
 *
 * Historias de usuario:
 * - HU-06 · Gestión de disciplinas deportivas
 * - HU-10 · Registro de participantes
 * - HU-11 · Gestión de equipos deportivos y tecnológicos
 * - HU-13 · Inscripción deportiva validada
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.competencias.data.model.DisciplineDto
import com.campus.competencias.data.model.ParticipantDto
import com.campus.competencias.data.model.TeamDto
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.repository.CampusRepository
import com.campus.competencias.ui.components.CampusSearchField
import com.campus.competencias.ui.components.EmptyState
import com.campus.competencias.ui.components.ErrorBanner
import com.campus.competencias.ui.components.ErrorState
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.LoadingSkeletonCard
import com.campus.competencias.ui.components.PageHeader
import com.campus.competencias.ui.components.PaginationBar
import com.campus.competencias.ui.components.RecordCard
import com.campus.competencias.ui.components.SummaryCards
import com.campus.competencias.ui.components.SummaryItem
import com.campus.competencias.ui.model.RecordKind
import com.campus.competencias.ui.model.statusLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// HU-06, HU-10, HU-11, HU-13 y AN-04 · Registros de competencia
fun RecordsScreen(
    kind: RecordKind,
    userId: String,
    user: UserDto,
    repository: CampusRepository,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel: RecordsViewModel = viewModel(
        key = "records-${kind.name}-$userId",
        factory = RecordsViewModel.factory(kind, repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showDisciplineForm by remember { mutableStateOf(false) }
    var editingDiscipline by remember { mutableStateOf<DisciplineDto?>(null) }
    var showParticipantForm by remember { mutableStateOf(false) }
    var editingParticipant by remember { mutableStateOf<ParticipantDto?>(null) }
    var showTeamForm by remember { mutableStateOf(false) }
    var editingTeam by remember { mutableStateOf<TeamDto?>(null) }
    var showRegistrationForm by remember { mutableStateOf(false) }

    val icon = kind.icon()
    val catalogs = state.catalogs
    val action = state.action
    val isSaving = action == RecordsAction.Saving
    val actionError = (action as? RecordsAction.Error)
    val canCreate = canCreateRecord(user, kind)
    val actionReady = kind == RecordKind.DISCIPLINES || catalogs != null
    val hasFilters = state.query.isNotBlank() || state.status != null

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collect { event ->
            showDisciplineForm = false
            showParticipantForm = false
            showTeamForm = false
            showRegistrationForm = false
            editingDiscipline = null
            editingParticipant = null
            editingTeam = null
            viewModel.clearAction()
            when (event) {
                is RecordsEvent.Saved -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // HU-12 · Refrescar catálogos al volver a la pantalla (nuevos estudiantes, eventos).
    LaunchedEffect(viewModel, kind) {
        if (kind != RecordKind.DISCIPLINES) viewModel.loadOptions(force = true)
    }

    val createAction: (() -> Unit)? = when {
        !canCreate -> null
        !actionReady -> ({ viewModel.loadOptions(force = true) })
        else -> ({
            viewModel.clearAction()
            when (kind) {
                RecordKind.DISCIPLINES -> {
                    editingDiscipline = null
                    showDisciplineForm = true
                }
                RecordKind.PARTICIPANTS -> {
                    editingParticipant = null
                    showParticipantForm = true
                }
                RecordKind.TEAMS -> {
                    editingTeam = null
                    showTeamForm = true
                }
                RecordKind.REGISTRATIONS -> showRegistrationForm = true
            }
        })
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    eyebrow = kind.eyebrow,
                    title = kind.title,
                    description = kind.description,
                    actionLabel = if (canCreate) {
                        if (actionReady) "Nuevo" else "Cargar datos"
                    } else {
                        null
                    },
                    onAction = createAction,
                )
            }

            item {
                SummaryCards(
                    items = listOf(
                        SummaryItem(
                            label = "Total de ${kind.title.lowercase()}",
                            value = if (state.isLoading) "—" else state.total.toString(),
                            caption = "Información sincronizada con la API",
                            icon = icon,
                        ),
                        SummaryItem(
                            label = if (kind == RecordKind.REGISTRATIONS) "Confirmadas" else "Activos",
                            value = if (state.isLoading) {
                                "—"
                            } else if (kind == RecordKind.REGISTRATIONS) {
                                state.total.toString()
                            } else {
                                state.summary.active.toString()
                            },
                            caption = if (kind == RecordKind.REGISTRATIONS) {
                                "Participación verificada"
                            } else {
                                "Disponibles para competir"
                            },
                            icon = Icons.Rounded.CheckCircle,
                            accent = MaterialTheme.colorScheme.primary,
                        ),
                    ),
                )
            }

            if (kind.supportsSearch) {
                item {
                    CampusSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "Buscar en ${kind.title.lowercase()}…",
                    )
                }
            }

            if (kind.supportsStatus) {
                item {
                    FilterChips(
                        options = listOf(
                            null to "Todos",
                            "activo" to statusLabel("activo"),
                            "inactivo" to statusLabel("inactivo"),
                        ),
                        selected = state.status,
                        onSelect = viewModel::onStatusChange,
                    )
                }
            }

            if (state.optionsError != null && canCreate) {
                item {
                    ErrorBanner(
                        message = "Catálogos: ${state.optionsError.orEmpty()}",
                        onRetry = { viewModel.loadOptions(force = true) },
                    )
                }
            }

            if (state.error != null && state.items.isNotEmpty()) {
                item {
                    ErrorBanner(
                        message = state.error.orEmpty(),
                        onRetry = viewModel::refresh,
                    )
                }
            }

            when {
                state.isLoading && state.items.isEmpty() -> {
                    items(3) { LoadingSkeletonCard() }
                }

                state.items.isEmpty() && state.error != null -> {
                    item {
                        ErrorState(
                            message = state.error.orEmpty(),
                            onRetry = viewModel::refresh,
                        )
                    }
                }

                state.items.isEmpty() -> {
                    item {
                        EmptyState(
                            icon = icon,
                            title = if (hasFilters) {
                                "No encontramos coincidencias"
                            } else {
                                "Todavía no hay ${kind.title.lowercase()}"
                            },
                            message = if (hasFilters) {
                                "Prueba con otra búsqueda o cambia los filtros."
                            } else {
                                "La información aparecerá aquí cuando esté registrada."
                            },
                            actionLabel = if (canCreate && actionReady && !hasFilters) "Crear ahora" else null,
                            onAction = if (canCreate && actionReady && !hasFilters) createAction else null,
                        )
                    }
                }

                else -> {
                    items(state.items, key = { it.id }) { record ->
                        val participant = if (kind == RecordKind.PARTICIPANTS) {
                            state.participantItems.firstOrNull { it.id == record.id }
                        } else {
                            null
                        }
                        val team = if (kind == RecordKind.TEAMS) {
                            state.teamItems.firstOrNull { it.id == record.id }
                        } else {
                            null
                        }
                        val editable = canEditRecord(user, kind, participant, team) &&
                            (kind == RecordKind.DISCIPLINES || catalogs != null)
                        val openEditor: (() -> Unit)? = if (editable) {
                            {
                                viewModel.clearAction()
                                when (kind) {
                                    RecordKind.DISCIPLINES -> {
                                        val item = state.disciplines.firstOrNull { it.id == record.id }
                                        if (item != null) {
                                            editingDiscipline = item
                                            showDisciplineForm = true
                                        }
                                    }
                                    RecordKind.PARTICIPANTS -> {
                                        val item = state.participantItems
                                            .firstOrNull { it.id == record.id }
                                        if (item != null) {
                                            editingParticipant = item
                                            showParticipantForm = true
                                        }
                                    }
                                    RecordKind.TEAMS -> {
                                        val item = state.teamItems.firstOrNull { it.id == record.id }
                                        if (item != null) {
                                            editingTeam = item
                                            showTeamForm = true
                                        }
                                    }
                                    RecordKind.REGISTRATIONS -> Unit
                                }
                            }
                        } else {
                            null
                        }
                        RecordCard(
                            record = record,
                            icon = icon,
                            onClick = openEditor,
                        )
                    }
                    item {
                        Spacer(Modifier.height(2.dp))
                        PaginationBar(
                            page = state.page,
                            pages = state.pages,
                            total = state.total,
                            onPrevious = viewModel::previousPage,
                            onNext = viewModel::nextPage,
                            enabled = !isSaving,
                        )
                    }
                }
            }
        }
    }

    if (showDisciplineForm) {
        DisciplineFormDialog(
            discipline = editingDiscipline,
            isSaving = isSaving,
            error = actionError?.message,
            onDismiss = {
                if (!isSaving) {
                    showDisciplineForm = false
                    editingDiscipline = null
                    viewModel.clearAction()
                }
            },
            onConfirm = { input -> viewModel.saveDiscipline(editingDiscipline?.id, input) },
        )
    }

    if (showParticipantForm && catalogs != null) {
        ParticipantFormDialog(
            participant = editingParticipant,
            catalogs = catalogs,
            isSaving = isSaving,
            error = actionError?.message,
            onDismiss = {
                if (!isSaving) {
                    showParticipantForm = false
                    editingParticipant = null
                    viewModel.clearAction()
                }
            },
            onConfirm = { input -> viewModel.saveParticipant(editingParticipant?.id, input) },
        )
    }

    if (showTeamForm && catalogs != null) {
        TeamFormDialog(
            team = editingTeam,
            catalogs = catalogs,
            currentUser = user,
            isSaving = isSaving,
            error = actionError?.message,
            onDismiss = {
                if (!isSaving) {
                    showTeamForm = false
                    editingTeam = null
                    viewModel.clearAction()
                }
            },
            onConfirm = { input -> viewModel.saveTeam(editingTeam?.id, input) },
        )
    }

    if (showRegistrationForm && catalogs != null) {
        RegistrationFormDialog(
            catalogs = catalogs,
            currentUser = user,
            isSaving = isSaving,
            error = actionError?.message,
            onDismiss = {
                if (!isSaving) {
                    showRegistrationForm = false
                    viewModel.clearAction()
                }
            },
            onConfirm = { input -> viewModel.saveRegistration(input) },
        )
    }
}

private fun RecordKind.icon(): ImageVector = when (this) {
    RecordKind.DISCIPLINES -> Icons.Rounded.MilitaryTech
    RecordKind.PARTICIPANTS -> Icons.Rounded.Person
    RecordKind.TEAMS -> Icons.Rounded.Groups
    RecordKind.REGISTRATIONS -> Icons.Rounded.HowToReg
}

// HU-06, HU-10, HU-11 y HU-13 · Creación permitida por rol
private fun canCreateRecord(user: UserDto, kind: RecordKind): Boolean = when (kind) {
    RecordKind.DISCIPLINES -> user.rol == "administrador"
    RecordKind.PARTICIPANTS -> user.rol == "administrador" || user.rol == "organizador"
    RecordKind.TEAMS, RecordKind.REGISTRATIONS ->
        user.rol == "administrador" || user.rol == "organizador" || user.rol == "participante"
}

// HU-06, HU-10 y HU-11 · Edición permitida por rol y propiedad del registro
private fun canEditRecord(
    user: UserDto,
    kind: RecordKind,
    participant: ParticipantDto?,
    team: TeamDto?,
): Boolean = when (kind) {
    RecordKind.DISCIPLINES -> user.rol == "administrador"
    RecordKind.PARTICIPANTS -> when (user.rol) {
        "administrador" -> true
        "organizador" -> participant?.creadoPor == user.id
        else -> false
    }
    RecordKind.TEAMS -> when (user.rol) {
        "administrador" -> true
        "organizador" -> team != null &&
            (team.creadoPor == user.id || team.responsable == user.id)
        "participante" -> team?.responsable == user.id
        else -> false
    }
    RecordKind.REGISTRATIONS -> false
}
