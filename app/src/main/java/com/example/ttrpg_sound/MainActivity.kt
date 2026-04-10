package com.example.ttrpg_sound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ttrpg_sound.ui.screens.HomeScreen
import com.example.ttrpg_sound.ui.theme.TTRPGSoundTheme

/**
 * Punto de entrada de la aplicación.
 *
 * MainActivity es deliberadamente minimalista: solo aplica el tema
 * y lanza HomeScreen. Toda la lógica vive en el ViewModel y en los
 * Composables correspondientes.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TTRPGSoundTheme {
                HomeScreen()
            }
        }
    }
}
