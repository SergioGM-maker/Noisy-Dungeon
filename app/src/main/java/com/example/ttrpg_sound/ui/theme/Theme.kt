package com.example.ttrpg_sound.ui.theme

import android.os.Build
import androidx.annotation.StringRes
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
import com.example.ttrpg_sound.R

// =============================================================================
// Esquemas de color
// =============================================================================

/**
 * Enum centralizado en Theme.kt porque es un concepto de tema, no de ViewModel.
 *
 * [nameRes] apunta a strings.xml para que el nombre sea localizable.
 * HomeScreen lo lee con stringResource(scheme.nameRes).
 * buildColorScheme() lo usa para construir el ColorScheme de Material3.
 */
enum class AppColorScheme(@StringRes val nameRes: Int) {
    DEFAULT(R.string.scheme_default),
    OSCURO(R.string.scheme_dark),
    MAGO(R.string.scheme_mage),
    VAMPIRO(R.string.scheme_vampire),
    BOSQUE(R.string.scheme_forest)
}

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
        AppColorScheme.OSCURO -> darkColorScheme(
            primaryContainer   = OscuroButtonColor,
            onPrimaryContainer = OscuroTextColor,
            background         = OscuroBackground,
            surface            = OscuroBackground,
            onBackground       = OscuroTextColor,
            onSurface          = OscuroTextColor,
            surfaceVariant     = Color(0xFF1A1A1A),
            onSurfaceVariant   = OscuroTextColor
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

@Composable
fun TTRPGSoundTheme(
    appColorScheme:    AppColorScheme = AppColorScheme.DEFAULT,
    useRoundedCorners: Boolean        = true,
    content:           @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
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
