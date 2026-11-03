/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/repository/CampusRepository.kt
 * Propósito: Repositorio único de instituciones, catálogos, eventos, registros e inscripciones.
 *
 * Historias de usuario:
 * - HU-04 · Registro y consulta de instituciones
 * - HU-05 · Edición, eliminación y desactivación de instituciones
 * - HU-06 · Gestión de disciplinas deportivas
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - HU-10 · Registro de participantes
 * - HU-11 · Gestión de equipos deportivos y tecnológicos
 * - HU-12 · Catálogo de apoyo y navegación por rol
 * - HU-13 · Inscripción deportiva validada
 * - AN-02 · Consulta de instituciones en Android
 * - AN-03 · Gestión de instituciones en Android
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.data.repository

import com.campus.competencias.data.model.CatalogDto
import com.campus.competencias.data.model.DeletedDto
import com.campus.competencias.data.model.DisciplineDto
import com.campus.competencias.data.model.DisciplineInput
import com.campus.competencias.data.model.EventStateInput
import com.campus.competencias.data.model.HackathonDto
import com.campus.competencias.data.model.HackathonInput
import com.campus.competencias.data.model.InstitutionDto
import com.campus.competencias.data.model.InstitutionInput
import com.campus.competencias.data.model.InstitutionPatch
import com.campus.competencias.data.model.PageDto
import com.campus.competencias.data.model.ParticipantDto
import com.campus.competencias.data.model.ParticipantInput
import com.campus.competencias.data.model.RegistrationDto
import com.campus.competencias.data.model.RegistrationInput
import com.campus.competencias.data.model.TeamDto
import com.campus.competencias.data.model.TeamInput
import com.campus.competencias.data.model.TournamentDto
import com.campus.competencias.data.model.TournamentInput
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.model.UserInput
import com.campus.competencias.data.model.UserStatusPatch
import com.campus.competencias.data.remote.ApiExecutor
import com.campus.competencias.data.remote.CampusApi

/**
 * Única puerta de entrada a los recursos existentes. La UI nunca conoce Retrofit
 * ni construye URLs: solo pide datos ya tipados.
 */
