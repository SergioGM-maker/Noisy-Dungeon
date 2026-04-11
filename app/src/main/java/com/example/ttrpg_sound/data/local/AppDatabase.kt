package com.example.ttrpg_sound.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ttrpg_sound.data.local.dao.SoundButtonDao
import com.example.ttrpg_sound.data.local.dao.SoundPanelDao
import com.example.ttrpg_sound.data.local.entity.SoundButtonEntity
import com.example.ttrpg_sound.data.local.entity.SoundPanelEntity

/**
 * Base de datos de la aplicación.
 *
 * @Database declara qué entidades (tablas) existen y la versión del esquema.
 * La versión es importante: si en el futuro cambias la estructura de una tabla
 * (añades una columna, renombras un campo), debes incrementar la versión
 * y escribir una Migration para que los datos del usuario no se pierdan.
 * Por ahora en versión 1, no hay nada que migrar.
 *
 * --- Singleton ---
 * La base de datos es un recurso caro de crear. Solo debe existir UNA instancia
 * en toda la aplicación. Usamos el patrón Singleton con @Volatile + synchronized
 * para garantizarlo incluso si dos hilos intentan crearla a la vez.
 *
 * @Volatile: los cambios a 'instance' son visibles inmediatamente a todos los hilos.
 * synchronized: solo un hilo puede ejecutar ese bloque a la vez.
 */
@Database(
    entities  = [SoundPanelEntity::class, SoundButtonEntity::class],
    version   = 1,
    exportSchema = false   // desactiva la exportación del esquema a un fichero JSON
)                          // (útil en producción, innecesario ahora)
abstract class AppDatabase : RoomDatabase() {

    abstract fun soundPanelDao(): SoundPanelDao
    abstract fun soundButtonDao(): SoundButtonDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,  // applicationContext evita leaks de Activity
                    AppDatabase::class.java,
                    "ttrpg_sound.db"
                ).build().also { instance = it }
            }
    }
}
