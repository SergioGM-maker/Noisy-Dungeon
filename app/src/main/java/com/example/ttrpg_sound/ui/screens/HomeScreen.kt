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
import androidx.compose.runtime.getValue
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

/**
 * Pantalla principal de la aplicación.
 *
 * Es el único Composable que conoce el ViewModel. Recoge el estado
 * y lo pasa hacia abajo a componentes "tontos" (SoundButtonCard, diálogos).
 * Este patrón se llama "state hoisting" (elevar el estado).
 *
 * Estructura visual:
 * ┌─────────────────────────────┐
 * │  TopAppBar  [+Panel]        │
 * ├─────────────────────────────┤
 * │  Tab1 │ Tab2 │ Tab3 │ ...   │  ← ScrollableTabRow
 * ├─────────────────────────────┤
 * │                             │
 * │  [Btn] [Btn] [Btn]          │  ← LazyVerticalGrid
 * │  [Btn] [Btn]                │
 * │                             │
 * └──────────────────────── [+] ┘  ← FAB añadir botón
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // 'viewModel()' obtiene (o crea) el ViewModel con el ciclo de vida correcto.
    // Al pasarlo como parámetro con valor por defecto, facilitamos los tests:
    // en un test podemos inyectar un ViewModel falso.
    viewModel: SoundPanelViewModel = viewModel()
) {
    // collectAsStateWithLifecycle: solo recolecta el Flow cuando la UI
    // está en primer plano. Más eficiente que collectAsState().
    val panels by viewModel.panels.collectAsStateWithLifecycle()
    val currentPanelIndex by viewModel.currentPanelIndex.collectAsStateWithLifecycle()
    val currentPanel by viewModel.currentPanel.collectAsStateWithLifecycle()

    // Estado local de la UI: si los diálogos están visibles.
    // Esto NO sube al ViewModel porque es puramente visual — no afecta
    // a los datos del modelo.
    var showAddButtonDialog by remember { mutableStateOf(false) }
    var showAddPanelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TTRPG Sound") },
                actions = {
                    IconButton(onClick = { showAddPanelDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir panel"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Solo mostramos el FAB si hay al menos un panel creado
            if (panels.isNotEmpty()) {
                FloatingActionButton(onClick = { showAddButtonDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir botón de sonido"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            // --- Navegación entre paneles ---
            if (panels.isNotEmpty()) {
                ScrollableTabRow(selectedTabIndex = currentPanelIndex) {
                    panels.forEachIndexed { index, panel ->
                        Tab(
                            selected = index == currentPanelIndex,
                            onClick = { viewModel.selectPanel(index) },
                            text = { Text(panel.name) }
                        )
                    }
                }
            }

            // --- Contenido del panel actual ---
            val buttons = currentPanel?.buttons.orEmpty()

            if (buttons.isEmpty()) {
                // Estado vacío: mensaje de ayuda contextual
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
                // LazyVerticalGrid: renderiza solo los botones visibles en pantalla.
                // GridCells.Adaptive: calcula automáticamente cuántas columnas caben
                // según el ancho disponible. Funciona bien en móvil y tablet.
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = buttons,
                        key = { it.id } // 'key' permite a Compose animar y reusar items eficientemente
                    ) { button ->
                        SoundButtonCard(
                            button = button,
                            onClick = { viewModel.playSound(button) },
                            onDelete = {
                                currentPanel?.let { panel ->
                                    viewModel.removeButton(panel.id, button.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Diálogos ---
    // Se renderizan fuera del Scaffold para solaparse correctamente sobre todo

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
