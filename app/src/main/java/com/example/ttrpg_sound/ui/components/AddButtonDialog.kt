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
import androidx.compose.ui.res.stringResource
import com.example.ttrpg_sound.R

@Composable
fun AddButtonDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(stringResource(R.string.dialog_new_button_title)) },
        text    = {
            OutlinedTextField(
                value         = text,
                onValueChange = { text = it },
                label         = { Text(stringResource(R.string.dialog_new_button_label)) },
                placeholder   = { Text(stringResource(R.string.dialog_new_button_placeholder)) },
                singleLine    = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text(stringResource(R.string.action_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
