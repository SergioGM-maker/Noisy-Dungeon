package com.example.ttrpg_sound.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ttrpg_sound.ui.components.AddButtonDialog
import com.example.ttrpg_sound.ui.components.AddPanelDialog
import com.example.ttrpg_sound.ui.components.SoundButtonCard
import com.example.ttrpg_sound.ui.viewmodel.SoundPanelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: SoundPanelViewModel = viewModel()) {

    // Un único collect: panels e index llegan siempre juntos, sin estado intermedio.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val panels            = uiState.panels
    val currentPanelIndex = uiState.currentPanelIndex
    val currentPanel      = uiState.currentPanel

    // -------------------------------------------------------------------------
    // Navegación diferida al nuevo panel
    //
    // Problema: ScrollableTabRow mide los tabs en la Fase 1 del frame y coloca
    // el indicador en la Fase 2. Si cambiamos selectedTabIndex al nuevo panel
    // en el MISMO frame en que lo añadimos, la Fase 2 intenta acceder a
    // tabPositions[nuevoIndex] antes de que la Fase 1 lo haya medido → CRASH.
    //
    // Solución: LaunchedEffect(panels.size) se ejecuta DESPUÉS de que el frame
    // completo ha sido procesado (composición + layout + draw). En ese punto,
    // tabPositions ya incluye el nuevo tab y la navegación es segura.
    //
    // prevPanelCount se inicializa con el tamaño actual para que la navegación
    // solo se active cuando el usuario añade un panel (no en la composición
    // inicial, donde panels.size == prevPanelCount).
    // -------------------------------------------------------------------------
    var prevPanelCount by remember { mutableIntStateOf(panels.size) }

    LaunchedEffect(panels.size) {
        if (panels.size > prevPanelCount) {
            // El nuevo panel ya ha sido compuesto y medido en el frame anterior.
            // Ahora es seguro navegar a él.
            viewModel.selectPanel(panels.lastIndex)
        }
        prevPanelCount = panels.size
    }

    // Estado local de la UI: visibilidad de los diálogos.
    var showAddButtonDialog by remember { mutableStateOf(false) }
    var showAddPanelDialog  by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TTRPG Sound") },
                actions = {
                    IconButton(onClick = { showAddPanelDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir panel")
                    }
                }
            )
        },
        floatingActionButton = {
            if (panels.isNotEmpty()) {
                FloatingActionButton(onClick = { showAddButtonDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir botón de sonido")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (panels.isNotEmpty()) {
                ScrollableTabRow(selectedTabIndex = currentPanelIndex) {
                    panels.forEachIndexed { index, panel ->
                        Tab(
                            selected = index == currentPanelIndex,
                            onClick  = { viewModel.selectPanel(index) },
                            text     = { Text(panel.name) }
                        )
                    }
                }
            }

            val buttons = currentPanel?.buttons.orEmpty()

            if (buttons.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (panels.isEmpty()) {
                            "Crea tu primer panel con el botón +"
                        } else {
                            "Añade sonidos con el botón +"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = buttons, key = { it.id }) { button ->
                        SoundButtonCard(
                            button   = button,
                            onClick  = { viewModel.playSound(button) },
                            onDelete = {
                                currentPanel?.let {
                                    viewModel.removeButton(it.id, button.id)
                                }
                            }
                        )
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
