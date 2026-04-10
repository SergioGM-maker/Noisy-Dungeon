package com.example.ttrpg_sound.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ttrpg_sound.data.model.SoundButton
import com.example.ttrpg_sound.data.model.SoundPanel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope

/**
 * ViewModel central de la app. Gestiona todos los paneles y sus botones.
 *
 * --- ¿Qué es un ViewModel? ---
 * Es un objeto que sobrevive a las rotaciones de pantalla y cambios de
 * configuración. Si no lo usáramos y guardáramos el estado directamente
 * en un Composable con 'remember', al girar el móvil perderíamos todos
 * los paneles y botones que el usuario ha creado.
 *
 * --- StateFlow vs MutableStateFlow ---
 * MutableStateFlow: el ViewModel puede escribir en él (privado).
 * StateFlow:        la UI solo puede leerlo (público, expuesto como asStateFlow()).
 * Esto es encapsulación: la UI nunca modifica el estado directamente,
 * solo llama a funciones del ViewModel.
 */
class SoundPanelViewModel : ViewModel() {

    // -------------------------------------------------------------------------
    // Estado privado (mutable, solo accesible dentro del ViewModel)
    // -------------------------------------------------------------------------

    private val _panels = MutableStateFlow(
        listOf(
            SoundPanel(name = "General"),
            SoundPanel(name = "Combate")
        )
    )

    private val _currentPanelIndex = MutableStateFlow(0)

    // -------------------------------------------------------------------------
    // Estado público (inmutable, expuesto a la UI)
    // -------------------------------------------------------------------------

    val panels: StateFlow<List<SoundPanel>> = _panels.asStateFlow()

    val currentPanelIndex: StateFlow<Int> = _currentPanelIndex.asStateFlow()

    /**
     * El panel actualmente seleccionado, derivado de los dos flows anteriores.
     *
     * 'combine' fusiona dos flows: cada vez que cambia cualquiera de los dos,
     * recalcula el resultado. La UI solo necesita observar este StateFlow,
     * no tiene que hacer la lógica de indexación ella misma.
     */
    val currentPanel: StateFlow<SoundPanel?> =
        combine(_panels, _currentPanelIndex) { panels, index ->
            panels.getOrNull(index)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = _panels.value.getOrNull(0)
        )

    // -------------------------------------------------------------------------
    // Acciones (funciones que la UI puede llamar)
    // -------------------------------------------------------------------------

    /** Cambia el panel visible. */
    fun selectPanel(index: Int) {
        _currentPanelIndex.value = index.coerceIn(0, _panels.value.lastIndex)
    }

    /** Crea un nuevo panel vacío y navega a él. */
    fun addPanel(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        _panels.update { currentList -> currentList + SoundPanel(name = trimmed) }
        // Navegar automáticamente al panel recién creado
        selectPanel(_panels.value.lastIndex)
    }

    /**
     * Añade un botón de sonido al panel indicado por [panelId].
     *
     * Usamos .update { } + .map { } para transformar la lista de forma
     * inmutable: no mutamos el objeto existente, creamos uno nuevo con .copy().
     * Esto es el patrón correcto con StateFlow + data classes.
     */
    fun addButton(panelId: String, buttonName: String) {
        val trimmed = buttonName.trim()
        if (trimmed.isBlank()) return

        _panels.update { panels ->
            panels.map { panel ->
                if (panel.id == panelId) {
                    panel.copy(buttons = panel.buttons + SoundButton(name = trimmed))
                } else {
                    panel
                }
            }
        }
    }

    /** Elimina un botón concreto de un panel. */
    fun removeButton(panelId: String, buttonId: String) {
        _panels.update { panels ->
            panels.map { panel ->
                if (panel.id == panelId) {
                    panel.copy(buttons = panel.buttons.filter { it.id != buttonId })
                } else {
                    panel
                }
            }
        }
    }

    /**
     * Reproduce el sonido asociado a [button].
     *
     * Por ahora es un stub (función vacía). En el siguiente paso
     * implementaremos aquí MediaPlayer o ExoPlayer.
     * La UI ya llama a esta función — cuando implementemos el audio,
     * no habrá que tocar nada en los Composables.
     */
    fun playSound(button: SoundButton) {
        // TODO: implementar reproducción de audio (MediaPlayer / ExoPlayer)
        // El parámetro button.soundUri contendrá la ruta al archivo
    }
}
