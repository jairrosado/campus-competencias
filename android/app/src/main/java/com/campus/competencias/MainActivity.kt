/**
 * Archivo: android/app/src/main/java/com/campus/competencias/MainActivity.kt
 * Propósito: Activity única: aplica el tema Compose y monta CampusApp.
 *
 * Historias de usuario:
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.campus.competencias.di.AppContainer
import com.campus.competencias.ui.CampusApp
import com.campus.competencias.ui.theme.CampusTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CampusApplication).container

        setContent {
            CampusTheme {
                CampusApp(container = container)
            }
        }
    }
}
