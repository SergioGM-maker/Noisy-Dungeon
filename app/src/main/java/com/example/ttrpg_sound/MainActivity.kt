package com.example.ttrpg_sound

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ttrpg_sound.ui.screens.HomeScreen
import com.example.ttrpg_sound.ui.theme.TTRPGSoundTheme
import com.example.ttrpg_sound.ui.viewmodel.AppLanguage
import com.example.ttrpg_sound.ui.viewmodel.SoundPanelViewModel

/**
 * MainActivity ahora extiende AppCompatActivity en lugar de ComponentActivity.
 *
 * El cambio es necesario para usar AppCompatDelegate.setApplicationLocales(),
 * que es la API recomendada para cambiar el idioma en tiempo de ejecución
 * con compatibilidad hasta API 24.
 *
 * Cuando setApplicationLocales() recibe una nueva etiqueta de idioma,
 * Android recrea automáticamente la Activity. El ViewModel sobrevive a la
 * recreación (está retenido por el ViewModelStore), así que el idioma
 * seleccionado en UiState persiste y el LaunchedEffect no vuelve a disparar
 * el cambio (el tag ya está aplicado).
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: SoundPanelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Aplica el idioma cuando cambia el estado.
            // LaunchedEffect(uiState.appLanguage) garantiza que solo se ejecuta
            // cuando el idioma cambia, no en cada recomposición.
            // Si el locale ya está aplicado, setApplicationLocales es idempotente
            // y no provoca una recreación innecesaria.
            LaunchedEffect(uiState.appLanguage) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(uiState.appLanguage.tag)
                )
            }

            TTRPGSoundTheme(
                appColorScheme    = uiState.appColorScheme,
                useRoundedCorners = uiState.useRoundedCorners
            ) {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
