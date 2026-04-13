package com.example.ttrpg_sound.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
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
    val pendingAudioButtonId: String? = null,
    /**
     * Controla si la UI está en modo borrado.
     *
     * Vive en UiState (no como estado local en HomeScreen) por dos razones:
     *  1. El ViewModel necesita conocerlo para gestionar el unload de SoundPool
     *     en el momento exacto del borrado.
     *  2. Si en el futuro añadimos otras pantallas, el modo borrado podría
     *     afectarlas también sin duplicar estado.
     */
    val isDeleteMode: Boolean = false
) {
    val currentPanel: SoundPanel?
        get() = panels.getOrNull(currentPanelIndex)
}

class SoundPanelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SoundRepository(
        panelDao  = AppDatabase.getInstance(application).soundPanelDao(),
        buttonDao = AppDatabase.getInstance(application).soundButtonDao()
    )

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundCache = mutableMapOf<String, Int>()
    private val pendingPlay = mutableMapOf<Int, String>()

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            val buttonId = pendingPlay.remove(soundId) ?: return@setOnLoadCompleteListener
            soundCache[buttonId] = soundId
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }

    // -------------------------------------------------------------------------
    // Estado
    // -------------------------------------------------------------------------

    private val _currentPanelIndex    = MutableStateFlow(0)
    private val _pendingAudioButtonId = MutableStateFlow<String?>(null)
    private val _isDeleteMode         = MutableStateFlow(false)

    val uiState: StateFlow<UiState> =
        combine(
            repository.panels,
            _currentPanelIndex,
            _pendingAudioButtonId,
            _isDeleteMode
        ) { panels, index, pendingId, deleteMode ->
            UiState(
                panels               = panels,
                currentPanelIndex    = index.coerceIn(0, (panels.size - 1).coerceAtLeast(0)),
                pendingAudioButtonId = pendingId,
                isDeleteMode         = deleteMode
            )
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState()
        )

    // -------------------------------------------------------------------------
    // Modo borrado
    // -------------------------------------------------------------------------

    /**
     * Alterna el modo borrado.
     * Si se desactiva mientras el picker de audio estaba pendiente,
     * lo cancelamos para no dejar estado sucio.
     */
    fun toggleDeleteMode() {
        _isDeleteMode.update { current ->
            val entering = !current
            if (!entering) {
                // Al salir del modo borrado, cancelar cualquier picker pendiente
                _pendingAudioButtonId.value = null
            }
            entering
        }
    }

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

    fun addButton(panelId: String, buttonName: String) {
        val trimmed = buttonName.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val position  = uiState.value.currentPanel?.buttons?.size ?: 0
            val newButton = SoundButton(name = trimmed)
            repository.addButton(newButton, panelId, position)
            _pendingAudioButtonId.value = newButton.id
        }
    }

    /**
     * Elimina un botón y libera su soundId de SoundPool.
     * La liberación debe ocurrir antes del borrado en BD para garantizar
     * que el soundId que liberamos es válido.
     */
    fun removeButton(panelId: String, buttonId: String) {
        soundCache.remove(buttonId)?.let { soundId ->
            soundPool.unload(soundId)
        }
        viewModelScope.launch {
            val panel  = uiState.value.panels.find { it.id == panelId } ?: return@launch
            val button = panel.buttons.find { it.id == buttonId }        ?: return@launch
            repository.deleteButton(button, panelId, panel.buttons.indexOf(button))
        }
    }

    // -------------------------------------------------------------------------
    // Reproducción
    // -------------------------------------------------------------------------

    fun playSound(button: SoundButton) {
        val uriString = button.soundUri ?: return

        val cachedSoundId = soundCache[button.id]
        if (cachedSoundId != null) {
            soundPool.play(cachedSoundId, 1f, 1f, 0, 0, 1f)
            return
        }

        val uri = Uri.parse(uriString)
        try {
            val pfd     = getApplication<Application>().contentResolver
                .openFileDescriptor(uri, "r") ?: return
            val soundId = soundPool.load(pfd.fileDescriptor, 0, pfd.statSize, 1)
            pendingPlay[soundId] = button.id
            pfd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // -------------------------------------------------------------------------
    // Selección de archivo de audio
    // -------------------------------------------------------------------------

    fun requestAudioPicker(buttonId: String) {
        _pendingAudioButtonId.value = buttonId
    }

    fun onAudioFileSelected(uri: Uri) {
        val buttonId = _pendingAudioButtonId.value ?: return
        _pendingAudioButtonId.value = null

        getApplication<Application>().contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        soundCache.remove(buttonId)?.let { oldSoundId ->
            soundPool.unload(oldSoundId)
        }

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

    fun onAudioPickerCancelled() {
        _pendingAudioButtonId.value = null
    }

    override fun onCleared() {
        soundPool.release()
        super.onCleared()
    }
}
