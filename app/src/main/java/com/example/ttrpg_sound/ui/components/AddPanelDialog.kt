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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ttrpg_sound.R
import com.example.ttrpg_sound.ui.viewmodel.MAX_PANEL_NAME_LENGTH

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
        title = { Text(stringResource(R.string.dialog_new_panel_title)) },
        text  = {
            Column {
                OutlinedTextField(
                    value         = text,
                    onValueChange = { input ->
                        if (input.length <= MAX_PANEL_NAME_LENGTH) text = input
                    },
                    label         = { Text(stringResource(R.string.dialog_new_panel_label)) },
                    placeholder   = { Text(stringResource(R.string.dialog_new_panel_placeholder)) },
                    singleLine    = true,
                    suffix        = {
                        Text(
                            text  = "$remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (atLimit) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = atLimit
                )
                if (atLimit) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = stringResource(R.string.dialog_max_chars, MAX_PANEL_NAME_LENGTH),
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
