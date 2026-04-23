package com.example.ttrpg_sound.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// =============================================================================
// Esquemas de color
// =============================================================================

/**
 * Enum que representa los esquemas de color disponibles en la app.
 *
 * [displayName] es el nombre que ve el usuario en el dropdown.
 *
 * Cada valor del enum corresponde a un conjunto de tres colores definidos
 * en Color.kt: color de botones, fondo, y texto. La función [buildColorScheme]
 * los convierte en un [ColorScheme] completo de Material3.
 *
 * ¿Por qué un enum y no una sealed class?
 * Porque los esquemas son un conjunto cerrado y finito de opciones — no
 * necesitan datos adicionales ni subtipos. El enum es más simple, serializable
 * directamente, y permite iterar con `AppColorScheme.entries` en el dropdown.
 */
enum class AppColorScheme(val displayName: String) {
    DEFAULT("Por defecto"),
    OSCURO("Oscuro"),
    MAGO("Mago"),
    VAMPIRO("Vampiro"),
    BOSQUE("Bosque")
}

/**
 * Construye un [ColorScheme] de Material3 a partir de los tres colores
 * clave de cada esquema de la app.
 *
 * Estrategia: usar [lightColorScheme] o [darkColorScheme] como base
 * (que provee valores razonables para todos los ~30 colores del sistema)
 * y sobreescribir solo los que controlamos explícitamente.
 *
 * Los esquemas con fondo oscuro (todos menos DEFAULT) extienden
 * [darkColorScheme] para que los componentes del sistema (checkboxes,
 * switches, scrollbars) también adopten tonos apropiados para fondo oscuro.
 *
 * Los colores que sobreescribimos en cada esquema:
 *   - [primaryContainer]    → fondo de las tarjetas de botón de sonido
 *   - [onPrimaryContainer]  → texto dentro de las tarjetas
 *   - [background]          → fondo de la pantalla completa
 *   - [surface]             → fondo de Scaffold, drawers, bottom sheets
 *   - [onBackground]        → texto sobre el fondo de pantalla
 *   - [onSurface]           → texto sobre surfaces (topbar, drawer…)
 *   - [surfaceVariant]      → fondo de elementos secundarios (FAB apagado)
 *   - [onSurfaceVariant]    → texto en surfaceVariant
 */
fun buildColorScheme(scheme: AppColorScheme): ColorScheme {
    return when (scheme) {
        AppColorScheme.DEFAULT -> lightColorScheme(
            primaryContainer   = DefaultButtonColor,
            onPrimaryContainer = DefaultTextColor,
            background         = DefaultBackground,
            surface            = DefaultBackground,
            onBackground       = DefaultTextColor,
            onSurface          = DefaultTextColor,
            surfaceVariant     = Color(0xFFE0E0E0),
            onSurfaceVariant   = Color(0xFF616161)
        )
        AppColorScheme.MAGO -> darkColorScheme(
            primaryContainer   = MagoButtonColor,
            onPrimaryContainer = MagoTextColor,
            background         = MagoBackground,
            surface            = MagoBackground,
            onBackground       = MagoTextColor,
            onSurface          = MagoTextColor,
            surfaceVariant     = Color(0xFF455A64),
            onSurfaceVariant   = Color(0xFFB0BEC5)
        )
        AppColorScheme.VAMPIRO -> darkColorScheme(
            primaryContainer   = VampiroButtonColor,
            onPrimaryContainer = VampiroTextColor,
            background         = VampiroBackground,
            surface            = VampiroBackground,
            onBackground       = VampiroTextColor,
            onSurface          = VampiroTextColor,
            surfaceVariant     = Color(0xFF1A1A1A),
            onSurfaceVariant   = Color(0xFFBDBDBD)
        )
        AppColorScheme.BOSQUE -> darkColorScheme(
            primaryContainer   = BosqueButtonColor,
            onPrimaryContainer = BosqueTextColor,
            background         = BosqueBackground,
            surface            = BosqueBackground,
            onBackground       = BosqueTextColor,
            onSurface          = BosqueTextColor,
            surfaceVariant     = Color(0xFF2E7D32),
            onSurfaceVariant   = Color(0xFFC8E6C9)
        )
        AppColorScheme.OSCURO -> darkColorScheme(
            primaryContainer   = OscuroButtonColor,
            onPrimaryContainer = OscuroTextColor,
            background         = OscuroBackground,
            surface            = OscuroBackground,
            onBackground       = OscuroTextColor,
            onSurface          = OscuroTextColor,
            surfaceVariant     = Color(0xFF4E342E),
            onSurfaceVariant   = OscuroTextColor
        )
    }
}

// =============================================================================
// Shapes
// =============================================================================

private val RoundedShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val SharpShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small      = RoundedCornerShape(0.dp),
    medium     = RoundedCornerShape(0.dp),
    large      = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

// =============================================================================
// Tema principal
// =============================================================================

/**
 * Tema central de la aplicación.
 *
 * Cuando [appColorScheme] es [AppColorScheme.DEFAULT] y el dispositivo
 * soporta Dynamic Color (Android 12+), se usa el color dinámico del sistema.
 * En cualquier otro esquema, Dynamic Color se desactiva y se aplica
 * el [ColorScheme] construido por [buildColorScheme].
 *
 * @param appColorScheme    Esquema de color elegido por el usuario.
 * @param useRoundedCorners Si true, esquinas redondeadas; si false, afiladas.
 */
@Composable
fun TTRPGSoundTheme(
    appColorScheme:    AppColorScheme = AppColorScheme.DEFAULT,
    useRoundedCorners: Boolean        = true,
    content:           @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        // Dynamic color solo en el esquema por defecto
        appColorScheme == AppColorScheme.DEFAULT &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        else -> buildColorScheme(appColorScheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = if (useRoundedCorners) RoundedShapes else SharpShapes,
        content     = content
    )
}
