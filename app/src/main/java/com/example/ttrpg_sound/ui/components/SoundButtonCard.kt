package com.example.ttrpg_sound.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ttrpg_sound.R
import com.example.ttrpg_sound.data.model.SoundButton

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
                onClick     = { if (!isDeleteMode) onClick() },
                onLongClick = { if (!isDeleteMode) menuExpanded = true }
            ),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text      = button.name,
                style     = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Aviso: botón sin audio asignado
            if (button.soundUri == null) {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.cd_no_audio),
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier
                        .size(14.dp)
                        .align(Alignment.TopEnd)
                )
            }

            // X de borrado con animación
            androidx.compose.animation.AnimatedVisibility(
                visible  = isDeleteMode,
                enter    = androidx.compose.animation.fadeIn(),
                exit     = androidx.compose.animation.fadeOut(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                IconButton(
                    onClick  = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_delete_button, button.name),
                        tint               = MaterialTheme.colorScheme.error,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            // Menú contextual
            DropdownMenu(
                expanded         = menuExpanded && !isDeleteMode,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text    = { Text(stringResource(R.string.menu_change_audio)) },
                    onClick = {
                        menuExpanded = false
                        onChangeAudio()
                    }
                )
                DropdownMenuItem(
                    text    = { Text(stringResource(R.string.menu_delete)) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}
