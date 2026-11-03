/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/theme/Theme.kt
 * Propósito: Tema Material 3 y esquema de colores claro/oscuro.
 *
 * Historias de usuario:
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = CampusGreen,
    onPrimary = Color.White,
    primaryContainer = CampusMint,
    onPrimaryContainer = CampusDeep,
    inversePrimary = CampusLime,
    secondary = CampusOlive,
    onSecondary = Color.White,
    secondaryContainer = CampusLime,
    onSecondaryContainer = Color(0xFF2C3B18),
    tertiary = CampusSand,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF7EDD6),
    onTertiaryContainer = Color(0xFF4A3A16),
    background = CampusBackground,
    onBackground = CampusText,
    surface = CampusSurface,
    onSurface = CampusText,
    surfaceVariant = CampusSurfaceVariant,
    onSurfaceVariant = Color(0xFF5B6B5F),
    surfaceContainer = Color(0xFFF1F4EC),
    surfaceContainerLow = Color(0xFFF7F9F3),
    surfaceContainerHigh = Color(0xFFECF0E6),
    outline = CampusOutline,
    outlineVariant = CampusLine,
    error = CampusDanger,
    onError = Color.White,
    errorContainer = StatusDangerBg,
    onErrorContainer = CampusDangerDark,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9D5B8),
    onPrimary = Color(0xFF0B2419),
    primaryContainer = Color(0xFF1E4A37),
    onPrimaryContainer = Color(0xFFD6EEDA),
    secondary = CampusLime,
    onSecondary = Color(0xFF2C3B18),
    secondaryContainer = Color(0xFF40552A),
    onSecondaryContainer = Color(0xFFE8F4C8),
    tertiary = Color(0xFFD8BE84),
    onTertiary = Color(0xFF3C2F12),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted,
    surfaceContainer = Color(0xFF193126),
    surfaceContainerLow = Color(0xFF12271E),
    surfaceContainerHigh = Color(0xFF213C30),
    outline = DarkOutline,
    outlineVariant = Color(0xFF2C4639),
    error = Color(0xFFFFB4A2),
    onError = Color(0xFF5F190B),
    errorContainer = Color(0xFF7A2E1D),
    onErrorContainer = Color(0xFFFFDAD2),
)

private val CampusShapes = Shapes(
    extraSmall = RoundedCornerShape(7.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun CampusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CampusTypography,
        shapes = CampusShapes,
        content = content,
    )
}
