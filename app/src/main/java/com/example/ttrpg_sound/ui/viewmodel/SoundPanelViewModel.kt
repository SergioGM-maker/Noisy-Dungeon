package com.example.ttrpg_sound.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ttrpg_sound.data.model.SoundButton
import com.example.ttrpg_sound.data.model.SoundPanel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado completo de la pantalla principal.
 *
 * Un único objeto atómico → Compose nunca ve panels e index desincronizados.
 * currentPanel se deriva aquí para que siempre sea consistente.
 */
data class UiState(
    val panels: List<SoundPanel> = listOf(
        SoundPanel(name = "General"),
        SoundPanel(name = "Combate")
    ),
    val currentPanelIndex: Int = 0
) {
    val currentPanel: SoundPanel?
        get() = panels.getOrNull(currentPanelIndex)
}

class SoundPanelViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun selectPanel(index: Int) {
        _uiState.update { state ->
            if (state.panels.isEmpty()) return
            state.copy(currentPanelIndex = index.coerceIn(0, state.panels.lastIndex))
        }
    }

    /**
     * Añade un panel SIN cambiar el índice seleccionado.
     *
     * ¿Por qué no navegamos aquí?
     * ScrollableTabRow usa SubcomposeLayout internamente, que tiene dos fases:
     *   Fase 1: compone y MIDE los tabs → rellena tabPositions[]
     *   Fase 2: coloca el indicador en tabPositions[selectedTabIndex]
     *
     * Si cambiamos panels + index en el mismo frame, la Fase 2 intenta leer
     * tabPositions[nuevoIndex] antes de que la Fase 1 lo haya medido → CRASH.
     *
     * La navegación ocurre en HomeScreen mediante LaunchedEffect(panels.size),
     * que se ejecuta DESPUÉS de que el frame ha sido completamente procesado
     * (composición + layout + draw). En ese punto, tabPositions ya incluye
     * el nuevo tab y la navegación es segura.
     */
    fun addPanel(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        _uiState.update { state ->
            state.copy(panels = state.panels + SoundPanel(name = trimmed))
            // currentPanelIndex se deja intacto deliberadamente.
        }
    }

    fun addButton(panelId: String, buttonName: String) {
        val trimmed = buttonName.trim()
        if (trimmed.isBlank()) return

        _uiState.update { state ->
            state.copy(
                panels = state.panels.map { panel ->
                    if (panel.id == panelId) {
                        panel.copy(buttons = panel.buttons + SoundButton(name = trimmed))
                    } else panel
                }
            )
        }
    }

    fun removeButton(panelId: String, buttonId: String) {
        _uiState.update { state ->
            state.copy(
                panels = state.panels.map { panel ->
                    if (panel.id == panelId) {
                        panel.copy(buttons = panel.buttons.filter { it.id != buttonId })
                    } else panel
                }
            )
        }
    }

    fun playSound(button: SoundButton) {
        // TODO: ExoPlayer
    }
}
