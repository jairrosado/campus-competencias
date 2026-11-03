/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/components/EventCard.kt
 * Propósito: Tarjeta de torneo o hackatón con estado, fechas y datos principales.
 *
 * Historias de usuario:
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campus.competencias.ui.model.EventCardData
import com.campus.competencias.ui.model.ageRangeLabel
import com.campus.competencias.ui.model.formatDateRange
import com.campus.competencias.ui.model.genderLabel
import com.campus.competencias.ui.model.metricLabel
import com.campus.competencias.ui.model.orderLabel
import com.campus.competencias.ui.model.statusTone

@Composable
fun EventCard(
    event: EventCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CampusOutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(
                    text = event.statusText,
                    tone = statusTone(event.status),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = event.type.singular.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (event.description.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(11.dp))
            if (event.location.isNotBlank()) {
                EventMetaRow(
                    icon = Icons.Rounded.Place,
                    text = event.location,
                )
                Spacer(Modifier.height(5.dp))
            }
            EventMetaRow(
                icon = Icons.Rounded.CalendarMonth,
                text = formatDateRange(event.startsAt, event.endsAt),
            )

            if (event.registered != null && event.capacity > 0) {
                val progress = (event.registered.toFloat() / event.capacity.toFloat()).coerceIn(0f, 1f)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${event.registered} de ${event.capacity} cupos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (event.tags.isNotEmpty()) {
                Spacer(Modifier.height(11.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    event.tags.forEach { tag -> TagChip(tag) }
                }
            }
        }
    }
}

@Composable
private fun EventMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun EventDetailsContent(
    event: EventCardData,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(text = event.statusText, tone = statusTone(event.status))
            Spacer(Modifier.width(8.dp))
            Text(
                text = event.type.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = event.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (event.description.isNotBlank()) {
            Spacer(Modifier.height(7.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (event.tags.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                event.tags.forEach { TagChip(it) }
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(14.dp))
        event.details.forEach { detail ->
            InfoRow(label = detail.label, value = detail.value)
            Spacer(Modifier.height(9.dp))
        }

        event.rules?.let { rules ->
            Spacer(Modifier.height(5.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(14.dp))
            SectionTitle(
                title = "Reglamento deportivo",
                subtitle = "Reglas con las que se disputa este torneo.",
            )
            Spacer(Modifier.height(12.dp))
            RuleRow("Integrantes", "${rules.integrantesMin} a ${rules.integrantesMax}")
            RuleRow("Inscripciones por competidor", rules.maxInscripciones.toString())
            RuleRow("Métrica", metricLabel(rules.metrica))
            RuleRow("Unidad", rules.unidad)
            RuleRow("Precisión", rules.precision.toString())
            RuleRow("Orden", orderLabel(rules.orden))
            RuleRow("Género", genderLabel(rules.genero))
            RuleRow("Edad", ageRangeLabel(rules.edadMin, rules.edadMax))
            RuleRow("Fases", rules.fases.joinToString(" · "))
            RuleRow("Avance", rules.avance)
            RuleRow("Desempates", rules.desempates.joinToString(" · "))
            RuleRow("Pruebas", rules.pruebas.joinToString(" · "))
            RuleRow("Requisitos", rules.requisitos)
            RuleRow(
                "Equipos múltiples",
                if (rules.permiteMultiplesEquipos) "Permitidos" else "No permitidos",
            )
        }

        if (event.categories.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Categorías tecnológicas")
            Spacer(Modifier.height(9.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                event.categories.forEach { TagChip(it) }
            }
        }

        if (!event.requirements.isNullOrBlank()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Requisitos de participación")
            Spacer(Modifier.height(7.dp))
            Text(
                text = event.requirements,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RuleRow(
    label: String,
    value: String,
) {
    if (value.isBlank() || value == "0") return
    InfoRow(label = label, value = value)
    Spacer(Modifier.height(8.dp))
}
