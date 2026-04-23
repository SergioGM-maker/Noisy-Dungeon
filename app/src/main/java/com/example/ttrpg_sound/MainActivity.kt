package com.example.ttrpg_sound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ttrpg_sound.ui.screens.HomeScreen
import com.example.ttrpg_sound.ui.theme.TTRPGSoundTheme
import com.example.ttrpg_sound.ui.viewmodel.SoundPanelViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SoundPanelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            TTRPGSoundTheme(
                appColorScheme    = uiState.appColorScheme,
                useRoundedCorners = uiState.useRoundedCorners
            ) {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
