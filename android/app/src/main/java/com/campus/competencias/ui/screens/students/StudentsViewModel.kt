/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/students/StudentsViewModel.kt
 * Propósito: Estado, búsqueda y CRUD de cuentas con rol participante (estudiantes).
 *
 * Historias de usuario:
 * - HU-02 · Gestión de cuentas de usuario
 */
package com.campus.competencias.ui.screens.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.campus.competencias.data.model.SummaryDto
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.model.UserInput
import com.campus.competencias.data.remote.ApiException
import com.campus.competencias.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface StudentsAction {
    data object Idle : StudentsAction
    data object Saving : StudentsAction
    data class Error(val message: String, val code: String? = null) : StudentsAction
}

sealed interface StudentsEvent {
    data class Saved(val message: String) : StudentsEvent
}

data class StudentsUiState(
    val items: List<UserDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 1,
    val summary: SummaryDto = SummaryDto(),
    val query: String = "",
    val status: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val action: StudentsAction = StudentsAction.Idle,
)

class StudentsViewModel(
    private val repository: CampusRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StudentsUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<StudentsEvent>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        load(page = 1)
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            load(page = 1)
        }
    }

    fun onStatusChange(value: String?) {
        if (_state.value.status == value) return
        _state.update { it.copy(status = value) }
        load(page = 1)
    }

    fun refresh() {
        load(page = _state.value.page, showRefreshIndicator = true)
    }

    fun previousPage() {
        val current = _state.value
        if (current.page > 1) load(page = current.page - 1)
    }

    fun nextPage() {
        val current = _state.value
        if (current.page < current.pages) load(page = current.page + 1)
    }

    // HU-02 · Crear o editar una cuenta de estudiante
    fun save(userId: String?, input: UserInput) {
        if (_state.value.action == StudentsAction.Saving) return
        viewModelScope.launch {
            _state.update { it.copy(action = StudentsAction.Saving) }
            try {
                if (userId == null) {
                    repository.createUser(input)
                } else {
                    repository.updateUser(userId, input)
                }
                _state.update { it.copy(action = StudentsAction.Idle) }
                _events.tryEmit(
                    StudentsEvent.Saved(
                        if (userId == null) "Estudiante registrado." else "Estudiante actualizado.",
                    ),
                )
                reloadAfterMutation()
            } catch (error: ApiException) {
                _state.update {
                    it.copy(action = StudentsAction.Error(error.message, error.code))
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        action = StudentsAction.Error(
                            error.message ?: "No se pudo guardar el estudiante.",
                        ),
                    )
                }
            }
        }
    }

    // HU-02 · Activar o desactivar sin borrar el historial
    fun changeStatus(userId: String, estado: String) {
        if (_state.value.action == StudentsAction.Saving) return
        viewModelScope.launch {
            _state.update { it.copy(action = StudentsAction.Saving) }
            try {
                repository.changeUserStatus(userId, estado)
                _state.update { it.copy(action = StudentsAction.Idle) }
                _events.tryEmit(
                    StudentsEvent.Saved(
                        if (estado == "activo") "Estudiante activado." else "Estudiante desactivado.",
                    ),
                )
                reloadAfterMutation()
            } catch (error: ApiException) {
                _state.update {
                    it.copy(action = StudentsAction.Error(error.message, error.code))
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        action = StudentsAction.Error(
                            error.message ?: "No se pudo cambiar el estado del estudiante.",
                        ),
                    )
                }
            }
        }
    }

    fun clearAction() {
        if (_state.value.action != StudentsAction.Idle) {
            _state.update { it.copy(action = StudentsAction.Idle) }
        }
    }

    private fun reloadAfterMutation() {
        val current = _state.value
        val target = if (current.items.size <= 1 && current.page > 1) current.page - 1 else current.page
        load(page = target)
    }

    private fun load(
        page: Int,
        showRefreshIndicator: Boolean = false,
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = it.items.isEmpty(),
                    isRefreshing = showRefreshIndicator,
                    error = null,
                )
            }
            try {
                val result = repository.users(
                    page = page,
                    query = _state.value.query,
                    status = _state.value.status,
                    role = "participante",
                )
                _state.update {
                    it.copy(
                        items = result.items,
                        total = result.total,
                        page = result.page.coerceAtLeast(1),
                        pages = result.pages.coerceAtLeast(1),
                        summary = result.summary,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                    )
                }
            } catch (error: ApiException) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message,
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message ?: "No pudimos cargar los estudiantes.",
                    )
                }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 320L

        fun factory(repository: CampusRepository) = viewModelFactory {
            initializer { StudentsViewModel(repository) }
        }
    }
}
