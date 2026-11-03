/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/CampusApp.kt
 * Propósito: Fases de la app (cargando, setup, login, sesión, error) y cambio de servidor.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.competencias.data.remote.ServerSettings
import com.campus.competencias.di.AppContainer
import com.campus.competencias.ui.auth.AppPhase
import com.campus.competencias.ui.auth.AppViewModel
import com.campus.competencias.ui.auth.ConnectionErrorScreen
import com.campus.competencias.ui.auth.LoginScreen
import com.campus.competencias.ui.auth.SplashScreen
import com.campus.competencias.ui.components.ServerSettingsDialog
import com.campus.competencias.ui.screens.main.MainScaffold

@Composable
// HU-01 y AN-01/AN-05 · Fases y navegación principal
fun CampusApp(container: AppContainer) {
    val factory = remember(container) {
        AppViewModel.factory(container.authRepository, container.sessionManager)
    }
    val appViewModel: AppViewModel = viewModel(factory = factory)
    val state by appViewModel.state.collectAsStateWithLifecycle()
    var showServerDialog by rememberSaveable { mutableStateOf(false) }

    val openServerDialog: () -> Unit = { showServerDialog = true }

    when (state.phase) {
        AppPhase.LOADING -> SplashScreen()

        AppPhase.CONNECTION_ERROR -> ConnectionErrorScreen(
            message = state.connectionError ?: "No pudimos conectar con la API.",
            apiBaseUrl = container.apiBaseUrl,
            onRetry = appViewModel::bootstrap,
            onChangeServer = openServerDialog,
        )

        AppPhase.SETUP, AppPhase.LOGIN -> LoginScreen(
            setup = state.phase == AppPhase.SETUP,
            isSubmitting = state.isSubmitting,
            error = state.formError,
            apiBaseUrl = container.apiBaseUrl,
            onClearError = appViewModel::clearFormError,
            onChangeServer = openServerDialog,
            onSubmit = { nombre, correo, password, confirmation ->
                if (state.phase == AppPhase.SETUP) {
                    appViewModel.createAdministrator(nombre, correo, password, confirmation)
                } else {
                    appViewModel.login(correo, password)
                }
            },
        )

        AppPhase.READY -> {
            val user = state.user
            if (user == null) {
                LaunchedEffect(Unit) { appViewModel.bootstrap() }
            } else {
                MainScaffold(
                    container = container,
                    user = user,
                    onLogout = appViewModel::logout,
                    onChangeServer = openServerDialog,
                )
            }
        }
    }

    if (showServerDialog) {
        ServerSettingsDialog(
            currentUrl = container.apiBaseUrl,
            onDismiss = { showServerDialog = false },
            onSave = { url ->
                showServerDialog = false
                if (ServerSettings.normalize(url) != container.apiBaseUrl) {
                    // La cookie pertenece al servidor anterior; se descarta y se
                    // vuelve a preguntar el estado de sesión al nuevo backend.
                    container.changeServer(url)
                    appViewModel.bootstrap()
                }
            },
        )
    }
}
