/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/components/ServerSettingsDialog.kt
 * Propósito: Diálogo para cambiar la URL del backend sin recompilar.
 *
 * Historias de usuario:
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 * - AN-05 · Perfil, sesión y navegación en Android
 */
package com.campus.competencias.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.campus.competencias.data.remote.ServerSettings

/**
 * Permite cambiar la dirección del backend sin recompilar. Es la misma
 * pantalla para el emulador y para un celular físico.
 */
@Composable
// AN-01 y AN-05 · Cambio de servidor
fun ServerSettingsDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by rememberSaveable(currentUrl) { mutableStateOf(currentUrl) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val normalizedValue = ServerSettings.normalize(value)
    val loopback = ServerSettings.isLoopback(value)
    val changed = normalizedValue != ServerSettings.normalize(currentUrl)

    fun save() {
        val validation = ServerSettings.validate(value)
        error = validation
        if (validation == null) {
            onSave(ServerSettings.normalize(value))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Dns,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text("Servidor de la API") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "La app usa por defecto la API en la nube. También puedes escribir la dirección de un backend local para desarrollo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Dirección del servidor") },
                    placeholder = { Text(ServerSettings.CLOUD_URL) },
                    singleLine = true,
                    isError = error != null,
                    shape = RoundedCornerShape(13.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (loopback) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "En un celular, localhost apunta al propio teléfono. Usa la IP de tu computador.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AssistChip(
                        onClick = {
                            value = ServerSettings.CLOUD_URL
                            error = null
                        },
                        label = { Text("Nube") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Cloud,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                        },
                    )
                    AssistChip(
                        onClick = {
                            value = ServerSettings.EMULATOR_URL
                            error = null
                        },
                        label = { Text("Emulador") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.PhoneAndroid,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                        },
                    )
                }
                Spacer(Modifier.height(14.dp))
                HintLine(
                    icon = Icons.Rounded.Cloud,
                    text = "Nube: ${ServerSettings.CLOUD_URL}",
                )
                Spacer(Modifier.height(7.dp))
                HintLine(
                    icon = Icons.Rounded.PhoneAndroid,
                    text = "Emulador: ${ServerSettings.EMULATOR_URL}",
                )
                Spacer(Modifier.height(7.dp))
                HintLine(
                    icon = Icons.Rounded.Wifi,
                    text = "Celular: la IP de tu computador, por ejemplo http://192.168.1.10:3001",
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "Las direcciones locales solo funcionan si el celular y el computador están en la misma red Wi-Fi y el backend escucha en 0.0.0.0. La nube funciona desde cualquier red.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (changed) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Al guardar se cerrará la sesión actual para usar el nuevo servidor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = ::save) {
                Text(if (changed) "Guardar y conectar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun HintLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(15.dp),
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
