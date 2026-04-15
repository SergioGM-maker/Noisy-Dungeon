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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_PANEL_NAME_LENGTH = 20

data class UiState(
    val panels: List<SoundPanel> = emptyList(),
    val currentPanelIndex: Int = 0,
    val pendingAudioButtonId: String? = null,
    val isDeleteMode: Boolean = false,
    /**
     * True mientras SoundPool está cargando los sonidos del panel activo.
     * HomeScreen muestra un overlay de carga cuando este flag está activo.
     */
    val isLoadingSounds: Boolean = false
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

    private val soundCache   = mutableMapOf<String, Int>()  // buttonId → soundId
    private val pendingPlay  = mutableMapOf<Int, String>()  // soundId → buttonId (reproducir al cargar)
    private val pendingPreload = mutableMapOf<Int, String>() // soundId → buttonId (solo cachear)

    // Contador de sonidos que quedan por completar en la precarga actual.
    // Cuando llega a 0, isLoadingSounds pasa a false.
    private var preloadPending = 0

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, status ->

            when {
                // --- Caso 1: carga iniciada por playSound() ---
                // El usuario pulsó el botón mientras el sonido aún no estaba en caché.
                // Reproducir inmediatamente al terminar.
                pendingPlay.containsKey(soundId) -> {
                    val buttonId = pendingPlay.remove(soundId) ?: return@setOnLoadCompleteListener
                    if (status == 0) {
                        soundCache[buttonId] = soundId
                        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                    }
                }

                // --- Caso 2: carga iniciada por preloadPanel() ---
                // Solo guardamos en caché. Decrementamos el contador y, cuando
                // llega a 0, ocultamos el overlay de carga.
                pendingPreload.containsKey(soundId) -> {
                    val buttonId = pendingPreload.remove(soundId) ?: return@setOnLoadCompleteListener
                    if (status == 0) soundCache[buttonId] = soundId

                    preloadPending = (preloadPending - 1).coerceAtLeast(0)
                    if (preloadPending == 0) _isLoadingSounds.value = false
                }
            }
        }

        // Precarga del panel inicial en cuanto Room emite los primeros datos.
        // 'first()' recoge una sola emisión y cancela la suscripción —
        // no necesitamos seguir observando aquí, selectPanel() se encargará
        // de las precargas siguientes.
        viewModelScope.launch {
            val initialPanels = repository.panels.first()
            if (initialPanels.isNotEmpty()) {
                preloadPanel(initialPanels[0])
            }
        }
    }

    // -------------------------------------------------------------------------
    // Estado
    // -------------------------------------------------------------------------

    private val _currentPanelIndex    = MutableStateFlow(0)
    private val _pendingAudioButtonId = MutableStateFlow<String?>(null)
    private val _isDeleteMode         = MutableStateFlow(false)
    private val _isLoadingSounds      = MutableStateFlow(false)

    val uiState: StateFlow<UiState> =
        combine(
            repository.panels,
            _currentPanelIndex,
            _pendingAudioButtonId,
            _isDeleteMode,
            _isLoadingSounds
        ) { panels, index, pendingId, deleteMode, loadingSounds ->
            UiState(
                panels               = panels,
                currentPanelIndex    = index.coerceIn(0, (panels.size - 1).coerceAtLeast(0)),
                pendingAudioButtonId = pendingId,
                isDeleteMode         = deleteMode,
                isLoadingSounds      = loadingSounds
            )
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState()
        )

    // -------------------------------------------------------------------------
    // Precarga de sonidos
    // -------------------------------------------------------------------------

    /**
     * Carga en SoundPool todos los sonidos del [panel] que aún no estén en caché.
     *
     * La carga es asíncrona: SoundPool llama a OnLoadCompleteListener por cada
     * sonido cuando termina. El contador [preloadPending] rastrea cuántos quedan;
     * cuando llega a 0, [_isLoadingSounds] pasa a false y el overlay desaparece.
     *
     * Sonidos ya en caché se saltan — no tiene sentido recargar lo que ya está
     * en memoria, y hacerlo crearía soundIds duplicados sin referencia.
     */
    private fun preloadPanel(panel: SoundPanel) {
        val toLoad = panel.buttons.filter { button ->
            button.soundUri != null && !soundCache.containsKey(button.id)
        }

        if (toLoad.isEmpty()) {
            // Todos los sonidos ya están en caché (o el panel no tiene sonidos).
            // No hay nada que cargar — ocultar el overlay inmediatamente.
            _isLoadingSounds.value = false
            return
        }

        _isLoadingSounds.value = true
        preloadPending = toLoad.size

        val context = getApplication<Application>()

        toLoad.forEach { button ->
            val uri = Uri.parse(button.soundUri)
            try {
                val pfd     = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd == null) {
                    // El archivo ya no existe (el usuario lo eliminó del dispositivo).
                    // Decrementamos el contador para no bloquear el overlay indefinidamente.
                    preloadPending = (preloadPending - 1).coerceAtLeast(0)
                    if (preloadPending == 0) _isLoadingSounds.value = false
                    return@forEach
                }
                val soundId = soundPool.load(pfd.fileDescriptor, 0, pfd.statSize, 1)
                pendingPreload[soundId] = button.id
                pfd.close()
            } catch (e: Exception) {
                e.printStackTrace()
                preloadPending = (preloadPending - 1).coerceAtLeast(0)
                if (preloadPending == 0) _isLoadingSounds.value = false
            }
        }
    }

    // -------------------------------------------------------------------------
    // Paneles
    // -------------------------------------------------------------------------

    fun toggleDeleteMode() {
        _isDeleteMode.update { current ->
            if (current) _pendingAudioButtonId.value = null
            !current
        }
    }

    /**
     * Cambia el panel activo y precarga sus sonidos.
     * El overlay de carga aparece inmediatamente y desaparece cuando todos
     * los sonidos del nuevo panel están listos en memoria.
     */
    fun selectPanel(index: Int) {
        _currentPanelIndex.update { index }
        val panel = uiState.value.panels.getOrNull(index) ?: return
        preloadPanel(panel)
    }

    fun addPanel(name: String) {
        val trimmed = name.trim().take(MAX_PANEL_NAME_LENGTH)
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val position = uiState.value.panels.size
            repository.addPanel(SoundPanel(name = trimmed), position)
            _currentPanelIndex.value = position
            // Panel nuevo: sin botones, nada que precargar
        }
    }

    fun deletePanel(panelId: String) {
        viewModelScope.launch {
            val panel = uiState.value.panels.find { it.id == panelId } ?: return@launch
            panel.buttons.forEach { button ->
                soundCache.remove(button.id)?.let { soundPool.unload(it) }
                pendingPreload.entries.removeIf { it.value == button.id }
            }
            val position = uiState.value.panels.indexOf(panel)
            repository.deletePanel(panel, position)
            val currentIndex = _currentPanelIndex.value
            if (currentIndex >= position) {
                _currentPanelIndex.value = (currentIndex - 1).coerceAtLeast(0)
            }
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

    fun removeButton(panelId: String, buttonId: String) {
        soundCache.remove(buttonId)?.let { soundPool.unload(it) }
        pendingPreload.entries.removeIf { it.value == buttonId }
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

        // No está en caché: puede que la precarga aún no haya terminado,
        // o que el botón acabe de recibir una URI nueva. Cargamos con
        // intención de reproducir al terminar (pendingPlay, no pendingPreload).
        val uri = Uri.parse(uriString)
        try {
            val pfd     = getApplication<Application>().contentResolver
                .openFileDescriptor(uri, "r") ?: return
            val soundId = soundPool.load(pfd.fileDescriptor, 0, pfd.statSize, 1)
            pendingPlay[soundId] = button.id   // reproducir al cargar
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
        soundCache.remove(buttonId)?.let { soundPool.unload(it) }
        pendingPreload.entries.removeIf { it.value == buttonId }
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
