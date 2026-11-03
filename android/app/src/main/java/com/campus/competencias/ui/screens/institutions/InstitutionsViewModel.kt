/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/institutions/InstitutionsViewModel.kt
 * Propósito: Estado, paginación y CRUD de instituciones.
 *
 * Historias de usuario:
 * - HU-04 · Registro y consulta de instituciones
 * - HU-05 · Edición, eliminación y desactivación de instituciones
 * - AN-02 · Consulta de instituciones en Android
 * - AN-03 · Gestión de instituciones en Android
 */
package com.campus.competencias.ui.screens.institutions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.campus.competencias.data.model.InstitutionDto
import com.campus.competencias.data.model.InstitutionInput
import com.campus.competencias.data.model.SummaryDto
import com.campus.competencias.data.remote.ApiException
import com.campus.competencias.data.repository.CampusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface InstitutionAction {
    data object Idle : InstitutionAction
    data object Saving : InstitutionAction
    data class Error(
        val message: String,
        val code: String? = null,
    ) : InstitutionAction
}

sealed interface InstitutionsEvent {
    data class Saved(val message: String) : InstitutionsEvent
}

data class InstitutionsUiState(
    val items: List<InstitutionDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 1,
    val summary: SummaryDto = SummaryDto(),
    val query: String = "",
    val status: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val action: InstitutionAction = InstitutionAction.Idle,
)

class InstitutionsViewModel(
    private val repository: CampusRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InstitutionsUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<InstitutionsEvent>(extraBufferCapacity = 4)
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

    // HU-04 y HU-05 · Guardar institución
    fun save(institutionId: String?, input: InstitutionInput) {
        if (_state.value.action == InstitutionAction.Saving) return
        viewModelScope.launch {
            _state.update { it.copy(action = InstitutionAction.Saving) }
            try {
                if (institutionId == null) {
                    repository.createInstitution(input)
                } else {
                    repository.updateInstitution(institutionId, input)
                }
                _state.update { it.copy(action = InstitutionAction.Idle) }
                _events.tryEmit(
                    InstitutionsEvent.Saved(
                        if (institutionId == null) "Institución registrada." else "Institución actualizada.",
                    ),
                )
                reloadAfterMutation()
            } catch (error: ApiException) {
                _state.update {
                    it.copy(action = InstitutionAction.Error(error.message, error.code))
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        action = InstitutionAction.Error(
                            error.message ?: "No se pudo guardar la institución.",
                        ),
                    )
                }
            }
        }
    }

    // HU-05 · Eliminar institución
    fun delete(institutionId: String) {
        if (_state.value.action == InstitutionAction.Saving) return
        viewModelScope.launch {
            _state.update { it.copy(action = InstitutionAction.Saving) }
            try {
                repository.deleteInstitution(institutionId)
                _state.update { it.copy(action = InstitutionAction.Idle) }
                _events.tryEmit(InstitutionsEvent.Saved("Institución eliminada."))
                reloadAfterMutation()
            } catch (error: ApiException) {
                _state.update {
                    it.copy(action = InstitutionAction.Error(error.message, error.code))
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        action = InstitutionAction.Error(
                            error.message ?: "No se pudo eliminar la institución.",
                        ),
                    )
                }
            }
        }
    }

    // HU-05 · Desactivar en lugar de borrar
    fun deactivate(institutionId: String) {
        if (_state.value.action == InstitutionAction.Saving) return
        viewModelScope.launch {
            _state.update { it.copy(action = InstitutionAction.Saving) }
            try {
                repository.deactivateInstitution(institutionId)
                _state.update { it.copy(action = InstitutionAction.Idle) }
                _events.tryEmit(InstitutionsEvent.Saved("Institución desactivada."))
                reloadAfterMutation()
            } catch (error: ApiException) {
                _state.update {
                    it.copy(action = InstitutionAction.Error(error.message, error.code))
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        action = InstitutionAction.Error(
                            error.message ?: "No se pudo desactivar la institución.",
                        ),
                    )
                }
            }
        }
    }

    fun clearAction() {
        if (_state.value.action != InstitutionAction.Idle) {
            _state.update { it.copy(action = InstitutionAction.Idle) }
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
                val result = repository.institutions(
                    page = page,
                    query = _state.value.query,
                    status = _state.value.status,
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
                        error = error.message ?: "No pudimos cargar las instituciones.",
                    )
                }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 320L

        fun factory(repository: CampusRepository) = viewModelFactory {
            initializer { InstitutionsViewModel(repository) }
        }
    }
}
