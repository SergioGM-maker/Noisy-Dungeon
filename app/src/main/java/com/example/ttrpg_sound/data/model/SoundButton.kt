package com.example.ttrpg_sound.data.model

import java.util.UUID

/**
 * Representa un botón de sonido individual.
 *
 * [soundUri] es null por ahora — lo rellenaremos cuando implementemos
 * la selección de archivos de audio. El resto de la app ya está preparada
 * para recibirlo sin cambios estructurales.
 *
 * Es una 'data class': inmutable, con equals/hashCode/copy gratis.
 * En Compose, la inmutabilidad es fundamental para que la recomposición
 * sea predecible y eficiente.
 */
data class SoundButton(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val soundUri: String? = null
)
