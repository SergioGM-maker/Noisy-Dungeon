package com.example.ttrpg_sound.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Drawer de ajustes deslizante desde la derecha.
 *
 * Estructura:
 * ┌──────────────────────────────────────────┐
 * │  [overlay semitransparente — cierra]  [panel] │
 * └──────────────────────────────────────────┘
 *
 * El overlay ocupa toda la pantalla y cierra el drawer al pulsarlo,
 * igual que el comportamiento estándar de un ModalNavigationDrawer.
 * El panel blanco desliza desde el borde derecho.
 *
 * Las tres secciones de ajustes son dummies por ahora — cada una tiene
 * su propio bloque [SettingsSection] listo para recibir controles reales.
 *
 * @param isVisible  Controla si el drawer está abierto.
 * @param onDismiss  Llamado al pulsar el overlay o el botón de cerrar.
 */
@Composable
fun SettingsDrawer(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    // AnimatedVisibility a nivel de Box raíz: cuando isVisible pasa a false,
    // el drawer entero (overlay + panel) desaparece con animación.
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter   = slideInHorizontally(initialOffsetX = { it }),   // entra desde la derecha
        exit    = slideOutHorizontally(targetOffsetX = { it })    // sale hacia la derecha
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Overlay semitransparente — cierra el drawer al pulsar fuera del panel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss)
            )

            // Panel de ajustes — anclado al borde derecho
            Surface(
                modifier      = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .align(Alignment.CenterEnd),
                color         = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {

                    // Cabecera del drawer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text       = "Ajustes",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector        = Icons.Default.Close,
                                contentDescription = "Cerrar ajustes"
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))

                    // ----------------------------------------------------------
                    // Sección 1: Estilo de esquinas
                    // ----------------------------------------------------------
                    SettingsSection(
                        title       = "Esquinas de botones",
                        description = "Alterna entre esquinas redondeadas y afiladas."
                    ) {
                        // TODO: Switch o SegmentedButton
                        SettingsDummyPlaceholder("Redondeadas / Afiladas")
                    }

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(4.dp))

                    // ----------------------------------------------------------
                    // Sección 2: Esquema de color
                    // ----------------------------------------------------------
                    SettingsSection(
                        title       = "Esquema de color",
                        description = "Cambia los colores de botones, texto y fondo."
                    ) {
                        // TODO: Selector de tema (chips o color picker)
                        SettingsDummyPlaceholder("Por defecto / Oscuro / Rojo / Azul")
                    }

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(4.dp))

                    // ----------------------------------------------------------
                    // Sección 3: Idioma
                    // ----------------------------------------------------------
                    SettingsSection(
                        title       = "Idioma",
                        description = "Cambia el idioma de la interfaz."
                    ) {
                        // TODO: SegmentedButton ES / EN
                        SettingsDummyPlaceholder("Español / English")
                    }
                }
            }
        }
    }
}

/**
 * Bloque visual de una sección de ajustes.
 * Título + descripción muted + slot para el control concreto.
 */
@Composable
private fun SettingsSection(
    title:       String,
    description: String,
    content:     @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}

/**
 * Placeholder visual para los controles aún no implementados.
 * Se elimina cuando se implementa el ajuste real.
 */
@Composable
private fun SettingsDummyPlaceholder(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(horizontal = 0.dp)
    ) {
        Text(
            text  = "[ $label ]",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
