/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/institutions/InstitutionDialogs.kt
 * Propósito: Formularios de creación, edición y confirmación de borrado de instituciones.
 *
 * Historias de usuario:
 * - HU-04 · Registro y consulta de instituciones
 * - HU-05 · Edición, eliminación y desactivación de instituciones
 * - AN-03 · Gestión de instituciones en Android
 */
package com.campus.competencias.ui.screens.institutions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.campus.competencias.data.model.InstitutionDto
import com.campus.competencias.data.model.InstitutionInput
import com.campus.competencias.ui.components.DangerPanel
import com.campus.competencias.ui.components.FieldHint
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.InlineError

private val INSTITUTION_TYPES = listOf(
    "colegio" to "Colegio",
    "universidad" to "Universidad",
    "instituto" to "Instituto",
    "otro" to "Otra",
)

@Composable
// HU-04 y HU-05 · Alta y edición
fun InstitutionFormDialog(
    institution: InstitutionDto?,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (InstitutionInput) -> Unit,
) {
    val key = institution?.id ?: "new"
    var nombre by rememberSaveable(key) { mutableStateOf(institution?.nombre.orEmpty()) }
    var ciudad by rememberSaveable(key) { mutableStateOf(institution?.ciudad.orEmpty()) }
    var departamento by rememberSaveable(key) { mutableStateOf(institution?.departamento.orEmpty()) }
    var tipo by rememberSaveable(key) {
        mutableStateOf(institution?.tipo?.takeIf { it.isNotBlank() } ?: "colegio")
    }
    var estado by rememberSaveable(key) {
        mutableStateOf(institution?.estado?.takeIf { it.isNotBlank() } ?: "activo")
    }
    var localError by rememberSaveable(key) { mutableStateOf<String?>(null) }

    fun submit() {
        val trimmedName = nombre.trim()
        val trimmedCity = ciudad.trim()
        val trimmedDepartment = departamento.trim()
        val validation = when {
            trimmedName.length < 2 -> "Escribe el nombre de la institución."
            trimmedCity.length < 2 -> "Escribe la ciudad o municipio."
            trimmedDepartment.length < 2 -> "Escribe el departamento."
            else -> null
        }
        localError = validation
        if (validation == null) {
            onConfirm(
                InstitutionInput(
                    nombre = trimmedName,
                    ciudad = trimmedCity,
                    departamento = trimmedDepartment,
                    tipo = tipo,
                    estado = estado,
                ),
            )
        }
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(11.dp)
                                .size(25.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (institution == null) "Nueva institución" else "Editar institución",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (institution == null) {
                                "Agrega la información de la institución educativa."
                            } else {
                                "Actualiza los datos de la institución."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        localError = null
                    },
                    label = { Text("Nombre de la institución") },
                    placeholder = { Text("Ej. Universidad del Norte") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                )

                Spacer(Modifier.height(13.dp))
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = {
                        ciudad = it
                        localError = null
                    },
                    label = { Text("Ciudad / municipio") },
                    placeholder = { Text("Ej. Barranquilla") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                )

                Spacer(Modifier.height(13.dp))
                OutlinedTextField(
                    value = departamento,
                    onValueChange = {
                        departamento = it
                        localError = null
                    },
                    label = { Text("Departamento") },
                    placeholder = { Text("Ej. Atlántico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                )

                Spacer(Modifier.height(18.dp))
                FieldHint("Tipo de institución")
                Spacer(Modifier.height(8.dp))
                FilterChips(
                    options = INSTITUTION_TYPES,
                    selected = tipo,
                    onSelect = { value -> tipo = value ?: "colegio" },
                )

                Spacer(Modifier.height(16.dp))
                FieldHint("Estado")
                Spacer(Modifier.height(8.dp))
                FilterChips(
                    options = listOf("activo" to "Activa", "inactivo" to "Inactiva"),
                    selected = estado,
                    onSelect = { value -> estado = value ?: "activo" },
                )

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "Una institución inactiva permanece en el historial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                val visibleError = localError ?: error
                if (visibleError != null) {
                    Spacer(Modifier.height(14.dp))
                    InlineError(visibleError)
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = ::submit,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(17.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(if (institution == null) "Registrar" else "Guardar cambios")
                        }
                    }
                }
            }
        }
    }
}

@Composable
// HU-05 · Confirmación de borrado
fun InstitutionDeleteDialog(
    institution: InstitutionDto,
    isSaving: Boolean,
    error: String?,
    blocked: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDeactivate: () -> Unit,
) {
    var blockedState by rememberSaveable { mutableStateOf(blocked) }
    LaunchedEffect(blocked) {
        if (blocked) blockedState = true
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(11.dp)
                            .size(25.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (blockedState) "No se puede eliminar" else "Eliminar institución",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (blockedState) {
                        "La institución tiene registros asociados. Puedes desactivarla para conservar su historial."
                    } else {
                        "¿Seguro que deseas eliminar \"${institution.nombre}\"? Esta acción no se puede deshacer."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!blockedState) {
                    Spacer(Modifier.height(14.dp))
                    DangerPanel(
                        message = "Si la institución participa en competencias, el sistema te ofrecerá desactivarla.",
                    )
                }

                if (error != null) {
                    Spacer(Modifier.height(14.dp))
                    InlineError(error)
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    if (blockedState) {
                        Button(
                            onClick = onDeactivate,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            SavingContent(isSaving = isSaving, label = "Desactivar institución")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDelete,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            SavingContent(isSaving = isSaving, label = "Eliminar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingContent(
    isSaving: Boolean,
    label: String,
) {
    if (isSaving) {
        CircularProgressIndicator(
            modifier = Modifier.size(17.dp),
            strokeWidth = 2.dp,
        )
    } else {
        Text(label)
    }
}
