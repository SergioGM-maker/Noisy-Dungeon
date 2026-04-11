package com.example.ttrpg_sound.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttrpg_sound.data.local.AppDatabase
import com.example.ttrpg_sound.data.model.SoundButton
import com.example.ttrpg_sound.data.model.SoundPanel
import com.example.ttrpg_sound.data.repository.SoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Máximo de caracteres permitido en el nombre de un panel. */
const val MAX_PANEL_NAME_LENGTH = 20

/**
 * Estado de la UI: solo contiene lo que la pantalla necesita saber.
 * Ya no contiene la lista de paneles — eso viene de Room vía Repository.
 * Solo guardamos aquí el índice seleccionado, que es estado de navegación
 * (no un dato persistente; si cierras y abres la app, volver al panel 0 es razonable).
 */
data class UiState(
    val panels: List<SoundPanel> = emptyList(),
    val currentPanelIndex: Int = 0
) {
    val currentPanel: SoundPanel?
        get() = panels.getOrNull(currentPanelIndex)
}

/**
 * ViewModel actualizado para usar Room a través del Repository.
 *
 * Ahora extiende AndroidViewModel (en lugar de ViewModel) porque necesita
 * el Application context para inicializar la base de datos.
 * En el futuro, cuando usemos inyección de dependencias (Hilt),
 * el Repository se inyectará directamente y volveremos a ViewModel normal.
 *
 * --- Cómo funciona el flujo de datos ahora ---
 *
 * Antes:  ViewModel._uiState (MutableStateFlow manual)
 * Ahora:  Room → Repository.panels (Flow) → ViewModel.uiState (StateFlow)
 *
 * El ViewModel ya no guarda la lista de paneles en memoria.
 * Room es la única fuente de verdad. Cada escritura en Room
 * dispara automáticamente una nueva emisión del Flow, que actualiza
 * el UiState, que recompone la UI. El ciclo es completamente reactivo.
 */
class SoundPanelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SoundRepository(
        panelDao  = AppDatabase.getInstance(application).soundPanelDao(),
        buttonDao = AppDatabase.getInstance(application).soundButtonDao()
    )

    // El índice seleccionado sigue siendo estado local (no se persiste en BD).
    private val _currentPanelIndex = MutableStateFlow(0)

    /**
     * UiState construido combinando dos fuentes:
     *  1. repository.panels → Flow de Room (la lista real y persistida)
     *  2. _currentPanelIndex → estado local de navegación
     *
     * 'combine' produce un nuevo UiState cada vez que cualquiera de los dos cambia.
     * 'stateIn' convierte el Flow frío de combine en un StateFlow caliente
     *  que la UI puede colectar con collectAsStateWithLifecycle().
     *
     * SharingStarted.WhileSubscribed(5_000): el Flow activo permanece vivo
     * 5 segundos tras el último suscriptor (tolera rotaciones de pantalla).
     */
    val uiState: StateFlow<UiState> =
        combine(repository.panels, _currentPanelIndex) { panels, index ->
            UiState(
                panels            = panels,
                currentPanelIndex = index.coerceIn(0, (panels.size - 1).coerceAtLeast(0))
            )
        }.stateIn(
            scope            = viewModelScope,
            started          = SharingStarted.WhileSubscribed(5_000),
            initialValue     = UiState()
        )

    // -------------------------------------------------------------------------
    // Acciones
    // -------------------------------------------------------------------------

    fun selectPanel(index: Int) {
        _currentPanelIndex.update { index }
    }

    /**
     * Añade un panel. La escritura es suspend, así que la lanzamos en
     * viewModelScope: una corrutina ligada al ciclo de vida del ViewModel.
     * Si el usuario cierra la app antes de que termine, se cancela sola.
     *
     * Tras la inserción, Room emite la nueva lista por el Flow →
     * combine recalcula UiState → la UI se recompone.
     * No hace falta hacer nada más.
     */
    fun addPanel(name: String) {
        val trimmed = name.trim().take(MAX_PANEL_NAME_LENGTH)
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val currentPanels = uiState.value.panels
            val newPanel = SoundPanel(name = trimmed)
            repository.addPanel(newPanel, position = currentPanels.size)
            // Navegar al nuevo panel DESPUÉS de que Room haya escrito y emitido.
            // El índice que llegará por el Flow tendrá ya el panel nuevo.
            _currentPanelIndex.value = currentPanels.size
        }
    }

    fun addButton(panelId: String, buttonName: String) {
        val trimmed = buttonName.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val currentButtons = uiState.value.currentPanel?.buttons.orEmpty()
            repository.addButton(
                button   = SoundButton(name = trimmed),
                panelId  = panelId,
                position = currentButtons.size
            )
        }
    }

    fun removeButton(panelId: String, buttonId: String) {
        viewModelScope.launch {
            val panel  = uiState.value.panels.find { it.id == panelId } ?: return@launch
            val button = panel.buttons.find { it.id == buttonId } ?: return@launch
            val pos    = panel.buttons.indexOf(button)
            repository.deleteButton(button, panelId, pos)
        }
    }

    fun playSound(button: SoundButton) {
        // TODO: ExoPlayer / SoundPool
    }
}
