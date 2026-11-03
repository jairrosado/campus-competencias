/**
 * Archivo: android/app/src/main/java/com/campus/competencias/CampusApplication.kt
 * Propósito: Application: crea el contenedor de dependencias una sola vez por proceso.
 *
 * Historias de usuario:
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias

import android.app.Application
import com.campus.competencias.di.AppContainer

class CampusApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
