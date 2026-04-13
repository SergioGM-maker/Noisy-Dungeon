package com.example.ttrpg_sound.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

/**
 * Tarjeta de botón de sonido con soporte para modo borrado.
 *
 * Cuando [isDeleteMode] es false → comportamiento normal:
 *   - Pulsación corta  → reproduce el sonido
 *   - Pulsación larga  → menú contextual (cambiar sonido, eliminar)
 *
 * Cuando [isDeleteMode] es true → modo borrado:
 *   - La X aparece en la esquina superior izquierda con animación.
 *   - Pulsación corta sobre la tarjeta → no hace nada (evita reproducir
 *     accidentalmente mientras el usuario intenta borrar).
 *   - Pulsación sobre la X → llama a [onDelete].
 *   - El menú contextual no aparece (pulsación larga desactivada).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundButtonCard(
    button: SoundButton,
    isDeleteMode: Boolean,
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
                onClick = {
                    if (!isDeleteMode) onClick()
                    // En modo borrado, la pulsación sobre la tarjeta no hace nada.
                    // El único punto de interacción es la X.
                },
                onLongClick = {
                    if (!isDeleteMode) menuExpanded = true
                }
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
            // Nombre del botón
            Text(
                text      = button.name,
                style     = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Indicador de audio asignado (esquina superior derecha)
            if (button.soundUri != null) {
                Icon(
                    imageVector        = Icons.Default.PlayArrow,
                    contentDescription = "Tiene audio asignado",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier
                        .size(14.dp)
                        .align(Alignment.TopEnd)
                )
            }

            // X de borrado (esquina superior izquierda)
            // AnimatedVisibility hace que aparezca y desaparezca con un
            // fadeIn/fadeOut suave al entrar y salir del modo borrado.
            androidx.compose.animation.AnimatedVisibility(
                visible = isDeleteMode,
                enter   = fadeIn(),
                exit    = fadeOut(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                IconButton(
                    onClick  = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Eliminar ${button.name}",
                        tint               = MaterialTheme.colorScheme.error,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            // Menú contextual (solo en modo normal)
            DropdownMenu(
                expanded         = menuExpanded && !isDeleteMode,
                onDismissRequest = { menuExpanded = false }
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
