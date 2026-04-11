package com.example.ttrpg_sound.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ttrpg_sound.data.local.entity.SoundButtonEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para los botones de sonido.
 *
 * getButtonsForPanel devuelve Flow: cada vez que se añade o elimina
 * un botón del panel X, Room emite la lista actualizada automáticamente.
 */
@Dao
interface SoundButtonDao {

    @Query("SELECT * FROM sound_buttons WHERE panelId = :panelId ORDER BY position ASC")
    fun getButtonsForPanel(panelId: String): Flow<List<SoundButtonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: SoundButtonEntity)

    @Delete
    suspend fun deleteButton(button: SoundButtonEntity)
}
