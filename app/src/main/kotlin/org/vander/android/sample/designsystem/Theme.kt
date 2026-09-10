package org.vander.android.sample.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = VinylPurple,
        onPrimary = VinylOnPurple,
        primaryContainer = VinylPurpleDim,
        onPrimaryContainer = VinylTextEmphasis,
        secondary = VinylPurpleSoft,
        background = VinylInk,
        onBackground = VinylTextPrimary,
        surface = VinylSurface,
        surfaceContainerHigh = VinylSurfaceHigh,
        onSurface = VinylTextPrimary,
        onSurfaceVariant = VinylTextSecondary,
        outline = VinylOutlineStrong,
        outlineVariant = VinylOutline,
    )

// Rayons des maquettes. `outline`/`outlineVariant` ci-dessus sont des couleurs de
// TRAIT (blanc translucide), pas de texte : un écran qui a besoin d'une teinte de
// texte précise passe par le token explicite (VinylTextMuted, VinylTextTertiary),
// pas par un slot sémantique Material qui ne correspond pas.
// Public comme VotDimens : Modifier.filterChip()/dockSurface() (Surfaces.kt) ne sont
// pas @Composable et ne peuvent donc pas lire MaterialTheme.shapes.
val VotShapes =
    Shapes(
        extraSmall = RoundedCornerShape(VotDimens.radiusSleeve),
        small = RoundedCornerShape(VotDimens.radiusChip),
        medium = RoundedCornerShape(VotDimens.radiusCard),
        large = RoundedCornerShape(VotDimens.radiusSheet),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color.White,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
     */
    )

@Suppress("FunctionNaming")
@Composable
fun AndroidAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default: Vinyl O'Tech is a fixed brand identity (dark-only, like
    // Spotify itself), Material You's per-device dynamic color would override it.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = VotShapes,
        content = content,
    )
}
