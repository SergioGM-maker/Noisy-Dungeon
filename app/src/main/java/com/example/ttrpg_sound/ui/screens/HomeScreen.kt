package com.example.ttrpg_sound.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import com.example.ttrpg_sound.ui.components.AddButtonDialog
import com.example.ttrpg_sound.ui.components.AddPanelDialog
import com.example.ttrpg_sound.ui.components.SoundButtonCard
import com.example.ttrpg_sound.ui.viewmodel.SoundPanelViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla principal.
 *
 * Cambios respecto a la versión anterior:
 *  - Se elimina ScrollableTabRow (y con él el bug de tabPositions).
 *  - La navegación entre paneles se hace con un ModalNavigationDrawer,
 *    que se abre pulsando el icono ≡ (hamburger) de la TopAppBar.
 *  - El nombre del panel activo se muestra en el título de la TopAppBar.
 *
 * Estructura visual:
 * ┌─────────────────────────────┐
 * │  ≡  Panel actual      [+]   │  ← TopAppBar
 * ├─────────────────────────────┤
 * │  [Btn] [Btn] [Btn]          │
 * │  [Btn] [Btn]                │  ← Grid de botones
 * │                             │
 * └──────────────────────── [+] ┘  ← FAB añadir botón
 *
 * Drawer (aparece desde la izquierda al pulsar ≡):
 * ┌──────────────┐
 * │  Paneles     │
 * │ ─────────── │
 * │ ▶ General   │  ← panel activo (resaltado)
 * │   Combate   │
 * │   Dungeon   │
 * │             │
 * │  [+ Panel]  │
 * └──────────────┘
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: SoundPanelViewModel = viewModel()) {

    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val panels            = uiState.panels
    val currentPanelIndex = uiState.currentPanelIndex
    val currentPanel      = uiState.currentPanel

    // drawerState controla si el drawer está abierto o cerrado.
    // rememberDrawerState persiste el estado durante recomposiciones.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Para abrir/cerrar el drawer necesitamos una corrutina porque la
    // animación de apertura es suspendable (espera a que termine).
    val scope = rememberCoroutineScope()

    var showAddButtonDialog by remember { mutableStateOf(false) }
    var showAddPanelDialog  by remember { mutableStateOf(false) }

    // -------------------------------------------------------------------------
    // ModalNavigationDrawer: el componente raíz que envuelve todo.
    // 'drawerContent' es lo que aparece dentro del drawer al abrirlo.
    // 'content' es el resto de la pantalla (lo de siempre).
    // -------------------------------------------------------------------------
    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            PanelDrawerContent(
                panels            = panels,
                currentPanelIndex = currentPanelIndex,
                onPanelSelected   = { index ->
                    viewModel.selectPanel(index)
                    // Cerrar el drawer tras seleccionar (con animación)
                    scope.launch { drawerState.close() }
                },
                onAddPanelClicked = {
                    scope.launch { drawerState.close() }
                    showAddPanelDialog = true
                }
            )
        }
    ) {
        // Contenido principal (lo que se ve cuando el drawer está cerrado)
        Scaffold(
            topBar = {
                TopAppBar(
                    // Icono hamburger (≡) para abrir el drawer
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector        = Icons.Default.Menu,
                                contentDescription = "Abrir menú de paneles"
                            )
                        }
                    },
                    // Nombre del panel activo como título
                    title = {
                        Text(currentPanel?.name ?: "TTRPG Sound")
                    },
                    actions = {
                        // El botón + de la TopAppBar ya no añade panel (está en el drawer).
                        // Lo reservamos para acciones futuras o lo eliminamos.
                        // Por ahora, lo quitamos para evitar confusión.
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

            val buttons = currentPanel?.buttons.orEmpty()

            if (buttons.isEmpty()) {
                Box(
                    modifier          = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment  = Alignment.Center
                ) {
                    Text(
                        text  = if (panels.isEmpty()) {
                            "Abre el menú ≡ y crea tu primer panel"
                        } else {
                            "Añade sonidos con el botón +"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns                = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding         = PaddingValues(12.dp),
                    horizontalArrangement  = Arrangement.spacedBy(8.dp),
                    verticalArrangement    = Arrangement.spacedBy(8.dp),
                    modifier               = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
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

    // Diálogos (fuera del Scaffold y del Drawer para superponerse a todo)
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

/**
 * Contenido del drawer de navegación de paneles.
 *
 * Se extrae en un Composable propio (separación de responsabilidades):
 * HomeScreen no necesita saber cómo se pinta el drawer, solo qué hace.
 *
 * @param panels            Lista de paneles disponibles.
 * @param currentPanelIndex Índice del panel actualmente activo (para resaltarlo).
 * @param onPanelSelected   Callback cuando el usuario selecciona un panel.
 * @param onAddPanelClicked Callback cuando el usuario pulsa "Nuevo panel".
 */
@Composable
private fun PanelDrawerContent(
    panels:            List<com.example.ttrpg_sound.data.model.SoundPanel>,
    currentPanelIndex: Int,
    onPanelSelected:   (Int) -> Unit,
    onAddPanelClicked: () -> Unit
) {
    // ModalDrawerSheet: el contenedor estándar de Material3 para drawers.
    // Se encarga del ancho, fondo, forma y sombra correctos.
    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))

        Text(
            text     = "Paneles",
            style    = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(8.dp))

        // Lista de paneles existentes
        panels.forEachIndexed { index, panel ->
            NavigationDrawerItem(
                label    = { Text(panel.name) },
                selected = index == currentPanelIndex,
                onClick  = { onPanelSelected(index) },
                // Padding estándar de Material3 para drawer items
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))

        // Botón para añadir un nuevo panel, al fondo del drawer
        NavigationDrawerItem(
            label    = { Text("Nuevo panel") },
            selected = false,
            onClick  = onAddPanelClicked,
            icon     = {
                Icon(Icons.Default.Add, contentDescription = null)
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
