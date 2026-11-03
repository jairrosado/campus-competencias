/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/components/SummaryCards.kt
 * Propósito: Tarjetas de resumen de totales y activos.
 *
 * Historias de usuario:
 * - HU-12 · Catálogo de apoyo y navegación por rol
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campus.competencias.ui.theme.CampusOlive

data class SummaryItem(
    val label: String,
    val value: String,
    val caption: String,
    val icon: ImageVector,
    val accent: Color? = null,
)

@Composable
fun SummaryCards(
    items: List<SummaryItem>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items, key = { it.label }) { item ->
            SummaryCard(item = item, modifier = Modifier.width(158.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    item: SummaryItem,
    modifier: Modifier = Modifier,
) {
    val accent = item.accent ?: MaterialTheme.colorScheme.primary
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.size(7.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = item.caption,
                style = MaterialTheme.typography.labelSmall,
                color = CampusOlive,
                maxLines = 2,
            )
        }
    }
}
