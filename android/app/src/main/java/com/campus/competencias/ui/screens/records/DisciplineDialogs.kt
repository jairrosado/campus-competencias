/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/records/DisciplineDialogs.kt
 * Propósito: Formulario de alta y edición de disciplinas con modalidades, formatos
 * y categorías.
 *
 * Historias de usuario:
 * - HU-06 · Gestión de disciplinas deportivas
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.screens.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.model.CategoryInput
import com.campus.competencias.data.model.DisciplineDto
import com.campus.competencias.data.model.DisciplineInput
import com.campus.competencias.ui.components.FieldHint
import com.campus.competencias.ui.components.FilterChips
import com.campus.competencias.ui.components.FormDialogShell
import com.campus.competencias.ui.components.FormGap
import com.campus.competencias.ui.components.FormHint
import com.campus.competencias.ui.components.FormSection
import com.campus.competencias.ui.components.FormTextField
import com.campus.competencias.ui.components.MultiSelectChips
import com.campus.competencias.ui.model.activeStateOptions
import com.campus.competencias.ui.model.categoryGenderOptions
import com.campus.competencias.ui.model.competitionFormatOptions
import com.campus.competencias.ui.model.participationModeOptions

private data class CategoryDraft(
    val id: String? = null,
    val nombre: String = "",
    val edadMin: String = "",
    val edadMax: String = "",
    val genero: String = "cualquiera",
)

