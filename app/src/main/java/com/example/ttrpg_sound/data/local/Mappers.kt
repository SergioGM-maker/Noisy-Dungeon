package com.example.ttrpg_sound.data.local

import com.example.ttrpg_sound.data.local.entity.SoundButtonEntity
import com.example.ttrpg_sound.data.local.entity.SoundPanelEntity
import com.example.ttrpg_sound.data.model.SoundButton
import com.example.ttrpg_sound.data.model.SoundPanel

/**
 * Funciones de traducción entre entidades de Room y modelos de dominio.
 *
 * ¿Por qué tener dos representaciones del mismo dato?
 *
 * SoundPanel / SoundButton son los modelos de DOMINIO: representan
 * el concepto desde el punto de vista de la lógica de negocio.
 * No saben nada de bases de datos.
 *
 * SoundPanelEntity / SoundButtonEntity son los modelos de DATOS:
 * representan exactamente cómo se almacena el dato en SQLite.
 * Tienen columnas como 'position' que son un detalle de persistencia,
 * irrelevante para la lógica de la app.
 *
 * Separar ambos permite que cambiar la BD no afecte al resto de la app,
 * y que la lógica de negocio sea independiente del mecanismo de almacenamiento.
 *
 * Se implementan como funciones de extensión (fun SoundPanel.toEntity())
 * para que el código en el Repository se lea de forma natural:
 *   panel.toEntity()       en lugar de   Mappers.toEntity(panel)
 *   entity.toDomain(...)   en lugar de   Mappers.toDomain(entity, ...)
 */

// --- Dominio → Entidad (para guardar en BD) ---

fun SoundPanel.toEntity(position: Int): SoundPanelEntity =
    SoundPanelEntity(
        id       = id,
        name     = name,
        position = position
    )

fun SoundButton.toEntity(panelId: String, position: Int): SoundButtonEntity =
    SoundButtonEntity(
        id       = id,
        panelId  = panelId,
        name     = name,
        soundUri = soundUri,
        position = position
    )

// --- Entidad → Dominio (para leer de BD) ---

fun SoundPanelEntity.toDomain(buttons: List<SoundButton> = emptyList()): SoundPanel =
    SoundPanel(
        id      = id,
        name    = name,
        buttons = buttons
    )

fun SoundButtonEntity.toDomain(): SoundButton =
    SoundButton(
        id       = id,
        name     = name,
        soundUri = soundUri
    )
