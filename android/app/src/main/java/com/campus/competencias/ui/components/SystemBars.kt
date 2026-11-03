/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/components/SystemBars.kt
 * Propósito: Control de barras del sistema y colores de la ventana.
 *
 * Historias de usuario:
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Las pantallas de bienvenida usan un fondo verde oscuro que se dibuja detrás
 * de la barra de estado. Mientras están visibles se usan iconos claros; al
 * salir se restaura el contraste que corresponde al tema actual.
 */
@Composable
fun LightStatusBarIcons() {
    val view = LocalView.current
    val darkTheme = isSystemInDarkTheme()
    if (!view.isInEditMode) {
        DisposableEffect(view, darkTheme) {
            val controller = view.context.findActivity()?.window?.let { window ->
                WindowCompat.getInsetsController(window, view)
            }
            controller?.isAppearanceLightStatusBars = false
            onDispose {
                controller?.isAppearanceLightStatusBars = !darkTheme
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
