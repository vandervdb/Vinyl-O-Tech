package org.vander.android.sample.designsystem

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val SpotifyGreen = Color(0xFF1ED760)

// Vinyl O'Tech brand palette (Claude Design canvas "Vinyl OTech - App Android",
// screen 01 "Connexion Spotify") — dark-only brand identity, distinct from the
// starter-template Purple/Pink tones above.
//
// Each name mirrors its `vot_*` counterpart in res/values/colors.xml so the
// Compose palette and the XML theme cannot drift apart unnoticed — the two were
// already crossed (VinylTextSecondary held vot_text_muted's value) before the
// design's colors.xml was merged in.
val VinylInk = Color(0xFF0C0A10) // vot_bg
val VinylPurple = Color(0xFF7C5CFF) // vot_accent
val VinylTextPrimary = Color(0xFFF2EFF7) // vot_text_primary
val VinylTextSecondary = Color(0xFF9A93A8) // vot_text_secondary
val VinylTextMuted = Color(0xFFA29BB0) // vot_text_muted
val VinylTextTertiary = Color(0xFF6B6479) // vot_text_tertiary

// Complément du kit d'implémentation (android-assets/compose/Color.kt). Les tokens
// des sillons du disque (VinylDisc*) et du bras de platine (VinylArm*) ne sont pas
// repris : leurs seuls consommateurs, VinylDisc/VinylLoader, arriveront avec l'écran 02.
val VinylPurpleDim = Color(0xFF5B47A8) // vot_accent_dim
val VinylPurpleSoft = Color(0xFFC7B4FF) // vot_accent_soft
val VinylOnPurple = Color(0xFF0C0A10) // vot_on_accent
val VinylSurface = Color(0xFF100E16) // vot_surface
val VinylSurfaceHigh = Color(0xFF16131E) // vot_surface_high
val VinylOutline = Color(0x12FFFFFF) // vot_outline
val VinylOutlineStrong = Color(0x29FFFFFF) // vot_outline_strong

// Dégradé héros de l'écran 02 (155° : 0 % / 42 % / 100 %)
val VinylGradStart = Color(0xFF4B2AA8) // vot_grad_start
val VinylGradMid = Color(0xFF7C5CFF) // vot_grad_mid
val VinylGradEnd = Color(0xFF201636) // vot_grad_end

// One-off pure-white emphasis used only for hero headlines (e.g. "Ta discothèque,
// à ta façon.") — the design's default text color elsewhere is VinylTextPrimary,
// not pure white.
val VinylTextEmphasis = Color.White // vot_text_on_media
