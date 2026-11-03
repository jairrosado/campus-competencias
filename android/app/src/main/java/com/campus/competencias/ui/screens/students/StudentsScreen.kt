/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/students/StudentsScreen.kt
 * Propósito: Listado, búsqueda y administración de cuentas de estudiante.
 *
 * Historias de usuario:
 * - HU-02 · Gestión de cuentas de usuario
 */
package com.campus.competencias.ui.screens.students

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.repository.CampusRepository
import com.campus.competencias.ui.components.CampusOutlinedCard
import com.campus.competencias.ui.components.CampusSearchField
import com.campus.competencias.ui.components.EmptyState
import com.campus.competencias.ui.components.ErrorBanner
import com.campus.competencias.ui.components.ErrorState
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.InitialsAvatar
import com.campus.competencias.ui.components.LoadingSkeletonCard
import com.campus.competencias.ui.components.PageHeader
import com.campus.competencias.ui.components.PaginationBar
import com.campus.competencias.ui.components.StatusBadge
import com.campus.competencias.ui.components.SummaryCards
import com.campus.competencias.ui.components.SummaryItem
import com.campus.competencias.ui.model.statusLabel
import com.campus.competencias.ui.model.statusTone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// HU-02 · Administración de estudiantes desde Android
fun StudentsScreen(
    userId: String,
    repository: CampusRepository,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel: StudentsViewModel = viewModel(
        key = "students-$userId",
        factory = StudentsViewModel.factory(repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<UserDto?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            showForm = false
            editing = null
            viewModel.clearAction()
            when (event) {
                is StudentsEvent.Saved -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val action = state.action
    val isSaving = action == StudentsAction.Saving
    val actionError = (action as? StudentsAction.Error)
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
                    eyebrow = "COMUNIDAD",
                    title = "Estudiantes",
                    description = "Cuentas con rol participante que podrán inscribirse en competencias.",
                    actionLabel = "Nuevo",
                    onAction = {
                        editing = null
                        viewModel.clearAction()
                        showForm = true
                    },
                )
            }

            item {
                SummaryCards(
                    items = listOf(
                        SummaryItem(
                            label = "Estudiantes registrados",
                            value = if (state.isLoading) "—" else state.total.toString(),
                            caption = "Cuentas con rol participante",
                            icon = Icons.Rounded.School,
                        ),
                        SummaryItem(
                            label = "Cuentas activas",
                            value = if (state.isLoading) "—" else state.summary.active.toString(),
                            caption = "En toda la plataforma",
                            icon = Icons.Rounded.VerifiedUser,
                            accent = MaterialTheme.colorScheme.primary,
                        ),
                    ),
                )
            }

            item {
                CampusSearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Buscar por nombre o correo…",
                )
            }

            item {
                FilterChips(
                    options = listOf(
                        null to "Todos",
                        "activo" to "Activos",
                        "inactivo" to "Inactivos",
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
                            title = "No pudimos cargar los estudiantes",
                            message = state.error.orEmpty(),
                            onRetry = viewModel::refresh,
                        )
                    }
                }

                state.items.isEmpty() -> {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.PersonAdd,
                            title = if (hasFilters) "No encontramos coincidencias" else "Aún no hay estudiantes",
                            message = if (hasFilters) {
                                "Prueba con otro nombre o cambia los filtros."
                            } else {
                                "Crea la primera cuenta de estudiante para que pueda inscribirse."
                            },
                            actionLabel = if (!hasFilters) "Registrar estudiante" else null,
                            onAction = if (!hasFilters) {
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
                }

                else -> {
                    items(state.items, key = { it.id }) { student ->
                        StudentCard(
                            student = student,
                            isSaving = isSaving,
                            onEdit = {
                                editing = student
                                viewModel.clearAction()
                                showForm = true
                            },
                            onToggleStatus = {
                                viewModel.changeStatus(
                                    student.id,
                                    if (student.estado == "activo") "inactivo" else "activo",
                                )
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
        StudentFormDialog(
            student = editing,
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
}

@Composable
private fun StudentCard(
    student: UserDto,
    isSaving: Boolean,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
) {
    CampusOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(name = student.nombre, size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.nombre.ifBlank { "Estudiante sin nombre" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = student.correo.ifBlank { "Sin correo" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(
                    text = statusLabel(student.estado),
                    tone = statusTone(student.estado),
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onEdit, enabled = !isSaving) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Editar")
                }
                TextButton(onClick = onToggleStatus, enabled = !isSaving) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (student.estado == "activo") "Desactivar" else "Activar")
                }
            }
        }
    }
}
