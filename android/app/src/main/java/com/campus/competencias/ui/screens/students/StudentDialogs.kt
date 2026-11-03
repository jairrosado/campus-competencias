/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/students/StudentDialogs.kt
 * Propósito: Formulario de alta y edición de cuentas de estudiante (rol participante).
 *
 * Historias de usuario:
 * - HU-02 · Gestión de cuentas de usuario
 */
package com.campus.competencias.ui.screens.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.data.model.UserInput
import com.campus.competencias.ui.components.CampusDropdown
import com.campus.competencias.ui.components.FieldHint
import com.campus.competencias.ui.components.FormDialogShell
import com.campus.competencias.ui.components.FormTextField
import com.campus.competencias.ui.model.activeStateOptions

@Composable
// HU-02 · Alta y edición de estudiantes
fun StudentFormDialog(
    student: UserDto?,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (UserInput) -> Unit,
) {
    val key = student?.id ?: "new"
    var nombre by rememberSaveable(key) { mutableStateOf(student?.nombre.orEmpty()) }
    var correo by rememberSaveable(key) { mutableStateOf(student?.correo.orEmpty()) }
    var password by rememberSaveable(key) { mutableStateOf("") }
    var estado by rememberSaveable(key) {
        mutableStateOf(student?.estado?.takeIf { it.isNotBlank() } ?: "activo")
    }
    var localError by rememberSaveable(key) { mutableStateOf<String?>(null) }

    fun submit() {
        val trimmedName = nombre.trim()
        val trimmedEmail = correo.trim().lowercase()
        val validation = when {
            trimmedName.length !in 2..100 -> "Escribe el nombre completo del estudiante."
            trimmedEmail.length > 254 -> "El correo es demasiado largo."
            !EMAIL_PATTERN.matches(trimmedEmail) -> "Escribe un correo válido."
            student == null && password.length !in 10..128 ->
                "La contraseña debe tener entre 10 y 128 caracteres."
            student != null && password.isNotBlank() && password.length !in 10..128 ->
                "La nueva contraseña debe tener entre 10 y 128 caracteres."
            else -> null
        }
        localError = validation
        if (validation == null) {
            onConfirm(
                UserInput(
                    nombre = trimmedName,
                    correo = trimmedEmail,
                    rol = "participante",
                    estado = estado,
                    password = password.takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    FormDialogShell(
        title = if (student == null) "Nuevo estudiante" else "Editar estudiante",
        subtitle = "Cuenta con rol participante para inscribirse en competencias.",
        icon = Icons.Rounded.School,
        confirmLabel = if (student == null) "Registrar" else "Guardar cambios",
        isSaving = isSaving,
        error = localError ?: error,
        onDismiss = onDismiss,
        onConfirm = ::submit,
    ) {
        FormTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                localError = null
            },
            label = "Nombre completo",
            placeholder = "Ej. Laura Gómez",
        )
        Spacer(Modifier.height(12.dp))
        FormTextField(
            value = correo,
            onValueChange = {
                correo = it
                localError = null
            },
            label = "Correo",
            placeholder = "estudiante@correo.com",
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(12.dp))
        FormTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = if (student == null) "Contraseña" else "Nueva contraseña (opcional)",
            placeholder = if (student == null) "Mínimo 10 caracteres" else "Déjala vacía para conservarla",
            keyboardType = KeyboardType.Password,
        )
        Spacer(Modifier.height(6.dp))
        FieldHint("La cuenta podrá iniciar sesión y ser asociada a un participante.")
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Estado de la cuenta",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(7.dp))
        CampusDropdown(
            label = "Estado",
            options = activeStateOptions,
            selected = estado,
            onSelect = { estado = it },
        )
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Rol asignado: participante",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
