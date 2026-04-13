package com.example.ttrpg_sound.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Diálogo de confirmación antes de borrar un panel.
 *
 * El borrado de un panel es destructivo e irreversible (elimina el panel
 * y todos sus botones por CASCADE en Room, y hace unload de sus soundIds).
 * Un paso de confirmación explícito evita borrados accidentales.
 *
 * @param panelName  Nombre del panel a borrar, mostrado en el mensaje.
 * @param onConfirm  El usuario confirmó el borrado.
 * @param onDismiss  El usuario canceló o cerró el diálogo.
 */
@Composable
fun ConfirmDeletePanelDialog(
    panelName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Borrar panel") },
        text  = {
            Text(
                text  = "¿Estás seguro de que quieres borrar el panel \"$panelName\"? " +
                        "Se eliminarán todos sus botones de sonido.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            // Botón de confirmación en color error para reforzar que es
            // una acción destructiva e irreversible.
            TextButton(onClick = onConfirm) {
                Text(
                    text  = "Borrar",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
