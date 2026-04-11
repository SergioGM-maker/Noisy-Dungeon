package com.example.ttrpg_sound.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un botón de sonido en la base de datos.
 *
 * La anotación @ForeignKey establece la relación con la tabla de paneles:
 *  - "este botón pertenece a un panel"
 *  - onDelete = CASCADE: si se borra el panel, se borran todos sus botones.
 *    Sin esto, quedarían botones huérfanos en la BD.
 *
 * @Index en panelId acelera las consultas "dame todos los botones del panel X".
 * Sin el índice, SQLite tendría que recorrer toda la tabla cada vez (lento).
 */
@Entity(
    tableName = "sound_buttons",
    foreignKeys = [
        ForeignKey(
            entity        = SoundPanelEntity::class,
            parentColumns = ["id"],
            childColumns  = ["panelId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index("panelId")]
)
data class SoundButtonEntity(
    @PrimaryKey
    val id: String,
    val panelId: String,
    val name: String,
    val soundUri: String?,
    val position: Int
)
