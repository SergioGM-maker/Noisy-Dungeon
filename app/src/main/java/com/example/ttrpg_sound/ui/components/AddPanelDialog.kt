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

/**
 * Diálogo modal para crear un nuevo panel (pestaña).
 *
 * Idéntico en estructura a [AddButtonDialog] — si en el futuro
 * estos diálogos crecen y divergen, cada uno evoluciona de forma
 * independiente sin afectarse mutuamente.
 *
 * @param onConfirm  Se llama con el nombre del panel si el usuario confirma.
 * @param onDismiss  Se llama si el usuario cancela.
 */
@Composable
fun AddPanelDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo panel") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nombre del panel") },
                placeholder = { Text("Ej: Taberna, Dungeon, Combate...") },
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
