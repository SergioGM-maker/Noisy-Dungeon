package com.example.ttrpg_sound.data.repository

import com.example.ttrpg_sound.data.local.dao.SoundButtonDao
import com.example.ttrpg_sound.data.local.dao.SoundPanelDao
import com.example.ttrpg_sound.data.local.toDomain
import com.example.ttrpg_sound.data.local.toEntity
import com.example.ttrpg_sound.data.model.SoundButton
import com.example.ttrpg_sound.data.model.SoundPanel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Repositorio: única fuente de verdad de los datos de la app.
 *
 * El ViewModel solo habla con el Repository. No sabe si los datos vienen
 * de Room, de la red, o de donde sea. Esta abstracción hace que cambiar
 * el origen de datos no afecte al ViewModel ni a la UI.
 *
 * --- Cómo funciona el Flow de paneles con botones ---
 *
 * El reto es que Room tiene dos tablas separadas (paneles y botones),
 * pero la UI necesita paneles que ya incluyan sus botones dentro.
 *
 * Lo resolvemos con operadores de Flow:
 *
 *   panelDao.getAllPanels()          → Flow<List<SoundPanelEntity>>
 *       .flatMapLatest { panels ->   → cada vez que cambia la lista de paneles…
 *           combine(                 → …combinamos un Flow por cada panel
 *               panels.map { panel ->
 *                   buttonDao.getButtonsForPanel(panel.id)   → Flow<List<SoundButtonEntity>>
 *               }
 *           ) { buttonLists ->       → cuando cualquier lista de botones cambia…
 *               panels.zip(buttonLists)   → …unimos cada panel con sus botones
 *                   .map { (panel, buttons) -> panel.toDomain(buttons.map { it.toDomain() }) }
 *           }
 *       }
 *
 * El resultado es un único Flow<List<SoundPanel>> que emite automáticamente
 * cuando cambia cualquier panel O cualquier botón de cualquier panel.
 */
class SoundRepository(
    private val panelDao:  SoundPanelDao,
    private val buttonDao: SoundButtonDao
) {

    /**
     * Flow que emite la lista completa de paneles (con sus botones incluidos)
     * cada vez que algo cambia en cualquiera de las dos tablas.
     */
    val panels: Flow<List<SoundPanel>> =
        panelDao.getAllPanels()
            .flatMapLatest { panelEntities ->
                if (panelEntities.isEmpty()) {
                    // Caso especial: sin paneles no hay botones que combinar.
                    // kotlinx.coroutines.flow.flowOf emite un único valor y termina.
                    kotlinx.coroutines.flow.flowOf(emptyList())
                } else {
                    // Un Flow de botones por cada panel
                    val buttonFlows = panelEntities.map { panel ->
                        buttonDao.getButtonsForPanel(panel.id)
                            .map { buttonEntities -> buttonEntities.map { it.toDomain() } }
                    }
                    // combine espera a que todos emitan al menos una vez,
                    // luego emite cada vez que cualquiera de ellos cambia.
                    combine(buttonFlows) { buttonLists ->
                        panelEntities.mapIndexed { i, panelEntity ->
                            panelEntity.toDomain(buttons = buttonLists[i])
                        }
                    }
                }
            }

    // -------------------------------------------------------------------------
    // Operaciones de escritura (todas suspend: se ejecutan en corrutinas)
    // -------------------------------------------------------------------------

    suspend fun addPanel(panel: SoundPanel, position: Int) {
        panelDao.insertPanel(panel.toEntity(position))
    }

    suspend fun deletePanel(panel: SoundPanel, position: Int) {
        panelDao.deletePanel(panel.toEntity(position))
        // Los botones del panel se borran solos por el CASCADE de la ForeignKey
    }

    suspend fun addButton(button: SoundButton, panelId: String, position: Int) {
        buttonDao.insertButton(button.toEntity(panelId, position))
    }

    suspend fun deleteButton(button: SoundButton, panelId: String, position: Int) {
        buttonDao.deleteButton(button.toEntity(panelId, position))
    }

    /** Actualiza la URI de audio de un botón (se usará al implementar selección de archivos). */
    suspend fun updateButtonUri(button: SoundButton, panelId: String, position: Int) {
        buttonDao.insertButton(button.toEntity(panelId, position))
    }
}
