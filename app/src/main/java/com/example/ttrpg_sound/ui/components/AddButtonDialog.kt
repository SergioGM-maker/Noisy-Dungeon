package com.example.ttrpg_sound.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ttrpg_sound.ui.viewmodel.SoundPanelViewModel

/**
 * Diálogo modal para crear un nuevo botón de sonido.
 *
 * El estado del campo de texto ([text]) es local a este diálogo —
 * no necesita subir al ViewModel porque es un detalle de la UI
 * que solo existe mientras el diálogo está abierto.
 *
 * @param onConfirm  Se llama con el nombre introducido si el usuario confirma.
 * @param onDismiss  Se llama si el usuario cancela o cierra el diálogo.
 */
@Composable
fun AddButtonDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo botón de sonido") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nombre del sonido") },
                placeholder = { Text("Ej: Espadas chocando") },
                singleLine = true
            )
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
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
