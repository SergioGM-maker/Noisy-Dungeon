package com.example.ttrpg_sound.data.model

import java.util.UUID

/**
 * Representa un panel (pestaña) que agrupa botones de sonido.
 *
 * Ejemplo de uso: "Combate", "Taberna", "Dungeon", etc.
 *
 * La lista de [buttons] es inmutable (List, no MutableList).
 * Para modificarla, el ViewModel crea una copia con .copy() — esto
 * garantiza que Compose detecte siempre los cambios de estado.
 */
data class SoundPanel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val buttons: List<SoundButton> = emptyList()
)
