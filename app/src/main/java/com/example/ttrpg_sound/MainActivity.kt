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
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: SoundPanelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------------------------------------------------------------------
        // Detección del idioma del sistema en el primer arranque.
        //
        // savedInstanceState es null únicamente la primera vez que la Activity
        // se crea (primer arranque o después de que el proceso muera).
        // En rotaciones de pantalla o recreaciones por cambio de idioma,
        // savedInstanceState no es null → no volvemos a ejecutar este bloque,
        // evitando un bucle infinito de recreaciones.
        //
        // Flujo:
        //   1. Leemos el idioma principal del sistema (Locale.getDefault()).
        //   2. Extraemos solo el código de idioma ISO 639-1 ("es", "en", "fr"…).
        //   3. Lo comparamos con los idiomas soportados (AppLanguage.entries).
        //   4. Si hay coincidencia → usamos ese idioma.
        //      Si no hay coincidencia → usamos ENGLISH como fallback.
        //   5. Llamamos a setLanguage() en el ViewModel y aplicamos el locale
        //      con AppCompatDelegate para que Android cargue el strings.xml
        //      correcto desde el primer frame.
        // ---------------------------------------------------------------------
        if (savedInstanceState == null) {
            val systemTag    = Locale.getDefault().language          // "es", "en", "fr", etc.
            val matchedLang  = AppLanguage.entries
                .firstOrNull { it.tag == systemTag }
                ?: AppLanguage.ENGLISH                               // fallback si no está soportado

            viewModel.setLanguage(matchedLang)

            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(matchedLang.tag)
            )
        }

        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Aplica el locale cuando el usuario lo cambia manualmente en ajustes.
            // LaunchedEffect solo se dispara cuando appLanguage cambia de valor,
            // no en cada recomposición.
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
