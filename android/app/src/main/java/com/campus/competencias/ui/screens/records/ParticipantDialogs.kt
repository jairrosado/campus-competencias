/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/records/ParticipantDialogs.kt
 * Propósito: Formulario de alta y edición de participantes vinculados a una
 * cuenta de estudiante y a una institución.
 *
 * Historias de usuario:
 * - HU-10 · Registro de participantes
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.records

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.model.CatalogDto
import com.campus.competencias.data.model.ParticipantDto
import com.campus.competencias.data.model.ParticipantInput
import com.campus.competencias.ui.components.CampusDropdown
import com.campus.competencias.ui.components.FieldHint
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.FormDialogShell
import com.campus.competencias.ui.components.FormGap
import com.campus.competencias.ui.components.FormHint
import com.campus.competencias.ui.components.FormSection
import com.campus.competencias.ui.components.FormTextField
import com.campus.competencias.ui.model.activeStateOptions
import com.campus.competencias.ui.model.optionLabel
import com.campus.competencias.ui.model.participantGenderOptions

@Composable
// HU-10 · Alta y edición de participantes
fun ParticipantFormDialog(
    participant: ParticipantDto?,
    catalogs: CatalogDto,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (ParticipantInput) -> Unit,
) {
    val key = participant?.id ?: "new"

    val userOptions = catalogs.usuarios
        .filter {
            (it.rol == "participante" && it.estado == "activo") ||
                it.id == participant?.usuario
        }
        .map { it.id to it.nombre }
    val institutionOptions = catalogs.instituciones
        .filter { it.estado == "activo" || it.id == participant?.institucion }
        .map { it.id to it.nombre }

    var documento by rememberSaveable(key) { mutableStateOf(participant?.documento.orEmpty()) }
    var nombre by rememberSaveable(key) { mutableStateOf(participant?.nombre.orEmpty()) }
    var nacimiento by rememberSaveable(key) {
        mutableStateOf(participant?.nacimiento?.take(10).orEmpty())
    }
    var genero by rememberSaveable(key) {
        mutableStateOf(participant?.genero?.takeIf { it.isNotBlank() } ?: "femenino")
    }
    var usuario by rememberSaveable(key) {
        mutableStateOf(participant?.usuario ?: userOptions.firstOrNull()?.first.orEmpty())
    }
    var institucion by rememberSaveable(key) {
        mutableStateOf(participant?.institucion ?: institutionOptions.firstOrNull()?.first.orEmpty())
    }
    var categoria by rememberSaveable(key) { mutableStateOf(participant?.categoria.orEmpty()) }
    var estado by rememberSaveable(key) {
        mutableStateOf(participant?.estado?.takeIf { it.isNotBlank() } ?: "activo")
    }
    var localError by rememberSaveable(key) { mutableStateOf<String?>(null) }

    fun submit() {
        val trimmedDocument = documento.trim()
        val trimmedName = nombre.trim()
        val trimmedCategory = categoria.trim()
        val validation = when {
            trimmedDocument.length !in 2..30 -> "Escribe un documento de 2 a 30 caracteres."
            trimmedName.length !in 2..100 -> "Escribe el nombre completo."
            !DATE_PATTERN.matches(nacimiento.trim()) -> "La fecha de nacimiento debe tener el formato AAAA-MM-DD."
            usuario.isBlank() -> "Selecciona la cuenta del participante."
            institucion.isBlank() -> "Selecciona la institución."
            trimmedCategory.length !in 2..80 -> "Escribe una categoría de 2 a 80 caracteres."
            else -> null
        }
        localError = validation
        if (validation == null) {
            onConfirm(
                ParticipantInput(
                    documento = trimmedDocument,
                    nombre = trimmedName,
                    nacimiento = nacimiento.trim(),
                    genero = genero,
                    usuario = usuario,
                    institucion = institucion,
                    categoria = trimmedCategory,
                    estado = estado,
                ),
            )
        }
    }

    FormDialogShell(
        title = if (participant == null) "Nuevo participante" else "Editar participante",
        subtitle = "Vincula una cuenta de estudiante con su institución y categoría.",
        icon = Icons.Rounded.Person,
        confirmLabel = if (participant == null) "Registrar" else "Guardar cambios",
        isSaving = isSaving,
        error = localError ?: error,
        onDismiss = onDismiss,
        onConfirm = ::submit,
    ) {
        FormSection("Identificación")
        FormTextField(
            value = documento,
            onValueChange = {
                documento = it
                localError = null
            },
            label = "Documento",
            placeholder = "Ej. 1020304050",
        )
        FormGap()
        FormTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                localError = null
            },
            label = "Nombre completo",
            placeholder = "Ej. Camila Torres",
        )
        FormGap()
        FormTextField(
            value = nacimiento,
            onValueChange = {
                nacimiento = it
                localError = null
            },
            label = "Fecha de nacimiento",
            placeholder = "AAAA-MM-DD",
        )
        FormHint("Ejemplo: 2007-04-18. Se usa para validar la edad en cada categoría.")
        FormGap()
        FieldHint("Género")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = participantGenderOptions,
            selected = genero,
            onSelect = { genero = it ?: "femenino" },
        )

        Spacer(Modifier.height(20.dp))
        FormSection("Cuenta e institución")
        if (userOptions.isEmpty()) {
            Text(
                text = "No hay cuentas de estudiante activas. Créalas primero en la sección Estudiantes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(8.dp))
        }
        CampusDropdown(
            label = "Cuenta del participante",
            options = userOptions,
            selected = usuario.takeIf { it.isNotBlank() },
            onSelect = {
                usuario = it
                localError = null
            },
        )
        FormGap()
        CampusDropdown(
            label = "Institución",
            options = institutionOptions,
            selected = institucion.takeIf { it.isNotBlank() },
            onSelect = {
                institucion = it
                localError = null
            },
        )
        FormGap()
        FormTextField(
            value = categoria,
            onValueChange = {
                categoria = it
                localError = null
            },
            label = "Categoría de referencia",
            placeholder = "Ej. Sub-17",
        )
        FormGap()
        FieldHint("Estado")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = activeStateOptions,
            selected = estado,
            onSelect = { estado = it ?: "activo" },
        )
        Spacer(Modifier.height(8.dp))
        FormHint(
            "Género seleccionado: ${optionLabel(participantGenderOptions, genero)}.",
        )
    }
}

private val DATE_PATTERN = Regex("^\\d{4}-\\d{2}-\\d{2}$")
