/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/events/EventsScreen.kt
 * Propósito: Listado, detalle y administración de torneos y hackatones desde Android.
 *
 * Historias de usuario:
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.competencias.data.model.HackathonDto
import com.campus.competencias.data.model.TournamentDto
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.repository.CampusRepository
import com.campus.competencias.ui.components.CampusSearchField
import com.campus.competencias.ui.components.EmptyState
import com.campus.competencias.ui.components.ErrorBanner
import com.campus.competencias.ui.components.ErrorState
import com.campus.competencias.ui.components.EventCard
import com.campus.competencias.ui.components.EventDetailsContent
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.LoadingSkeletonCard
import com.campus.competencias.ui.components.PageHeader
import com.campus.competencias.ui.components.PaginationBar
import com.campus.competencias.ui.components.SummaryCards
import com.campus.competencias.ui.components.SummaryItem
import com.campus.competencias.ui.model.EventCardData
import com.campus.competencias.ui.model.EventType
import com.campus.competencias.ui.model.statusLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// HU-07, HU-08, HU-09 y AN-04 · Torneos y hackatones
fun EventsScreen(
    userId: String,
    user: UserDto,
    repository: CampusRepository,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel: EventsViewModel = viewModel(
        key = "events-$userId",
        factory = EventsViewModel.factory(repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selected by remember { mutableStateOf<EventCardData?>(null) }
    var showTournamentForm by remember { mutableStateOf(false) }
    var editingTournament by remember { mutableStateOf<TournamentDto?>(null) }
    var showHackathonForm by remember { mutableStateOf(false) }
    var editingHackathon by remember { mutableStateOf<HackathonDto?>(null) }
    var pendingOpen by remember { mutableStateOf<EventCardData?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val catalogs = state.catalogs
    val action = state.action
    val isSaving = action == EventsAction.Saving
    val actionError = (action as? EventsAction.Error)
    val canCreate = canCreateEvent(user, state.type)
    val actionReady = catalogs != null
    val hasFilters = state.query.isNotBlank() || state.status != null

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collect { event ->
            showTournamentForm = false
            showHackathonForm = false
            editingTournament = null
            editingHackathon = null
            pendingOpen = null
            selected = null
            viewModel.clearAction()
            when (event) {
                is EventsEvent.Saved -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // HU-12 · Refrescar catálogos al volver a la pantalla (nuevas disciplinas, cuentas).
    LaunchedEffect(viewModel) {
        viewModel.loadOptions(force = true)
    }

    val createAction: (() -> Unit)? = when {
        !canCreate -> null
        !actionReady -> ({ viewModel.loadOptions(force = true) })
        state.type == EventType.TOURNAMENTS -> ({
            editingTournament = null
            viewModel.clearAction()
            showTournamentForm = true
        })
        else -> ({
            editingHackathon = null
            viewModel.clearAction()
            showHackathonForm = true
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
                    eyebrow = "COMPETENCIAS",
                    title = "Eventos",
                    description = "Torneos y hackatones de la comunidad, con sus fechas, cupos y reglamentos.",
                    actionLabel = if (canCreate) {
                        if (actionReady) "Nuevo" else "Cargar datos"
                    } else {
                        null
                    },
                    onAction = createAction,
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventType.entries.forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { viewModel.selectType(type) },
                            label = {
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
            }

            item {
                SummaryCards(
                    items = listOf(
                        SummaryItem(
                            label = if (state.type == EventType.TOURNAMENTS) {
                                "Torneos registrados"
                            } else {
                                "Hackatones registrados"
                            },
                            value = if (state.isLoading) "—" else state.total.toString(),
                            caption = "En el calendario de la comunidad",
                            icon = Icons.Rounded.EmojiEvents,
                        ),
                        SummaryItem(
                            label = "Con inscripciones abiertas",
                            value = if (state.isLoading) "—" else state.summary.active.toString(),
                            caption = "Listos para recibir competidores",
                            icon = Icons.Rounded.TaskAlt,
                            accent = MaterialTheme.colorScheme.primary,
                        ),
                        SummaryItem(
                            label = "Eventos en total",
                            value = if (state.isLoading) "—" else state.summary.total.toString(),
                            caption = "Incluye todos los estados",
                            icon = Icons.Rounded.CalendarMonth,
                        ),
                    ),
                )
            }

            item {
                CampusSearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Buscar por nombre, ciudad o responsable…",
                )
            }

            item {
                FilterChips(
                    options = statusOptions(state.type),
                    selected = state.status,
                    onSelect = viewModel::onStatusChange,
                )
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
                            title = "No pudimos cargar los eventos",
                            message = state.error.orEmpty(),
                            onRetry = viewModel::refresh,
                        )
                    }
                }

                state.items.isEmpty() -> {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.CalendarMonth,
                            title = if (hasFilters) {
                                "No encontramos coincidencias"
                            } else {
                                "Todavía no hay ${state.type.label.lowercase()}"
                            },
                            message = if (hasFilters) {
                                "Prueba con otro nombre o cambia los filtros."
                            } else {
                                "Cuando se registren ${state.type.label.lowercase()}, aparecerán aquí."
                            },
                            actionLabel = if (canCreate && actionReady && !hasFilters) "Crear ahora" else null,
                            onAction = if (canCreate && actionReady && !hasFilters) createAction else null,
                        )
                    }
                }

                else -> {
                    items(state.items, key = { it.id }) { event ->
                        EventCard(event = event, onClick = { selected = event })
                    }
                    item {
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

    selected?.let { event ->
        val rawTournament = state.tournaments.firstOrNull { it.id == event.id }
        val rawHackathon = state.hackathons.firstOrNull { it.id == event.id }
        val responsableId = rawTournament?.responsable
            ?: rawHackathon?.responsable
            ?: ""
        val estado = rawTournament?.estado
            ?: rawHackathon?.estado
            ?: event.status
        val manages = canManageEvent(user, responsableId)
        val editable = manages && estado in EDITABLE_EVENT_STATES && catalogs != null

        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp),
            ) {
                EventDetailsContent(event = event)

                if (manages) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (editable) {
                            TextButton(
                                onClick = {
                                    selected = null
                                    viewModel.clearAction()
                                    if (event.type == EventType.TOURNAMENTS) {
                                        editingTournament = rawTournament
                                        showTournamentForm = true
                                    } else {
                                        editingHackathon = rawHackathon
                                        showHackathonForm = true
                                    }
                                },
                                enabled = !isSaving,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Editar")
                            }
                        }
                        if (estado == "borrador") {
                            TextButton(
                                onClick = {
                                    selected = null
                                    viewModel.clearAction()
                                    pendingOpen = event
                                },
                                enabled = !isSaving,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.HowToReg,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Abrir inscripciones")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    pendingOpen?.let { event ->
        AlertDialog(
            onDismissRequest = {
                if (!isSaving) {
                    pendingOpen = null
                    viewModel.clearAction()
                }
            },
            title = { Text("Abrir inscripciones") },
            text = {
                Column {
                    Text(
                        "El evento \"${event.name}\" pasará de borrador a inscripciones. " +
                            "El reglamento y el formato quedarán bloqueados por el backend.",
                    )
                    actionError?.message?.let { message ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.openRegistrations(event.type, event.id) },
                    enabled = !isSaving,
                ) {
                    Text("Abrir inscripciones")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingOpen = null
                        viewModel.clearAction()
                    },
                    enabled = !isSaving,
                ) {
                    Text("Cancelar")
                }
            },
        )
    }

    if (showTournamentForm && catalogs != null) {
        TournamentFormDialog(
            tournament = editingTournament,
            catalogs = catalogs,
            currentUser = user,
            isSaving = isSaving,
            error = actionError?.message,
            onDismiss = {
                if (!isSaving) {
                    showTournamentForm = false
                    editingTournament = null
                    viewModel.clearAction()
                }
            },
            onConfirm = { input -> viewModel.saveTournament(editingTournament?.id, input) },
        )
    }

    if (showHackathonForm && catalogs != null) {
        HackathonFormDialog(
            hackathon = editingHackathon,
            catalogs = catalogs,
            currentUser = user,
            isSaving = isSaving,
            error = actionError?.message,
            onDismiss = {
                if (!isSaving) {
                    showHackathonForm = false
                    editingHackathon = null
                    viewModel.clearAction()
                }
            },
            onConfirm = { input -> viewModel.saveHackathon(editingHackathon?.id, input) },
        )
    }
}

private val EDITABLE_EVENT_STATES =
    setOf("borrador", "inscripciones", "en_curso", "entrega", "evaluacion")

// HU-07 y HU-09 · El administrador crea cualquier evento; el organizador solo torneos.
private fun canCreateEvent(user: UserDto, type: EventType): Boolean = when (type) {
    EventType.TOURNAMENTS -> user.rol == "administrador" || user.rol == "organizador"
    EventType.HACKATHONS -> user.rol == "administrador"
}

// HU-08 y HU-09 · El responsable del evento o cualquier administrador lo gestiona.
private fun canManageEvent(user: UserDto, responsableId: String): Boolean =
    user.rol == "administrador" ||
        (user.rol == "organizador" && responsableId.isNotBlank() && responsableId == user.id)

private fun statusOptions(type: EventType): List<Pair<String?, String>> = when (type) {
    EventType.TOURNAMENTS -> listOf(
        null to "Todos",
        "borrador" to statusLabel("borrador"),
        "inscripciones" to statusLabel("inscripciones"),
        "en_curso" to statusLabel("en_curso"),
        "finalizado" to statusLabel("finalizado"),
        "cancelado" to statusLabel("cancelado"),
    )

    EventType.HACKATHONS -> listOf(
        null to "Todos",
        "borrador" to statusLabel("borrador"),
        "inscripciones" to statusLabel("inscripciones"),
        "entrega" to statusLabel("entrega"),
        "evaluacion" to statusLabel("evaluacion"),
        "publicado" to statusLabel("publicado"),
        "cancelado" to statusLabel("cancelado"),
    )
}
