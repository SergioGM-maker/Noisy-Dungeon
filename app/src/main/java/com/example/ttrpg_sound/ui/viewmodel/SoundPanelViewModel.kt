package com.example.ttrpg_sound.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ttrpg_sound.data.model.SoundButton
import com.example.ttrpg_sound.data.model.SoundPanel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Máximo de caracteres permitido en el nombre de un panel. */
const val MAX_PANEL_NAME_LENGTH = 20

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
     * El nombre se trunca a [MAX_PANEL_NAME_LENGTH] como segunda línea de defensa
     * (la primera es la UI, que ya limita la entrada del usuario).
     */
    fun addPanel(name: String) {
        val trimmed = name.trim().take(MAX_PANEL_NAME_LENGTH)
        if (trimmed.isBlank()) return

        _uiState.update { state ->
            state.copy(panels = state.panels + SoundPanel(name = trimmed))
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
