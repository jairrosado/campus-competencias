/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/navigation/CampusDestination.kt
 * Propósito: Destinos de navegación y visibilidad según el rol del usuario.
 *
 * Historias de usuario:
 * - HU-12 · Catálogo de apoyo y navegación por rol
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.campus.competencias.ui.model.RecordKind

// HU-12 y AN-05 · Destinos visibles por rol
enum class CampusDestination(
    val title: String,
    val icon: ImageVector,
    val recordKind: RecordKind? = null,
    val adminOnly: Boolean = false,
) {
    INSTITUTIONS("Instituciones", Icons.Rounded.Business),
    STUDENTS("Estudiantes", Icons.Rounded.School, adminOnly = true),
    EVENTS("Eventos", Icons.Rounded.EmojiEvents),
    DISCIPLINES("Disciplinas", Icons.Rounded.MilitaryTech, RecordKind.DISCIPLINES),
    PARTICIPANTS("Participantes", Icons.Rounded.Person, RecordKind.PARTICIPANTS),
    TEAMS("Equipos", Icons.Rounded.Groups, RecordKind.TEAMS),
    REGISTRATIONS("Inscripciones", Icons.Rounded.HowToReg, RecordKind.REGISTRATIONS),
    PROFILE("Mi perfil", Icons.Rounded.Person),
    ;

    companion object {
        /** Recursos que el backend restringe a organizadores y participantes. */
        private val competitionOnly = setOf(PARTICIPANTS, TEAMS, REGISTRATIONS)

        /** HU-02 y HU-12 · Visibilidad según rol y permisos reales del backend. */
        fun visibleFor(role: String): List<CampusDestination> =
            entries.filter { destination ->
                (!destination.adminOnly || role == "administrador") &&
                    (role != "jurado" || destination !in competitionOnly)
            }

        /** La barra inferior cambia para dar acceso directo a Estudiantes al administrador. */
        fun bottomBar(role: String): List<CampusDestination> =
            if (role == "administrador") {
                listOf(INSTITUTIONS, STUDENTS, EVENTS, DISCIPLINES, PROFILE)
            } else {
                listOf(INSTITUTIONS, EVENTS, DISCIPLINES, PROFILE)
            }
    }
}
