/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/screens/profile/ProfileScreen.kt
 * Propósito: Perfil propio, sesión, cierre de sesión y cambio de servidor.
 *
 * Historias de usuario:
 * - HU-03 · Consulta y edición del perfil propio
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.model.UserDto
import com.campus.competencias.ui.components.CampusOutlinedCard
import com.campus.competencias.ui.components.InfoRow
import com.campus.competencias.ui.components.InitialsAvatar
import com.campus.competencias.ui.components.PageHeader
import com.campus.competencias.ui.components.SectionTitle
import com.campus.competencias.ui.components.StatusBadge
import com.campus.competencias.ui.model.roleLabel
import com.campus.competencias.ui.model.statusLabel
import com.campus.competencias.ui.model.statusTone

@Composable
// HU-03 y AN-05 · Perfil y sesión
fun ProfileScreen(
    user: UserDto,
    apiBaseUrl: String,
    onLogout: () -> Unit,
    onChangeServer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        PageHeader(
            eyebrow = "TU CUENTA",
            title = "Mi perfil",
            description = "Los datos de la sesión que está activa en este dispositivo.",
        )

        Spacer(Modifier.height(16.dp))
        CampusOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InitialsAvatar(name = user.nombre, size = 64.dp)
                Spacer(Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.nombre.ifBlank { "Usuario sin nombre" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = user.correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(9.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        StatusBadge(
                            text = roleLabel(user.rol),
                            tone = statusTone(user.estado),
                        )
                        StatusBadge(
                            text = statusLabel(user.estado),
                            tone = statusTone(user.estado),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        CampusOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                SectionTitle(
                    title = "Datos de la cuenta",
                    subtitle = "Información pública devuelta por la API.",
                )
                Spacer(Modifier.height(13.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(13.dp))
                InfoRow(label = "Nombre", value = user.nombre)
                Spacer(Modifier.height(10.dp))
                InfoRow(label = "Correo", value = user.correo)
                Spacer(Modifier.height(10.dp))
                InfoRow(label = "Rol", value = roleLabel(user.rol))
                Spacer(Modifier.height(10.dp))
                InfoRow(label = "Estado", value = statusLabel(user.estado))
                Spacer(Modifier.height(10.dp))
                InfoRow(label = "Identificador", value = user.id)
            }
        }

        Spacer(Modifier.height(16.dp))
        CampusOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(19.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    SectionTitle(
                        title = "Conexión",
                        subtitle = "Dirección de la API configurada en este dispositivo.",
                    )
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = apiBaseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                    )
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    text = "En el emulador, 10.0.2.2 apunta al equipo donde corre el backend. " +
                        "En un dispositivo físico usa la IP de tu red local.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onChangeServer,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Dns,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text("Cambiar servidor")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "Tu sesión se mantiene con una cookie segura emitida por la API.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(13.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Campus · Gestión de competencias",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}
