package com.example.ttrpg_sound.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttrpg_sound.R
import com.example.ttrpg_sound.data.local.AppDatabase
import com.example.ttrpg_sound.data.model.SoundButton
import com.example.ttrpg_sound.data.model.SoundPanel
import com.example.ttrpg_sound.data.repository.SoundRepository
import com.example.ttrpg_sound.ui.theme.AppColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_PANEL_NAME_LENGTH = 20

/**
 * Idiomas disponibles.
 *
 * Vive en el ViewModel (no en Theme) porque es lógica de comportamiento,
 * no de apariencia visual. AppColorScheme vive en Theme porque sí es
 * un concepto de tema.
 *
 * [nameRes] → string en strings.xml, NO se traduce (cada idioma aparece
 *             siempre en su propio nombre: "Español", "English").
 * [tag]     → etiqueta BCP-47 para AppCompatDelegate.setApplicationLocales().
 */
enum class AppLanguage(@StringRes val nameRes: Int, val tag: String) {
    SPANISH(R.string.lang_spanish, "es"),
    ENGLISH(R.string.lang_english, "en")
}

data class UiState(
    val panels: List<SoundPanel> = emptyList(),
    val currentPanelIndex: Int = 0,
    val pendingAudioButtonId: String? = null,
    val isDeleteMode: Boolean = false,
    val isLoadingSounds: Boolean = false,
    val useRoundedCorners: Boolean = true,
    // AppColorScheme importado desde ui.theme — fuente única de verdad
    val appColorScheme: AppColorScheme = AppColorScheme.DEFAULT,
    val appLanguage: AppLanguage = AppLanguage.SPANISH
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

    private val soundCache     = mutableMapOf<String, Int>()
    private val pendingPlay    = mutableMapOf<Int, String>()
    private val pendingPreload = mutableMapOf<Int, String>()
    private var preloadPending = 0

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            when {
                pendingPlay.containsKey(soundId) -> {
                    val buttonId = pendingPlay.remove(soundId) ?: return@setOnLoadCompleteListener
                    if (status == 0) {
                        soundCache[buttonId] = soundId
                        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                    }
                }
                pendingPreload.containsKey(soundId) -> {
                    val buttonId = pendingPreload.remove(soundId) ?: return@setOnLoadCompleteListener
                    if (status == 0) soundCache[buttonId] = soundId
                    preloadPending = (preloadPending - 1).coerceAtLeast(0)
                    if (preloadPending == 0) _isLoadingSounds.value = false
                }
            }
        }
        viewModelScope.launch {
            val initialPanels = repository.panels.first()
            if (initialPanels.isNotEmpty()) preloadPanel(initialPanels[0])
        }
    }

    // -------------------------------------------------------------------------
    // Estado
    // -------------------------------------------------------------------------

    private val _currentPanelIndex    = MutableStateFlow(0)
    private val _pendingAudioButtonId = MutableStateFlow<String?>(null)
    private val _isDeleteMode         = MutableStateFlow(false)
    private val _isLoadingSounds      = MutableStateFlow(false)
    private val _useRoundedCorners    = MutableStateFlow(true)
    private val _appColorScheme       = MutableStateFlow(AppColorScheme.DEFAULT)
    private val _appLanguage          = MutableStateFlow(AppLanguage.SPANISH)

    val uiState: StateFlow<UiState> =
        combine(
            repository.panels,
            _currentPanelIndex,
            _pendingAudioButtonId,
            _isDeleteMode,
            combine(_isLoadingSounds, _useRoundedCorners, _appColorScheme, _appLanguage) {
                loading, rounded, scheme, lang ->
                object {
                    val loading  = loading
                    val rounded  = rounded
                    val scheme   = scheme
                    val language = lang
                }
            }
        ) { panels, index, pendingId, deleteMode, prefs ->
            UiState(
                panels               = panels,
                currentPanelIndex    = index.coerceIn(0, (panels.size - 1).coerceAtLeast(0)),
                pendingAudioButtonId = pendingId,
                isDeleteMode         = deleteMode,
                isLoadingSounds      = prefs.loading,
                useRoundedCorners    = prefs.rounded,
                appColorScheme       = prefs.scheme,
                appLanguage          = prefs.language
            )
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState()
        )

    // -------------------------------------------------------------------------
    // Ajustes
    // -------------------------------------------------------------------------

    fun toggleCornerStyle()                    { _useRoundedCorners.update { !it } }
    fun setColorScheme(s: AppColorScheme)      { _appColorScheme.value = s }
    fun setLanguage(l: AppLanguage)            { _appLanguage.value = l }

    // -------------------------------------------------------------------------
    // Precarga
    // -------------------------------------------------------------------------

    private fun preloadPanel(panel: SoundPanel) {
        val toLoad = panel.buttons.filter {
            it.soundUri != null && !soundCache.containsKey(it.id)
        }
        if (toLoad.isEmpty()) { _isLoadingSounds.value = false; return }

        _isLoadingSounds.value = true
        preloadPending = toLoad.size
        val context = getApplication<Application>()

        toLoad.forEach { button ->
            val uri = Uri.parse(button.soundUri)
            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd == null) {
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

    fun selectPanel(index: Int) {
        _currentPanelIndex.update { index }
        preloadPanel(uiState.value.panels.getOrNull(index) ?: return)
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

    fun deletePanel(panelId: String) {
        viewModelScope.launch {
            val panel = uiState.value.panels.find { it.id == panelId } ?: return@launch
            panel.buttons.forEach { button ->
                soundCache.remove(button.id)?.let { soundPool.unload(it) }
                pendingPreload.entries.removeIf { it.value == button.id }
            }
            val position     = uiState.value.panels.indexOf(panel)
            val currentIndex = _currentPanelIndex.value
            repository.deletePanel(panel, position)
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
        val uriString     = button.soundUri ?: return
        val cachedSoundId = soundCache[button.id]
        if (cachedSoundId != null) {
            soundPool.play(cachedSoundId, 1f, 1f, 0, 0, 1f)
            return
        }
        try {
            val pfd     = getApplication<Application>().contentResolver
                .openFileDescriptor(Uri.parse(uriString), "r") ?: return
            val soundId = soundPool.load(pfd.fileDescriptor, 0, pfd.statSize, 1)
            pendingPlay[soundId] = button.id
            pfd.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // -------------------------------------------------------------------------
    // Selección de audio
    // -------------------------------------------------------------------------

    fun requestAudioPicker(buttonId: String)  { _pendingAudioButtonId.value = buttonId }

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

    fun onAudioPickerCancelled() { _pendingAudioButtonId.value = null }

    override fun onCleared() { soundPool.release(); super.onCleared() }
}
