/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/institutions/InstitutionsScreen.kt
 * Propósito: Listado, búsqueda, filtros y acciones de instituciones.
 *
 * Historias de usuario:
 * - HU-04 · Registro y consulta de instituciones
 * - HU-05 · Edición, eliminación y desactivación de instituciones
 * - AN-02 · Consulta de instituciones en Android
 * - AN-03 · Gestión de instituciones en Android
 */
package com.campus.competencias.ui.screens.institutions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.competencias.data.model.InstitutionDto
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.repository.CampusRepository
import com.campus.competencias.ui.components.CampusOutlinedCard
import com.campus.competencias.ui.components.CampusSearchField
import com.campus.competencias.ui.components.DetailPill
import com.campus.competencias.ui.components.EmptyState
import com.campus.competencias.ui.components.ErrorBanner
import com.campus.competencias.ui.components.ErrorState
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.LoadingSkeletonCard
import com.campus.competencias.ui.components.PageHeader
import com.campus.competencias.ui.components.PaginationBar
import com.campus.competencias.ui.components.StatusBadge
import com.campus.competencias.ui.components.SummaryCards
import com.campus.competencias.ui.components.SummaryItem
import com.campus.competencias.ui.model.institutionTypeLabel
import com.campus.competencias.ui.model.statusTone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// HU-04, HU-05 y AN-02/AN-03 · Listado y acciones
fun InstitutionsScreen(
    userId: String,
    user: UserDto,
    repository: CampusRepository,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel: InstitutionsViewModel = viewModel(
        key = "institutions-$userId",
        factory = InstitutionsViewModel.factory(repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdmin = user.rol == "administrador"

    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<InstitutionDto?>(null) }
    var deleting by remember { mutableStateOf<InstitutionDto?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            showForm = false
            editing = null
            deleting = null
            viewModel.clearAction()
            when (event) {
                is InstitutionsEvent.Saved -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val action = state.action
    val isSaving = action == InstitutionAction.Saving
    val actionError = (action as? InstitutionAction.Error)
    val hasFilters = state.query.isNotBlank() || state.status != null

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
                    eyebrow = "NUESTRA COMUNIDAD",
                    title = "Instituciones",
                    description = "Las instituciones que dan vida a cada competencia.",
                    actionLabel = if (isAdmin) "Nueva" else null,
                    onAction = if (isAdmin) {
                        {
                            editing = null
                            viewModel.clearAction()
                            showForm = true
                        }
                    } else {
                        null
                    },
                )
            }

            item {
                SummaryCards(
                    items = listOf(
                        SummaryItem(
                            label = "Instituciones registradas",
                            value = if (state.isLoading) "—" else state.total.toString(),
                            caption = "Una red que sigue creciendo",
                            icon = Icons.Rounded.Business,
                        ),
                        SummaryItem(
                            label = "Instituciones activas",
                            value = if (state.isLoading) "—" else state.summary.active.toString(),
                            caption = "Disponibles para participar",
                            icon = Icons.Rounded.CheckCircle,
                            accent = MaterialTheme.colorScheme.primary,
                        ),
                        SummaryItem(
                            label = "Ciudades y municipios",
                            value = if (state.isLoading) "—" else state.summary.cities.toString(),
                            caption = "Presencia en el territorio",
                            icon = Icons.Rounded.Place,
                        ),
                    ),
                )
            }

            item {
                CampusSearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Buscar por nombre, ciudad o departamento…",
                )
            }

            item {
                FilterChips(
                    options = listOf(
                        null to "Todas",
                        "activo" to "Activas",
                        "inactivo" to "Inactivas",
                    ),
                    selected = state.status,
                    onSelect = viewModel::onStatusChange,
                )
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
                            title = "No pudimos cargar el directorio",
                            message = state.error.orEmpty(),
                            onRetry = viewModel::refresh,
                        )
                    }
                }

                state.items.isEmpty() -> {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.Business,
                            title = if (hasFilters) {
                                "No encontramos coincidencias"
                            } else {
                                "Tu comunidad comienza aquí"
                            },
                            message = if (hasFilters) {
                                "Prueba con otro nombre o cambia los filtros."
                            } else if (isAdmin) {
                                "Registra la primera institución para empezar a construir tu directorio."
                            } else {
                                "Las instituciones aparecerán aquí cuando el administrador las registre."
                            },
                            actionLabel = if (isAdmin && !hasFilters) "Registrar una institución" else null,
                            onAction = if (isAdmin && !hasFilters) {
                                {
                                    editing = null
                                    showForm = true
                                }
                            } else {
                                null
                            },
                        )
                    }
                }

                else -> {
                    items(state.items, key = { it.id }) { institution ->
                        InstitutionCard(
                            institution = institution,
                            isAdmin = isAdmin,
                            onEdit = {
                                editing = institution
                                viewModel.clearAction()
                                showForm = true
                            },
                            onDelete = {
                                deleting = institution
                                viewModel.clearAction()
                            },
                        )
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

    if (showForm) {
        InstitutionFormDialog(
            institution = editing,
            isSaving = isSaving,
            error = actionError?.message,
            onDismiss = {
                if (!isSaving) {
                    showForm = false
                    editing = null
                    viewModel.clearAction()
                }
            },
            onConfirm = { input -> viewModel.save(editing?.id, input) },
        )
    }

    deleting?.let { institution ->
        InstitutionDeleteDialog(
            institution = institution,
            isSaving = isSaving,
            error = actionError?.message,
            blocked = actionError?.code == "INSTITUTION_IN_USE",
            onDismiss = {
                if (!isSaving) {
                    deleting = null
                    viewModel.clearAction()
                }
            },
            onDelete = { viewModel.delete(institution.id) },
            onDeactivate = { viewModel.deactivate(institution.id) },
        )
    }
}

@Composable
private fun InstitutionCard(
    institution: InstitutionDto,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    CampusOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = institution.nombre.ifBlank { "Institución sin nombre" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = listOf(institution.ciudad, institution.departamento)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                                .ifBlank { "Sin ubicación" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(
                    text = if (institution.estado == "activo") "Activa" else "Inactiva",
                    tone = statusTone(institution.estado),
                )
            }

            Spacer(Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                DetailPill(institutionTypeLabel(institution.tipo))
                if (institution.ciudad.isNotBlank()) {
                    DetailPill(institution.ciudad)
                }
            }

            if (isAdmin) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Eliminar ${institution.nombre}",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
