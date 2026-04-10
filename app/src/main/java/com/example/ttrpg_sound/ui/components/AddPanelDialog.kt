package com.example.ttrpg_sound.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ttrpg_sound.ui.viewmodel.MAX_PANEL_NAME_LENGTH

/**
 * Diálogo para crear un nuevo panel.
 *
 * Aplica dos restricciones al nombre:
 *  1. No puede estar en blanco (el botón "Crear" permanece desactivado).
 *  2. No puede superar [MAX_PANEL_NAME_LENGTH] caracteres.
 *     - La entrada se corta automáticamente en ese límite.
 *     - Un contador visible informa al usuario de los caracteres restantes.
 *     - El contador se pone rojo cuando se alcanza el máximo.
 */
@Composable
fun AddPanelDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val remaining = MAX_PANEL_NAME_LENGTH - text.length
    val atLimit   = remaining == 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo panel") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        // Cortar silenciosamente si el usuario pega texto más largo
                        if (input.length <= MAX_PANEL_NAME_LENGTH) text = input
                    },
                    label       = { Text("Nombre del panel") },
                    placeholder = { Text("Ej: Taberna, Dungeon…") },
                    singleLine  = true,
                    // Sufijo con contador de caracteres restantes
                    suffix = {
                        Text(
                            text  = "$remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (atLimit) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    isError = atLimit
                )
                // Mensaje de error cuando se ha alcanzado el límite
                if (atLimit) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Máximo $MAX_PANEL_NAME_LENGTH caracteres",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
