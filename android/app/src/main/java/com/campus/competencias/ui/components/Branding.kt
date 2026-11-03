/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/components/Branding.kt
 * Propósito: Identidad visual compartida: logo y encabezados de marca.
 *
 * Historias de usuario:
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campus.competencias.ui.model.initials
import com.campus.competencias.ui.theme.CampusLime
import com.campus.competencias.ui.theme.CampusOlive

@Composable
fun CampusBrand(
    modifier: Modifier = Modifier,
    light: Boolean = false,
    showTagline: Boolean = false,
) {
    val textColor = if (light) Color.White else MaterialTheme.colorScheme.onBackground
    val markColor = if (light) CampusLime else MaterialTheme.colorScheme.primary
    val markBackground = if (light) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer
    val markSize = if (showTagline) 46.dp else 38.dp

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(markSize)
                .background(markBackground, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.School,
                contentDescription = null,
                tint = markColor,
                modifier = Modifier.size(if (showTagline) 27.dp else 23.dp),
            )
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "campus",
                    fontSize = if (showTagline) 29.sp else 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.2).sp,
                    color = textColor,
                )
                Text(
                    text = ".",
                    fontSize = if (showTagline) 29.sp else 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (light) CampusLime else CampusOlive,
                )
            }
            if (showTagline) {
                Text(
                    text = "GESTIÓN DE COMPETENCIAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (light) Color.White.copy(alpha = 0.62f) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials(name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