@Composable
// HU-06 · Alta y edición de disciplinas
fun DisciplineFormDialog(
    discipline: DisciplineDto?,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (DisciplineInput) -> Unit,
) {
    val key = discipline?.id ?: "new"
    var nombre by remember(key) { mutableStateOf(discipline?.nombre.orEmpty()) }
    var modalidades by remember(key) {
        mutableStateOf(discipline?.modalidades?.toSet() ?: setOf("equipo"))
    }
    var formatos by remember(key) {
        mutableStateOf(discipline?.formatos?.toSet() ?: setOf("liga"))
    }
    var estado by remember(key) {
        mutableStateOf(discipline?.estado?.takeIf { it.isNotBlank() } ?: "activo")
    }
    var categories by remember(key) {
        mutableStateOf(
            discipline?.categorias
                ?.map {
                    CategoryDraft(
                        id = it.id,
                        nombre = it.nombre,
                        edadMin = it.edadMin?.toString().orEmpty(),
                        edadMax = it.edadMax?.toString().orEmpty(),
                        genero = it.genero,
                    )
                }
                ?.ifEmpty { listOf(CategoryDraft()) }
                ?: listOf(CategoryDraft()),
        )
    }
    var localError by remember(key) { mutableStateOf<String?>(null) }

    fun updateCategory(index: Int, transform: (CategoryDraft) -> CategoryDraft) {
        categories = categories.toMutableList().also { it[index] = transform(it[index]) }
        localError = null
    }

    fun submit() {
        val trimmedName = nombre.trim()
        val validation = when {
            trimmedName.length !in 2..100 -> "Escribe el nombre de la disciplina."
            modalidades.isEmpty() -> "Selecciona al menos una modalidad."
            modalidades.size > 3 -> "Puedes seleccionar hasta 3 modalidades."
            formatos.isEmpty() -> "Selecciona al menos un formato."
            formatos.size > 5 -> "Puedes seleccionar hasta 5 formatos."
            categories.isEmpty() -> "Agrega al menos una categoría."
            categories.size > 30 -> "Puedes registrar hasta 30 categorías."
            else -> null
        }
        if (validation != null) {
            localError = validation
            return
        }

        val parsed = mutableListOf<CategoryInput>()
        for (draft in categories) {
            val categoryName = draft.nombre.trim()
            if (categoryName.length !in 2..80) {
                localError = "Cada categoría necesita un nombre de 2 a 80 caracteres."
                return
            }
            val min = draft.edadMin.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
            val max = draft.edadMax.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
            if (draft.edadMin.isNotBlank() && (min == null || min !in 0..100)) {
                localError = "La edad mínima de ${categoryName} debe estar entre 0 y 100."
                return
            }
            if (draft.edadMax.isNotBlank() && (max == null || max !in 0..100)) {
                localError = "La edad máxima de ${categoryName} debe estar entre 0 y 100."
                return
            }
            if (min != null && max != null && min > max) {
                localError = "En ${categoryName} la edad mínima no puede superar la máxima."
                return
            }
            parsed += CategoryInput(
                id = draft.id,
                nombre = categoryName,
                edadMin = min,
                edadMax = max,
                genero = draft.genero,
            )
        }

        if (parsed.map { it.nombre.lowercase() }.toSet().size != parsed.size) {
            localError = "No repitas categorías dentro de la disciplina."
            return
        }

        localError = null
        onConfirm(
            DisciplineInput(
                nombre = trimmedName,
                modalidades = modalidades.toList(),
                formatos = formatos.toList(),
                categorias = parsed,
                estado = estado,
            ),
        )
    }

    FormDialogShell(
        title = if (discipline == null) "Nueva disciplina" else "Editar disciplina",
        subtitle = "Define modalidades, formatos y categorías de competencia.",
        icon = Icons.Rounded.MilitaryTech,
        confirmLabel = if (discipline == null) "Registrar" else "Guardar cambios",
        isSaving = isSaving,
        error = localError ?: error,
        onDismiss = onDismiss,
        onConfirm = ::submit,
    ) {
        FormSection("Datos generales")
        FormTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                localError = null
            },
            label = "Nombre de la disciplina",
            placeholder = "Ej. Fútbol",
        )
        FormGap()
        FormHint("Modalidades (máximo 3)")
        Spacer(Modifier.height(7.dp))
        MultiSelectChips(
            options = participationModeOptions,
            selected = modalidades,
            onToggle = { value ->
                modalidades = if (value in modalidades) modalidades - value else modalidades + value
                localError = null
            },
        )
        FormGap()
        FormHint("Formatos (máximo 5)")
        Spacer(Modifier.height(7.dp))
        MultiSelectChips(
            options = competitionFormatOptions,
            selected = formatos,
            onToggle = { value ->
                formatos = if (value in formatos) formatos - value else formatos + value
                localError = null
            },
        )
        FormGap()
        FieldHint("Estado")
        Spacer(Modifier.height(7.dp))
        FilterChips(
            options = activeStateOptions,
            selected = estado,
            onSelect = { estado = it ?: "activo" },
        )

        Spacer(Modifier.height(20.dp))
        FormSection(
            title = "Categorías",
            subtitle = "Edad y género con los que se disputa cada categoría.",
        )
        categories.forEachIndexed { index, category ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = "Categoría ${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (categories.size > 1) {
                            IconButton(onClick = {
                                categories = categories.toMutableList().also { it.removeAt(index) }
                                localError = null
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Quitar categoría",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    FormTextField(
                        value = category.nombre,
                        onValueChange = { value -> updateCategory(index) { it.copy(nombre = value) } },
                        label = "Nombre",
                        placeholder = "Ej. Sub-17",
                    )
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        FormTextField(
                            value = category.edadMin,
                            onValueChange = { value -> updateCategory(index) { it.copy(edadMin = value) } },
                            label = "Edad mínima",
                            placeholder = "Opcional",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        FormTextField(
                            value = category.edadMax,
                            onValueChange = { value -> updateCategory(index) { it.copy(edadMax = value) } },
                            label = "Edad máxima",
                            placeholder = "Opcional",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                    FilterChips(
                        options = categoryGenderOptions,
                        selected = category.genero,
                        onSelect = { value -> updateCategory(index) { it.copy(genero = value ?: "cualquiera") } },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        FilledTonalButton(
            onClick = {
                categories = categories + CategoryDraft()
                localError = null
            },
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
            Text("Agregar categoría")
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Las categorías en uso no se pueden retirar ni cambiar de edad o género.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
