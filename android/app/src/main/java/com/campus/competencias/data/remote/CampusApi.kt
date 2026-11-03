/**
 * Archivo: android/app/src/main/java/com/campus/competencias/data/remote/CampusApi.kt
 * Propósito: Interfaz Retrofit con todos los endpoints existentes de la API.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - HU-02 · Gestión de cuentas de usuario
 * - HU-03 · Consulta y edición del perfil propio
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
 */
package com.campus.competencias.data.remote

import com.campus.competencias.data.model.ApiEnvelope
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
import com.campus.competencias.data.model.LoginRequest
import com.campus.competencias.data.model.MessageDto
import com.campus.competencias.data.model.PageDto
import com.campus.competencias.data.model.ParticipantDto
import com.campus.competencias.data.model.ParticipantInput
import com.campus.competencias.data.model.RegistrationDto
import com.campus.competencias.data.model.RegistrationInput
import com.campus.competencias.data.model.SessionDto
import com.campus.competencias.data.model.SetupAdminRequest
import com.campus.competencias.data.model.SetupStatusDto
import com.campus.competencias.data.model.TeamDto
import com.campus.competencias.data.model.TeamInput
import com.campus.competencias.data.model.TournamentDto
import com.campus.competencias.data.model.TournamentInput
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.model.UserInput
import com.campus.competencias.data.model.UserStatusPatch
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato HTTP existente. Solo se declaran los endpoints; ninguna ruta,
 * parámetro ni estructura JSON se modifica.
 */
interface CampusApi {

    @GET("api/setup/status")
    suspend fun setupStatus(): ApiEnvelope<SetupStatusDto>

