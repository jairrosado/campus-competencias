/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/main/MainScaffold.kt
 * Propósito: Estructura principal: barra superior, menú lateral, barra inferior y contenido.
 *
 * Historias de usuario:
 * - HU-12 · Catálogo de apoyo y navegación por rol
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.di.AppContainer
import com.campus.competencias.ui.components.CampusBrand
import com.campus.competencias.ui.components.InitialsAvatar
import com.campus.competencias.ui.model.roleLabel
import com.campus.competencias.ui.navigation.CampusDestination
import com.campus.competencias.ui.screens.events.EventsScreen
import com.campus.competencias.ui.screens.institutions.InstitutionsScreen
import com.campus.competencias.ui.screens.profile.ProfileScreen
import com.campus.competencias.ui.screens.records.RecordsScreen
import com.campus.competencias.ui.screens.students.StudentsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// HU-12 y AN-05 · Menú, barra inferior y contenido
fun MainScaffold(
    container: AppContainer,
    user: UserDto,
    onLogout: () -> Unit,
    onChangeServer: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var routeName by rememberSaveable { mutableStateOf(CampusDestination.INSTITUTIONS.name) }
    val current = remember(routeName) {
        runCatching { CampusDestination.valueOf(routeName) }.getOrDefault(CampusDestination.INSTITUTIONS)
    }

    fun navigate(destination: CampusDestination) {
        routeName = destination.name
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CampusDrawer(
                user = user,
                current = current,
                onNavigate = ::navigate,
                onLogout = {
                    scope.launch {
                        drawerState.close()
                        onLogout()
                    }
                },
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "campus.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = current.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(end = 12.dp),
                        ) {
                            Text(
                                text = roleLabel(user.rol),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    CampusDestination.bottomBar(user.rol).forEach { destination ->
                        NavigationBarItem(
                            selected = current == destination,
                            onClick = { routeName = destination.name },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title,
                                )
                            },
                            label = { Text(destination.title, style = MaterialTheme.typography.labelMedium) },
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                when (current) {
                    CampusDestination.INSTITUTIONS -> InstitutionsScreen(
                        userId = user.id,
                        user = user,
                        repository = container.campusRepository,
                        snackbarHostState = snackbarHostState,
                    )

                    CampusDestination.EVENTS -> EventsScreen(
                        userId = user.id,
                        user = user,
                        repository = container.campusRepository,
                        snackbarHostState = snackbarHostState,
                    )

                    CampusDestination.STUDENTS -> StudentsScreen(
                        userId = user.id,
                        repository = container.campusRepository,
                        snackbarHostState = snackbarHostState,
                    )

                    CampusDestination.PROFILE -> ProfileScreen(
                        user = user,
                        apiBaseUrl = container.apiBaseUrl,
                        onLogout = onLogout,
                        onChangeServer = onChangeServer,
                    )

                    else -> current.recordKind?.let { kind ->
                        RecordsScreen(
                            kind = kind,
                            userId = user.id,
                            user = user,
                            repository = container.campusRepository,
                            snackbarHostState = snackbarHostState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampusDrawer(
    user: UserDto,
    current: CampusDestination,
    onNavigate: (CampusDestination) -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.width(304.dp),
        drawerShape = RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 17.dp, vertical = 26.dp),
        ) {
            CampusBrand(showTagline = true, modifier = Modifier.padding(horizontal = 8.dp))
            Spacer(Modifier.height(26.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InitialsAvatar(name = user.nombre, size = 38.dp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = user.nombre,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                        )
                        Text(
                            text = roleLabel(user.rol),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "ESPACIO DE TRABAJO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Spacer(Modifier.height(10.dp))
            CampusDestination.entries
                .filter { it != CampusDestination.PROFILE && CampusDestination.visibleFor(user.rol).contains(it) }
                .forEach { destination ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (current == destination) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        selected = current == destination,
                        onClick = { onNavigate(destination) },
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))
            NavigationDrawerItem(
                label = { Text("Mi perfil", style = MaterialTheme.typography.bodyMedium) },
                icon = {
                    Icon(
                        imageVector = CampusDestination.PROFILE.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                selected = current == CampusDestination.PROFILE,
                onClick = { onNavigate(CampusDestination.PROFILE) },
                shape = RoundedCornerShape(12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
            Spacer(Modifier.height(26.dp))
            Text(
                text = "Una comunidad. Muchas posibilidades.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Todo comienza con las personas y las instituciones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Spacer(Modifier.height(22.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
