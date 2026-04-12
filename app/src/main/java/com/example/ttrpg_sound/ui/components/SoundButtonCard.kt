package com.example.ttrpg_sound.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ttrpg_sound.data.model.SoundButton

/**
 * Tarjeta de botón de sonido.
 *
 * Cambios respecto a la versión anterior:
 * - Menú contextual ampliado con "Cambiar sonido" → llama a [onChangeAudio].
 * - Indicador visual: un icono de nota musical en la esquina superior derecha
 *   cuando el botón ya tiene un archivo de audio asignado (soundUri != null).
 *   Así el usuario sabe de un vistazo qué botones tienen sonido y cuáles no.
 *
 * @param onChangeAudio  Llamado cuando el usuario pulsa "Cambiar sonido".
 *                       HomeScreen lanzará el picker al recibir este callback.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundButtonCard(
    button: SoundButton,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onChangeAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick     = onClick,
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Nombre del botón, centrado
            Text(
                text      = button.name,
                style     = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Indicador visual: el botón tiene audio asignado
            if (button.soundUri != null) {
                Icon(
                    imageVector        = Icons.Default.Notifications,
                    contentDescription = "Tiene audio asignado",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier
                        .size(14.dp)
                        .align(Alignment.TopEnd)
                )
            }

            DropdownMenu(
                expanded          = menuExpanded,
                onDismissRequest  = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text    = { Text("Cambiar sonido") },
                    onClick = {
                        menuExpanded = false
                        onChangeAudio()
                    }
                )
                DropdownMenuItem(
                    text    = { Text("Eliminar") },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}