    @POST("api/setup/administrador")
    suspend fun setupAdministrator(@Body body: SetupAdminRequest): ApiEnvelope<SessionDto>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): ApiEnvelope<SessionDto>

    @GET("api/auth/me")
    suspend fun me(): ApiEnvelope<SessionDto>

    @POST("api/auth/logout")
    suspend fun logout(): ApiEnvelope<MessageDto>

    @GET("api/instituciones")
    // HU-04 y AN-02 · Instituciones
    suspend fun institutions(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String?,
        @Query("estado") estado: String?,
    ): ApiEnvelope<PageDto<InstitutionDto>>

    @POST("api/instituciones")
    suspend fun createInstitution(@Body body: InstitutionInput): ApiEnvelope<InstitutionDto>

    @PUT("api/instituciones/{id}")
    suspend fun updateInstitution(
        @Path("id") id: String,
        @Body body: InstitutionInput,
    ): ApiEnvelope<InstitutionDto>

    @PATCH("api/instituciones/{id}")
    suspend fun patchInstitution(
        @Path("id") id: String,
        @Body body: InstitutionPatch,
    ): ApiEnvelope<InstitutionDto>

    @DELETE("api/instituciones/{id}")
    suspend fun deleteInstitution(@Path("id") id: String): ApiEnvelope<DeletedDto>

    @GET("api/usuarios")
    // HU-02 · Cuentas de usuario (solo administrador)
    suspend fun users(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String?,
        @Query("rol") role: String?,
        @Query("estado") estado: String?,
    ): ApiEnvelope<PageDto<UserDto>>

    @POST("api/usuarios")
    // HU-02 · Alta de cuentas
    suspend fun createUser(@Body body: UserInput): ApiEnvelope<UserDto>

    @PATCH("api/usuarios/{id}")
    // HU-02 · Edición administrativa de cuentas
    suspend fun updateUser(
        @Path("id") id: String,
        @Body body: UserInput,
    ): ApiEnvelope<UserDto>

    @PATCH("api/usuarios/{id}")
    // HU-02 · Activar o desactivar una cuenta
    suspend fun patchUserStatus(
        @Path("id") id: String,
        @Body body: UserStatusPatch,
    ): ApiEnvelope<UserDto>

    @GET("api/catalogos")
    // HU-12 · Catálogos
    suspend fun catalogs(): ApiEnvelope<CatalogDto>

    @GET("api/torneos")
    // HU-07 · Torneos
    suspend fun tournaments(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String?,
        @Query("estado") estado: String?,
    ): ApiEnvelope<PageDto<TournamentDto>>

    @GET("api/hackatones")
    // HU-09 · Hackatones
    suspend fun hackathons(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String?,
        @Query("estado") estado: String?,
    ): ApiEnvelope<PageDto<HackathonDto>>

    @GET("api/disciplinas")
    // HU-06 · Disciplinas
    suspend fun disciplines(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String?,
        @Query("estado") estado: String?,
    ): ApiEnvelope<PageDto<DisciplineDto>>

    @GET("api/participantes")
    // HU-10 · Participantes
    suspend fun participants(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String?,
        @Query("estado") estado: String?,
    ): ApiEnvelope<PageDto<ParticipantDto>>

    @GET("api/equipos")
    // HU-11 · Equipos
    suspend fun teams(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String?,
        @Query("estado") estado: String?,
    ): ApiEnvelope<PageDto<TeamDto>>

    @GET("api/inscripciones")
    // HU-13 · Inscripciones
    suspend fun registrations(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("torneo") tournamentId: String?,
    ): ApiEnvelope<PageDto<RegistrationDto>>

    @POST("api/disciplinas")
    // HU-06 · Crear disciplina
    suspend fun createDiscipline(@Body body: DisciplineInput): ApiEnvelope<DisciplineDto>

    @PATCH("api/disciplinas/{id}")
    // HU-06 · Editar disciplina
    suspend fun updateDiscipline(
        @Path("id") id: String,
        @Body body: DisciplineInput,
    ): ApiEnvelope<DisciplineDto>

    @POST("api/torneos")
    // HU-07 y HU-08 · Crear torneo con reglamento
    suspend fun createTournament(@Body body: TournamentInput): ApiEnvelope<TournamentDto>

    @PATCH("api/torneos/{id}")
    // HU-07 y HU-08 · Editar torneo
    suspend fun updateTournament(
        @Path("id") id: String,
        @Body body: TournamentInput,
    ): ApiEnvelope<TournamentDto>

    @PATCH("api/torneos/{id}/estado")
    // HU-08 · Abrir inscripciones de un torneo
    suspend fun openTournamentRegistrations(
        @Path("id") id: String,
        @Body body: EventStateInput,
    ): ApiEnvelope<TournamentDto>

    @POST("api/hackatones")
    // HU-09 · Crear hackatón
    suspend fun createHackathon(@Body body: HackathonInput): ApiEnvelope<HackathonDto>

    @PATCH("api/hackatones/{id}")
    // HU-09 · Editar hackatón
    suspend fun updateHackathon(
        @Path("id") id: String,
        @Body body: HackathonInput,
    ): ApiEnvelope<HackathonDto>

    @PATCH("api/hackatones/{id}/estado")
    // HU-08 y HU-09 · Abrir inscripciones de un hackatón
    suspend fun openHackathonRegistrations(
        @Path("id") id: String,
        @Body body: EventStateInput,
    ): ApiEnvelope<HackathonDto>

    @POST("api/participantes")
    // HU-10 · Crear participante
    suspend fun createParticipant(@Body body: ParticipantInput): ApiEnvelope<ParticipantDto>

    @PATCH("api/participantes/{id}")
    // HU-10 · Editar participante
    suspend fun updateParticipant(
        @Path("id") id: String,
        @Body body: ParticipantInput,
    ): ApiEnvelope<ParticipantDto>

    @POST("api/equipos")
    // HU-11 · Crear equipo
    suspend fun createTeam(@Body body: TeamInput): ApiEnvelope<TeamDto>

    @PATCH("api/equipos/{id}")
    // HU-11 · Editar equipo
    suspend fun updateTeam(
        @Path("id") id: String,
        @Body body: TeamInput,
    ): ApiEnvelope<TeamDto>

    @POST("api/inscripciones")
    // HU-13 · Inscribir competidor
    suspend fun createRegistration(@Body body: RegistrationInput): ApiEnvelope<RegistrationDto>
}
