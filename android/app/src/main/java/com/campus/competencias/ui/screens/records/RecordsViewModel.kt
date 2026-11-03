/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/records/RecordsViewModel.kt
 * Propósito: Estado, filtros, catálogos y operaciones de disciplinas, participantes,
 * equipos e inscripciones.
 *
 * Historias de usuario:
 * - HU-06 · Gestión de disciplinas deportivas
 * - HU-10 · Registro de participantes
 * - HU-11 · Gestión de equipos deportivos y tecnológicos
 * - HU-13 · Inscripción deportiva validada
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.campus.competencias.data.model.CatalogDto
import com.campus.competencias.data.model.DisciplineDto
import com.campus.competencias.data.model.DisciplineInput
import com.campus.competencias.data.model.PageDto
import com.campus.competencias.data.model.ParticipantDto
import com.campus.competencias.data.model.ParticipantInput
import com.campus.competencias.data.model.RegistrationDto
import com.campus.competencias.data.model.RegistrationInput
import com.campus.competencias.data.model.SummaryDto
import com.campus.competencias.data.model.TeamDto
import com.campus.competencias.data.model.TeamInput
import com.campus.competencias.data.remote.ApiException
import com.campus.competencias.data.repository.CampusRepository
import com.campus.competencias.ui.model.RecordCardData
import com.campus.competencias.ui.model.RecordKind
import com.campus.competencias.ui.model.toRecordCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RecordsAction {
    data object Idle : RecordsAction
    data object Saving : RecordsAction
    data class Error(val message: String, val code: String? = null) : RecordsAction
}

sealed interface RecordsEvent {
    data class Saved(val message: String) : RecordsEvent
}

data class RecordsUiState(
    val items: List<RecordCardData> = emptyList(),
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
    val disciplines: List<DisciplineDto> = emptyList(),
    val participantItems: List<ParticipantDto> = emptyList(),
    val teamItems: List<TeamDto> = emptyList(),
    val registrationItems: List<RegistrationDto> = emptyList(),
    // Catálogos de apoyo para los formularios (usuarios, instituciones, eventos).
    val catalogs: CatalogDto? = null,
    val isLoadingOptions: Boolean = false,
    val optionsError: String? = null,
    val action: RecordsAction = RecordsAction.Idle,
)

class RecordsViewModel(
    val kind: RecordKind,
    private val repository: CampusRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecordsUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<RecordsEvent>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        load(page = 1)
    }

    fun onQueryChange(value: String) {
        if (!kind.supportsSearch) return
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            load(page = 1)
        }
    }

    fun onStatusChange(value: String?) {
        if (!kind.supportsStatus) return
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

    // HU-06 a HU-13 · Catálogos de apoyo para los formularios
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

    // HU-06 · Crear o editar disciplina
    fun saveDiscipline(id: String?, input: DisciplineInput) {
        submit(if (id == null) "Disciplina registrada." else "Disciplina actualizada.") {
            if (id == null) repository.createDiscipline(input) else repository.updateDiscipline(id, input)
        }
    }

    // HU-10 · Crear o editar participante
    fun saveParticipant(id: String?, input: ParticipantInput) {
        submit(if (id == null) "Participante registrado." else "Participante actualizado.") {
            if (id == null) repository.createParticipant(input) else repository.updateParticipant(id, input)
        }
    }

    // HU-11 · Crear o editar equipo
    fun saveTeam(id: String?, input: TeamInput) {
        submit(if (id == null) "Equipo registrado." else "Equipo actualizado.") {
            if (id == null) repository.createTeam(input) else repository.updateTeam(id, input)
        }
    }

    // HU-13 · Registrar inscripción validada
    fun saveRegistration(input: RegistrationInput) {
        submit("Inscripción registrada.") { repository.createRegistration(input) }
    }

    fun clearAction() {
        if (_state.value.action != RecordsAction.Idle) {
            _state.update { it.copy(action = RecordsAction.Idle) }
        }
    }

    private fun submit(
        successMessage: String,
        operation: suspend () -> Unit,
    ) {
        if (_state.value.action == RecordsAction.Saving) return
        viewModelScope.launch {
            _state.update { it.copy(action = RecordsAction.Saving) }
            try {
                operation()
                _state.update { it.copy(action = RecordsAction.Idle) }
                _events.tryEmit(RecordsEvent.Saved(successMessage))
                load(page = _state.value.page)
                if (kind != RecordKind.DISCIPLINES) loadOptions(force = true)
            } catch (error: ApiException) {
                _state.update { it.copy(action = RecordsAction.Error(error.message, error.code)) }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        action = RecordsAction.Error(
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
                val result: PageDto<RecordCardData> = when (kind) {
                    RecordKind.DISCIPLINES -> {
                        val pageResult = repository.disciplines(page, current.query, current.status)
                        _state.update { it.copy(disciplines = pageResult.items) }
                        pageResult.toCardPage { it.toRecordCard() }
                    }
                    RecordKind.PARTICIPANTS -> {
                        val pageResult = repository.participants(page, current.query, current.status)
                        _state.update { it.copy(participantItems = pageResult.items) }
                        pageResult.toCardPage { it.toRecordCard() }
                    }
                    RecordKind.TEAMS -> {
                        val pageResult = repository.teams(page, current.query, current.status)
                        _state.update { it.copy(teamItems = pageResult.items) }
                        pageResult.toCardPage { it.toRecordCard() }
                    }
                    RecordKind.REGISTRATIONS -> {
                        val pageResult = repository.registrations(page)
                        _state.update { it.copy(registrationItems = pageResult.items) }
                        pageResult.toCardPage { it.toRecordCard() }
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
                        error = error.message ?: "No pudimos cargar la información.",
                    )
                }
            }
        }
    }

    private fun <T> PageDto<T>.toCardPage(transform: (T) -> RecordCardData): PageDto<RecordCardData> =
        PageDto(
            items = items.map(transform),
            total = total,
            page = page,
            pages = pages,
            summary = summary,
        )

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 320L

        fun factory(
            kind: RecordKind,
            repository: CampusRepository,
        ) = viewModelFactory {
            initializer { RecordsViewModel(kind, repository) }
        }
    }
}
