package com.example.ttrpg_sound.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un panel en la base de datos.
 *
 * @Entity le dice a Room: "esta data class es una tabla de SQLite".
 * Cada propiedad se convierte en una columna.
 * @PrimaryKey marca la clave única que identifica cada fila.
 *
 * [position] almacena el orden del panel en la lista.
 * Sin él, SQLite no garantiza ningún orden al leer.
 */
@Entity(tableName = "sound_panels")
data class SoundPanelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val position: Int
)
