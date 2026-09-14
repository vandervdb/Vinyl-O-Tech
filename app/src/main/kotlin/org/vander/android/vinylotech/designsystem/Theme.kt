package org.vander.android.vinylotech.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

@Composable
fun AndroidAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = VotShapes,
        content = content,
    )
}
