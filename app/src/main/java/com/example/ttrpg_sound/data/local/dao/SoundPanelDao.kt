package com.example.ttrpg_sound.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ttrpg_sound.data.local.entity.SoundPanelEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para los paneles.
 *
 * Una interfaz anotada con @Dao — Room genera la implementación automáticamente
 * en tiempo de compilación. No escribimos SQL a mano para las operaciones
 * habituales; solo cuando necesitamos algo específico (como ORDER BY).
 *
 * Todas las operaciones de escritura son 'suspend':
 * deben ejecutarse en una corrutina (nunca en el hilo principal de la UI).
 *
 * getAllPanels() devuelve Flow<...>:
 * Room emite una nueva lista cada vez que la tabla cambia.
 * El ViewModel la observa; la UI se recompone sola.
 */
@Dao
interface SoundPanelDao {

    /**
     * Observa todos los paneles ordenados por su posición.
     * Flow: se emite automáticamente cada vez que cambia la tabla.
     */
    @Query("SELECT * FROM sound_panels ORDER BY position ASC")
    fun getAllPanels(): Flow<List<SoundPanelEntity>>

    /**
     * Inserta o reemplaza un panel.
     * REPLACE: si ya existe un panel con ese id, lo actualiza en lugar de fallar.
     * Esto nos permite usar la misma función para insertar y para renombrar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPanel(panel: SoundPanelEntity)

    @Delete
    suspend fun deletePanel(panel: SoundPanelEntity)
}
