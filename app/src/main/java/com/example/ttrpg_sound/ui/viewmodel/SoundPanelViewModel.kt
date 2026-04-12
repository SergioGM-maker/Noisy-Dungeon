package com.example.ttrpg_sound.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
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

const val MAX_PANEL_NAME_LENGTH = 20

data class UiState(
    val panels: List<SoundPanel> = emptyList(),
    val currentPanelIndex: Int = 0,
    val pendingAudioButtonId: String? = null
) {
    val currentPanel: SoundPanel?
        get() = panels.getOrNull(currentPanelIndex)
}

class SoundPanelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SoundRepository(
        panelDao  = AppDatabase.getInstance(application).soundPanelDao(),
        buttonDao = AppDatabase.getInstance(application).soundButtonDao()
    )

    private val _currentPanelIndex    = MutableStateFlow(0)
    private val _pendingAudioButtonId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> =
        combine(repository.panels, _currentPanelIndex, _pendingAudioButtonId) { panels, index, pendingId ->
            UiState(
                panels               = panels,
                currentPanelIndex    = index.coerceIn(0, (panels.size - 1).coerceAtLeast(0)),
                pendingAudioButtonId = pendingId
            )
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState()
        )

    // -------------------------------------------------------------------------
    // Paneles
    // -------------------------------------------------------------------------

    fun selectPanel(index: Int) {
        _currentPanelIndex.update { index }
    }

    fun addPanel(name: String) {
        val trimmed = name.trim().take(MAX_PANEL_NAME_LENGTH)
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val position = uiState.value.panels.size
            repository.addPanel(SoundPanel(name = trimmed), position)
            _currentPanelIndex.value = position
        }
    }

    // -------------------------------------------------------------------------
    // Botones
    // -------------------------------------------------------------------------

    /**
     * Crea el botón en Room y señaliza que debe abrirse el picker de audio.
     *
     * La secuencia es:
     *   1. Se inserta el botón en la BD (con soundUri = null por ahora).
     *   2. Una vez confirmada la inserción, se pone pendingAudioButtonId
     *      con el ID del nuevo botón.
     *   3. HomeScreen detecta el cambio y lanza el picker automáticamente.
     *   4. Si el usuario elige un archivo → onAudioFileSelected() actualiza la URI.
     *      Si cancela → el botón existe pero sin audio (puede asignarlo después).
     *
     * La razón de insertar ANTES de abrir el picker (en lugar de esperar a la
     * URI para insertar) es que el picker puede tardar tiempo o cancelarse.
     * Así el botón siempre queda creado, y el audio es opcional.
     */
    fun addButton(panelId: String, buttonName: String) {
        val trimmed = buttonName.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val position  = uiState.value.currentPanel?.buttons?.size ?: 0
            val newButton = SoundButton(name = trimmed)

            repository.addButton(
                button   = newButton,
                panelId  = panelId,
                position = position
            )

            // Abre el picker automáticamente para el botón recién creado
            _pendingAudioButtonId.value = newButton.id
        }
    }

    fun removeButton(panelId: String, buttonId: String) {
        viewModelScope.launch {
            val panel  = uiState.value.panels.find { it.id == panelId } ?: return@launch
            val button = panel.buttons.find { it.id == buttonId }        ?: return@launch
            repository.deleteButton(button, panelId, panel.buttons.indexOf(button))
        }
    }

    // -------------------------------------------------------------------------
    // Selección de archivo de audio
    // -------------------------------------------------------------------------

    /** Llamado desde el menú contextual ("Cambiar sonido") para reasignar audio. */
    fun requestAudioPicker(buttonId: String) {
        _pendingAudioButtonId.value = buttonId
    }

    /** El usuario eligió un archivo. Persiste el permiso y guarda la URI. */
    fun onAudioFileSelected(uri: Uri) {
        val buttonId = _pendingAudioButtonId.value ?: return
        _pendingAudioButtonId.value = null

        getApplication<Application>().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        viewModelScope.launch {
            val panel  = uiState.value.panels
                .find { p -> p.buttons.any { it.id == buttonId } } ?: return@launch
            val button = panel.buttons.find { it.id == buttonId }   ?: return@launch

            repository.updateButtonUri(
                button   = button.copy(soundUri = uri.toString()),
                panelId  = panel.id,
                position = panel.buttons.indexOf(button)
            )
        }
    }

    /** El usuario canceló el picker. El botón queda creado pero sin audio. */
    fun onAudioPickerCancelled() {
        _pendingAudioButtonId.value = null
    }

    // -------------------------------------------------------------------------
    // Reproducción
    // -------------------------------------------------------------------------

    fun playSound(button: SoundButton) {
        // TODO: ExoPlayer / SoundPool
    }
}
