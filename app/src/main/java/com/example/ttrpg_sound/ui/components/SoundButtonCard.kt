package com.example.ttrpg_sound.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
 * Tarjeta cuadrada que representa un botón de sonido.
 *
 * - Pulsación corta → reproduce el sonido ([onClick])
 * - Pulsación larga → muestra un menú contextual con opciones (eliminar, etc.)
 *
 * Este componente es "tonto" (dumb component / stateless): no sabe nada
 * de ViewModels ni de lógica. Solo recibe datos y callbacks.
 * Así es más fácil de reutilizar y de testear en preview.
 *
 * @param button    El modelo de datos del botón.
 * @param onClick   Llamado al pulsar (debería reproducir el sonido).
 * @param onDelete  Llamado al seleccionar "Eliminar" en el menú contextual.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundButtonCard(
    button: SoundButton,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado local: si el menú contextual está visible.
    // 'remember' mantiene este valor entre recomposiciones de este componente.
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .aspectRatio(1f) // Siempre cuadrado, independientemente del tamaño
            .combinedClickable(
                onClick = onClick,
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
            Text(
                text = button.name,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // El DropdownMenu se ancla al Box que lo contiene
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
                // Aquí añadiremos en el futuro: "Renombrar", "Cambiar sonido", etc.
            }
        }
    }
}
