/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/components/Status.kt
 * Propósito: Indicadores y etiquetas de estado de eventos y registros.
 *
 * Historias de usuario:
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.campus.competencias.ui.model.StatusTone
import com.campus.competencias.ui.theme.CampusInfo
import com.campus.competencias.ui.theme.CampusPurple
import com.campus.competencias.ui.theme.CampusWarning
import com.campus.competencias.ui.theme.StatusDangerBg
import com.campus.competencias.ui.theme.StatusInfoBg
import com.campus.competencias.ui.theme.StatusNeutralBg
import com.campus.competencias.ui.theme.StatusPurpleBg
import com.campus.competencias.ui.theme.StatusSuccessBg
import com.campus.competencias.ui.theme.StatusWarningBg

private data class ToneColors(
    val foreground: Color,
    val background: Color,
)

@Composable
private fun toneColors(tone: StatusTone): ToneColors {
    val dark = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    return when (tone) {
        StatusTone.SUCCESS -> ToneColors(
            foreground = if (dark) Color(0xFFA9D5B8) else scheme.primary,
            background = if (dark) Color(0xFFA9D5B8).copy(alpha = 0.16f) else StatusSuccessBg,
        )
        StatusTone.WARNING -> ToneColors(
            foreground = if (dark) Color(0xFFE8C47A) else CampusWarning,
            background = if (dark) Color(0xFFE8C47A).copy(alpha = 0.16f) else StatusWarningBg,
        )
        StatusTone.INFO -> ToneColors(
            foreground = if (dark) Color(0xFF8EC7E8) else CampusInfo,
            background = if (dark) Color(0xFF8EC7E8).copy(alpha = 0.16f) else StatusInfoBg,
        )
        StatusTone.DANGER -> ToneColors(
            foreground = scheme.error,
            background = if (dark) scheme.error.copy(alpha = 0.18f) else StatusDangerBg,
        )
        StatusTone.PURPLE -> ToneColors(
            foreground = if (dark) Color(0xFFC4A7E8) else CampusPurple,
            background = if (dark) Color(0xFFC4A7E8).copy(alpha = 0.16f) else StatusPurpleBg,
        )
        StatusTone.NEUTRAL -> ToneColors(
            foreground = scheme.onSurfaceVariant,
            background = if (dark) scheme.surfaceVariant else StatusNeutralBg,
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    tone: StatusTone = StatusTone.NEUTRAL,
    modifier: Modifier = Modifier,
) {
    val colors = toneColors(tone)
    Surface(
        modifier = modifier,
        color = colors.background,
        contentColor = colors.foreground,
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(colors.foreground, CircleShape),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            maxLines = 1,
        )
    }
}
