package com.example.ttrpg_sound.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ttrpg_sound.data.model.SoundPanel
import com.example.ttrpg_sound.ui.components.AddButtonDialog
import com.example.ttrpg_sound.ui.components.AddPanelDialog
import com.example.ttrpg_sound.ui.components.ConfirmDeletePanelDialog
import com.example.ttrpg_sound.ui.components.SoundButtonCard
import com.example.ttrpg_sound.ui.viewmodel.SoundPanelViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: SoundPanelViewModel = viewModel()) {

    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val panels            = uiState.panels
    val currentPanelIndex = uiState.currentPanelIndex
    val currentPanel      = uiState.currentPanel
    val isDeleteMode      = uiState.isDeleteMode
    val isLoadingSounds   = uiState.isLoadingSounds

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    var showAddButtonDialog by remember { mutableStateOf(false) }
    var showAddPanelDialog  by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onAudioFileSelected(uri)
        else             viewModel.onAudioPickerCancelled()
    }

    LaunchedEffect(uiState.pendingAudioButtonId) {
        if (uiState.pendingAudioButtonId != null) {
            audioPickerLauncher.launch(arrayOf("audio/*"))
        }
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            PanelDrawerContent(
                panels            = panels,
                currentPanelIndex = currentPanelIndex,
                drawerState       = drawerState,
                onPanelSelected   = { index ->
                    viewModel.selectPanel(index)
                    scope.launch { drawerState.close() }
                },
                onPanelDeleted    = { panelId -> viewModel.deletePanel(panelId) },
                onAddPanelClicked = {
                    scope.launch { drawerState.close() }
                    showAddPanelDialog = true
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú de paneles")
                        }
                    },
                    title = { Text(currentPanel?.name ?: "TTRPG Sound") }
                )
            },
            floatingActionButton = {
                if (panels.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FloatingActionButton(
                            onClick        = { viewModel.toggleDeleteMode() },
                            containerColor = if (isDeleteMode) MaterialTheme.colorScheme.errorContainer
                                            else FloatingActionButtonDefaults.containerColor
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Delete,
                                contentDescription = if (isDeleteMode) "Salir del modo borrado"
                                                     else "Activar modo borrado",
                                tint = if (isDeleteMode) MaterialTheme.colorScheme.onErrorContainer
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        FloatingActionButton(
                            onClick        = { if (!isDeleteMode) showAddButtonDialog = true },
                            containerColor = if (isDeleteMode) MaterialTheme.colorScheme.surfaceVariant
                                            else FloatingActionButtonDefaults.containerColor
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Add,
                                contentDescription = "Añadir botón de sonido",
                                tint = if (isDeleteMode) MaterialTheme.colorScheme.onSurfaceVariant
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->

            // ----------------------------------------------------------------
            // Contenido principal envuelto en Box para poder superponer
            // el overlay de carga encima sin desplazar el grid.
            // ----------------------------------------------------------------
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

                val buttons = currentPanel?.buttons.orEmpty()

                if (buttons.isEmpty() && !isLoadingSounds) {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = if (panels.isEmpty()) "Abre el menú ≡ y crea tu primer panel"
                                    else "Añade sonidos con el botón +",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // LazyVerticalGrid es scrolleable por defecto —
                    // no necesita ningún modificador adicional.
                    LazyVerticalGrid(
                        columns               = GridCells.Adaptive(minSize = 110.dp),
                        contentPadding        = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.fillMaxSize()
                    ) {
                        items(items = buttons, key = { it.id }) { button ->
                            SoundButtonCard(
                                button        = button,
                                isDeleteMode  = isDeleteMode,
                                onClick       = { viewModel.playSound(button) },
                                onDelete      = {
                                    currentPanel?.let { viewModel.removeButton(it.id, button.id) }
                                },
                                onChangeAudio = { viewModel.requestAudioPicker(button.id) }
                            )
                        }
                    }
                }

                // ------------------------------------------------------------
                // Overlay de carga
                //
                // Surface semitransparente sobre el grid que bloquea la
                // interacción mientras los sonidos no están listos.
                // Se sitúa después del grid en el árbol de composición →
                // se renderiza encima de él (orden Z natural en Box).
                //
                // El alpha 0.6f deja ver el contenido bajo el overlay,
                // indicando que hay contenido pero aún no está disponible.
                // ------------------------------------------------------------
                if (isLoadingSounds) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color    = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    ) {
                        Column(
                            modifier            = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text  = "Cargando sonidos…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddButtonDialog) {
        currentPanel?.let { panel ->
            AddButtonDialog(
                onConfirm = { name ->
                    viewModel.addButton(panel.id, name)
                    showAddButtonDialog = false
                },
                onDismiss = { showAddButtonDialog = false }
            )
        }
    }

    if (showAddPanelDialog) {
        AddPanelDialog(
            onConfirm = { name ->
                viewModel.addPanel(name)
                showAddPanelDialog = false
            },
            onDismiss = { showAddPanelDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PanelDrawerContent(
    panels:            List<SoundPanel>,
    currentPanelIndex: Int,
    drawerState:       DrawerState,
    onPanelSelected:   (Int) -> Unit,
    onPanelDeleted:    (String) -> Unit,
    onAddPanelClicked: () -> Unit
) {
    var isPanelDeleteMode by remember { mutableStateOf(false) }
    var panelToDelete     by remember { mutableStateOf<SoundPanel?>(null) }

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            isPanelDeleteMode = false
            panelToDelete     = null
        }
    }

    ModalDrawerSheet {
        // ----------------------------------------------------------------
        // Column con verticalScroll: el drawer es scrolleable cuando hay
        // más paneles de los que caben en pantalla.
        //
        // rememberScrollState() mantiene la posición de scroll entre
        // recomposiciones, por lo que no salta al inicio al añadir paneles.
        // ----------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            Text(
                text       = "Paneles",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            panels.forEachIndexed { index, panel ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                ) {
                    NavigationDrawerItem(
                        label    = { Text(panel.name) },
                        selected = index == currentPanelIndex,
                        onClick  = { onPanelSelected(index) },
                        modifier = Modifier.weight(1f)
                    )

                    if (isPanelDeleteMode) {
                        IconButton(
                            onClick  = { panelToDelete = panel },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Close,
                                contentDescription = "Borrar panel ${panel.name}",
                                tint               = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            NavigationDrawerItem(
                label    = { Text("Nuevo panel") },
                selected = false,
                onClick  = onAddPanelClicked,
                icon     = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label    = {
                    Text(
                        text  = if (isPanelDeleteMode) "Cancelar borrado" else "Borrar paneles",
                        color = if (isPanelDeleteMode) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = isPanelDeleteMode,
                onClick  = { isPanelDeleteMode = !isPanelDeleteMode },
                icon     = {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (isPanelDeleteMode) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors   = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }

    panelToDelete?.let { panel ->
        ConfirmDeletePanelDialog(
            panelName = panel.name,
            onConfirm = {
                onPanelDeleted(panel.id)
                panelToDelete = null
                if (panels.size <= 1) isPanelDeleteMode = false
            },
            onDismiss = { panelToDelete = null }
        )
    }
}