class CampusRepository(
    private val api: CampusApi,
    private val executor: ApiExecutor,
) {

    // HU-04 y AN-02 · Instituciones
    suspend fun institutions(
        page: Int,
        query: String,
        status: String?,
    ): PageDto<InstitutionDto> = executor.execute {
        api.institutions(page, PAGE_SIZE, query.blankToNull(), status.blankToNull())
    }.data

    suspend fun createInstitution(input: InstitutionInput): InstitutionDto =
        executor.execute { api.createInstitution(input) }.data

    suspend fun updateInstitution(id: String, input: InstitutionInput): InstitutionDto =
        executor.execute { api.updateInstitution(id, input) }.data

    suspend fun deactivateInstitution(id: String): InstitutionDto =
        executor.execute { api.patchInstitution(id, InstitutionPatch(estado = "inactivo")) }.data

    suspend fun deleteInstitution(id: String): DeletedDto =
        executor.execute { api.deleteInstitution(id) }.data

    // HU-02 · Cuentas de usuario
    suspend fun users(
        page: Int,
        query: String,
        status: String?,
        role: String = "participante",
    ): PageDto<UserDto> = executor.execute {
        api.users(page, PAGE_SIZE, query.blankToNull(), role.blankToNull(), status.blankToNull())
    }.data

    suspend fun createUser(input: UserInput): UserDto =
        executor.execute { api.createUser(input) }.data

    suspend fun updateUser(id: String, input: UserInput): UserDto =
        executor.execute { api.updateUser(id, input) }.data

    suspend fun changeUserStatus(id: String, estado: String): UserDto =
        executor.execute { api.patchUserStatus(id, UserStatusPatch(estado = estado)) }.data

    // HU-07 · Torneos
    suspend fun tournaments(
        page: Int,
        query: String,
        status: String?,
    ): PageDto<TournamentDto> = executor.execute {
        api.tournaments(page, PAGE_SIZE, query.blankToNull(), status.blankToNull())
    }.data

    // HU-09 · Hackatones
    suspend fun hackathons(
        page: Int,
        query: String,
        status: String?,
    ): PageDto<HackathonDto> = executor.execute {
        api.hackathons(page, PAGE_SIZE, query.blankToNull(), status.blankToNull())
    }.data

    // HU-06 · Disciplinas
    suspend fun disciplines(
        page: Int,
        query: String,
        status: String?,
    ): PageDto<DisciplineDto> = executor.execute {
        api.disciplines(page, PAGE_SIZE, query.blankToNull(), status.blankToNull())
    }.data

    // HU-10 · Participantes
    suspend fun participants(
        page: Int,
        query: String,
        status: String?,
    ): PageDto<ParticipantDto> = executor.execute {
        api.participants(page, PAGE_SIZE, query.blankToNull(), status.blankToNull())
    }.data

    // HU-11 · Equipos
    suspend fun teams(
        page: Int,
        query: String,
        status: String?,
    ): PageDto<TeamDto> = executor.execute {
        api.teams(page, PAGE_SIZE, query.blankToNull(), status.blankToNull())
    }.data

    // HU-13 · Inscripciones
    suspend fun registrations(
        page: Int,
        tournamentId: String? = null,
    ): PageDto<RegistrationDto> = executor.execute {
        api.registrations(page, PAGE_SIZE, tournamentId.blankToNull())
    }.data

    // HU-06 · Disciplinas
    suspend fun createDiscipline(input: DisciplineInput): DisciplineDto =
        executor.execute { api.createDiscipline(input) }.data

    suspend fun updateDiscipline(id: String, input: DisciplineInput): DisciplineDto =
        executor.execute { api.updateDiscipline(id, input) }.data

    // HU-07 y HU-08 · Torneos
    suspend fun createTournament(input: TournamentInput): TournamentDto =
        executor.execute { api.createTournament(input) }.data

    suspend fun updateTournament(id: String, input: TournamentInput): TournamentDto =
        executor.execute { api.updateTournament(id, input) }.data

    suspend fun openTournamentRegistrations(id: String): TournamentDto =
        executor.execute {
            api.openTournamentRegistrations(id, EventStateInput(estado = "inscripciones"))
        }.data

    // HU-09 · Hackatones
    suspend fun createHackathon(input: HackathonInput): HackathonDto =
        executor.execute { api.createHackathon(input) }.data

    suspend fun updateHackathon(id: String, input: HackathonInput): HackathonDto =
        executor.execute { api.updateHackathon(id, input) }.data

    suspend fun openHackathonRegistrations(id: String): HackathonDto =
        executor.execute {
            api.openHackathonRegistrations(id, EventStateInput(estado = "inscripciones"))
        }.data

    // HU-10 · Participantes
    suspend fun createParticipant(input: ParticipantInput): ParticipantDto =
        executor.execute { api.createParticipant(input) }.data

    suspend fun updateParticipant(id: String, input: ParticipantInput): ParticipantDto =
        executor.execute { api.updateParticipant(id, input) }.data

    // HU-11 · Equipos
    suspend fun createTeam(input: TeamInput): TeamDto =
        executor.execute { api.createTeam(input) }.data

    suspend fun updateTeam(id: String, input: TeamInput): TeamDto =
        executor.execute { api.updateTeam(id, input) }.data

    // HU-13 · Inscripciones
    suspend fun createRegistration(input: RegistrationInput): RegistrationDto =
        executor.execute { api.createRegistration(input) }.data

    // HU-12 · Catálogos
    suspend fun catalogs(): CatalogDto =
        executor.execute { api.catalogs() }.data

    private fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private companion object {
        // La API acepta limit entre 1 y 50. Diez tarjetas mantienen la lista fluida.
        const val PAGE_SIZE = 10
    }
}
