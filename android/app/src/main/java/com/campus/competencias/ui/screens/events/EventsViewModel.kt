/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/events/EventsViewModel.kt
 * Propósito: Estado, filtros, catálogos y operaciones de torneos y hackatones.
 *
 * Historias de usuario:
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.campus.competencias.data.model.CatalogDto
import com.campus.competencias.data.model.HackathonDto
import com.campus.competencias.data.model.HackathonInput
import com.campus.competencias.data.model.PageDto
import com.campus.competencias.data.model.SummaryDto
import com.campus.competencias.data.model.TournamentDto
import com.campus.competencias.data.model.TournamentInput
import com.campus.competencias.data.remote.ApiException
import com.campus.competencias.data.repository.CampusRepository
import com.campus.competencias.ui.model.EventCardData
import com.campus.competencias.ui.model.EventType
import com.campus.competencias.ui.model.toCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EventsAction {
    data object Idle : EventsAction
    data object Saving : EventsAction
    data class Error(val message: String, val code: String? = null) : EventsAction
}

sealed interface EventsEvent {
    data class Saved(val message: String) : EventsEvent
}

data class EventsUiState(
    val type: EventType = EventType.TOURNAMENTS,
    val items: List<EventCardData> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 1,
    val summary: SummaryDto = SummaryDto(),
    val query: String = "",
    val status: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    // DTO originales: permiten abrir el formulario de edición sin perder campos.
    val tournaments: List<TournamentDto> = emptyList(),
    val hackathons: List<HackathonDto> = emptyList(),
    // Catálogos que alimentan los formularios de torneo y hackatón.
    val catalogs: CatalogDto? = null,
    val isLoadingOptions: Boolean = false,
    val optionsError: String? = null,
    val action: EventsAction = EventsAction.Idle,
)

class EventsViewModel(
    private val repository: CampusRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EventsUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<EventsEvent>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        load(page = 1)
    }

    // HU-07 y HU-09 · Alternar torneos/hackatones
    fun selectType(type: EventType) {
        if (_state.value.type == type) return
        _state.update {
            it.copy(
                type = type,
                status = null,
                page = 1,
                items = emptyList(),
                total = 0,
                pages = 1,
                summary = SummaryDto(),
            )
        }
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

    // HU-07 a HU-09 · Recargar eventos
    fun refresh() {
        load(page = _state.value.page, showRefreshIndicator = true)
        loadOptions(force = true)
    }

    fun previousPage() {
        val current = _state.value
        if (current.page > 1) load(page = current.page - 1)
    }

    fun nextPage() {
        val current = _state.value
        if (current.page < current.pages) load(page = current.page + 1)
    }

    // HU-12 · Catálogos de disciplinas, cuentas y eventos
    fun loadOptions(force: Boolean = false) {
        if (!force && (_state.value.catalogs != null || _state.value.isLoadingOptions)) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingOptions = true, optionsError = null) }
            try {
                val catalogs = repository.catalogs()
                _state.update {
                    it.copy(catalogs = catalogs, isLoadingOptions = false, optionsError = null)
                }
            } catch (error: ApiException) {
                _state.update { it.copy(isLoadingOptions = false, optionsError = error.message) }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoadingOptions = false,
                        optionsError = error.message ?: "No pudimos cargar los catálogos.",
                    )
                }
            }
        }
    }

    // HU-07 y HU-08 · Crear o editar torneo
    fun saveTournament(id: String?, input: TournamentInput) {
        submit(if (id == null) "Torneo registrado." else "Torneo actualizado.") {
            if (id == null) repository.createTournament(input) else repository.updateTournament(id, input)
        }
    }

    // HU-09 · Crear o editar hackatón
    fun saveHackathon(id: String?, input: HackathonInput) {
        submit(if (id == null) "Hackatón registrado." else "Hackatón actualizado.") {
            if (id == null) repository.createHackathon(input) else repository.updateHackathon(id, input)
        }
    }

    // HU-08 · Abrir inscripciones (borrador -> inscripciones)
    fun openRegistrations(type: EventType, id: String) {
        submit(
            if (type == EventType.TOURNAMENTS) {
                "Inscripciones del torneo abiertas."
            } else {
                "Inscripciones del hackatón abiertas."
            },
        ) {
            when (type) {
                EventType.TOURNAMENTS -> repository.openTournamentRegistrations(id)
                EventType.HACKATHONS -> repository.openHackathonRegistrations(id)
            }
        }
    }

    fun clearAction() {
        if (_state.value.action != EventsAction.Idle) {
            _state.update { it.copy(action = EventsAction.Idle) }
        }
    }

    private fun submit(
        successMessage: String,
        operation: suspend () -> Unit,
    ) {
        if (_state.value.action == EventsAction.Saving) return
        viewModelScope.launch {
            _state.update { it.copy(action = EventsAction.Saving) }
            try {
                operation()
                _state.update { it.copy(action = EventsAction.Idle) }
                _events.tryEmit(EventsEvent.Saved(successMessage))
                load(page = _state.value.page)
                loadOptions(force = true)
            } catch (error: ApiException) {
                _state.update { it.copy(action = EventsAction.Error(error.message, error.code)) }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        action = EventsAction.Error(
                            error.message ?: "No se pudo completar la operación.",
                        ),
                    )
                }
            }
        }
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
            val current = _state.value
            try {
                val result: PageDto<EventCardData> = when (current.type) {
                    EventType.TOURNAMENTS -> {
                        val pageResult = repository.tournaments(page, current.query, current.status)
                        _state.update { it.copy(tournaments = pageResult.items) }
                        pageResult.toCardPage { it.toCard() }
                    }
                    EventType.HACKATHONS -> {
                        val pageResult = repository.hackathons(page, current.query, current.status)
                        _state.update { it.copy(hackathons = pageResult.items) }
                        pageResult.toCardPage { it.toCard() }
                    }
                }
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
                        error = error.message ?: "No pudimos cargar los eventos.",
                    )
                }
            }
        }
    }

    private fun <T> PageDto<T>.toCardPage(transform: (T) -> EventCardData): PageDto<EventCardData> =
        PageDto(
            items = items.map(transform),
            total = total,
            page = page,
            pages = pages,
            summary = summary,
        )

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 320L

        fun factory(repository: CampusRepository) = viewModelFactory {
            initializer { EventsViewModel(repository) }
        }
    }
}
