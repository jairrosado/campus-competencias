/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/auth/LoginScreen.kt
 * Propósito: Pantalla de acceso y configuración del administrador inicial.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
package com.campus.competencias.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campus.competencias.ui.components.CampusBrand
import com.campus.competencias.ui.components.LightStatusBarIcons
import com.campus.competencias.ui.components.InlineError
import com.campus.competencias.ui.theme.CampusDeep
import com.campus.competencias.ui.theme.CampusGreen
import com.campus.competencias.ui.theme.CampusLime

@Composable
// HU-01 y AN-01 · Formulario de acceso/setup
fun LoginScreen(
    setup: Boolean,
    isSubmitting: Boolean,
    error: String?,
    apiBaseUrl: String,
    onClearError: () -> Unit,
    onChangeServer: () -> Unit,
    onSubmit: (nombre: String, correo: String, password: String, confirmation: String) -> Unit,
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmationVisible by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    LightStatusBarIcons()

    fun updateError() {
        localError = null
        onClearError()
    }

    fun submit() {
        val validation = when {
            setup && nombre.trim().length < 2 -> "Escribe tu nombre completo."
            !correo.trim().matches(EMAIL_PATTERN) -> "Ingresa un correo válido."
            setup && password.length < 10 -> "La contraseña debe tener al menos 10 caracteres."
            !setup && password.isBlank() -> "Ingresa tu contraseña."
            setup && password != confirmation -> "Las contraseñas deben coincidir."
            else -> null
        }
        localError = validation
        if (validation == null) {
            focusManager.clearFocus()
            onSubmit(nombre, correo, password, confirmation)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(CampusDeep, CampusGreen)),
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 52.dp),
        ) {
            Column {
                CampusBrand(light = true)
                Spacer(Modifier.height(30.dp))
                Text(
                    text = "CONECTA. ORGANIZA. COMPITE.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CampusLime,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (setup) {
                        "Tu comunidad\nempieza aquí."
                    } else {
                        "Las grandes ideas\ny las grandes\ncompetencias."
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (setup) {
                        "Crea la cuenta de administrador que dará vida a torneos, hackatones e intercolegiados."
                    } else {
                        "Un punto de encuentro para las instituciones y los equipos que hacen posible cada evento."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                )
                Spacer(Modifier.height(17.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("Intercolegiados", "Torneos", "Hackatones").forEach { label ->
                        HeroPill(label)
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-28).dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
        ) {
            Column(modifier = Modifier.padding(21.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(27.dp),
                    )
                }
                Spacer(Modifier.height(17.dp))
                Text(
                    text = if (setup) "BIENVENIDO A CAMPUS" else "TU ESPACIO DE TRABAJO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (setup) "Comencemos." else "Qué bueno verte.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (setup) {
                        "Crea tu cuenta de administrador para empezar a organizar tu comunidad."
                    } else {
                        "Ingresa con tu cuenta para continuar."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))

                if (setup) {
                    LoginTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            updateError()
                        },
                        label = "Nombre completo",
                        placeholder = "Tu nombre",
                        icon = Icons.Rounded.Person,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    )
                    Spacer(Modifier.height(13.dp))
                }

                LoginTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        updateError()
                    },
                    label = "Correo electrónico",
                    placeholder = "nombre@institucion.edu.co",
                    icon = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                )
                Spacer(Modifier.height(13.dp))

                LoginTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        updateError()
                    },
                    label = "Contraseña",
                    placeholder = if (setup) "Al menos 10 caracteres" else "Tu contraseña",
                    icon = Icons.Rounded.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = if (setup) ImeAction.Next else ImeAction.Done,
                    onNext = {
                        if (setup) focusManager.moveFocus(FocusDirection.Down) else submit()
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                )

                if (setup) {
                    Spacer(Modifier.height(13.dp))
                    LoginTextField(
                        value = confirmation,
                        onValueChange = {
                            confirmation = it
                            updateError()
                        },
                        label = "Confirmar contraseña",
                        placeholder = "Repite tu contraseña",
                        icon = Icons.Rounded.Lock,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        onNext = ::submit,
                        visualTransformation = if (confirmationVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmationVisible = !confirmationVisible }) {
                                Icon(
                                    imageVector = if (confirmationVisible) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                    contentDescription = if (confirmationVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                    )
                }

                val visibleError = localError ?: error
                if (visibleError != null) {
                    Spacer(Modifier.height(14.dp))
                    InlineError(visibleError)
                }

                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = ::submit,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (setup) "Crear mi cuenta" else "Iniciar sesión",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))
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
                        text = "Tu cuenta y tus datos, en un espacio seguro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        TextButton(
            onClick = onChangeServer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Dns,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "Servidor: $apiBaseUrl",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = "Campus · Gestión de competencias",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp, top = 2.dp),
        )
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onNext: () -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            )
        },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
        trailingIcon = trailingIcon,
        singleLine = true,
        shape = RoundedCornerShape(13.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onNext() },
            onDone = { onNext() },
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun HeroPill(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        contentColor = Color.White.copy(alpha = 0.82f),
        shape = CircleShape,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
        )
    }
}

private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
