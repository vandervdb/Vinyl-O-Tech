package org.vander.android.vinylotech.designsystem

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
val VinylSurfaceCard = Color(0xFF221C2E) // vot_surface_card — carte du MiniPlayer
val VinylOutline = Color(0x12FFFFFF) // vot_outline
val VinylOutlineStrong = Color(0x29FFFFFF) // vot_outline_strong

// Dock flottant (écrans 02 · 03 · 04 · 10). Relevés dans la maquette, où ils sont
// systématiques : #7A7387 apparaît 13 fois, #A98CFF 10 fois. Ni l'un ni l'autre ne
// se déduit de la palette existante — VinylTextTertiary (#6B6479) est trop sombre
// pour une icône, VinylPurpleSoft (#C7B4FF) trop clair.
val VinylNavIconInactive = Color(0xFF7A7387)
val VinylNavIconActive = Color(0xFFA98CFF)

// Libellé des vignettes de collection (écran 02). Ni VinylTextPrimary (#F2EFF7, réservé
// au texte de contenu) ni VinylTextMuted (#A29BB0, trop effacé) : la maquette pose une
// valeur propre sous les pochettes.
val VinylTileLabel = Color(0xFFE8E4F0) // vot_tile_label

// Dégradé héros de l'écran 02 (155° : 0 % / 42 % / 100 %)
val VinylGradStart = Color(0xFF4B2AA8) // vot_grad_start
val VinylGradMid = Color(0xFF7C5CFF) // vot_grad_mid
val VinylGradEnd = Color(0xFF201636) // vot_grad_end

// Sous-titre posé sur le dégradé héros (« Elia Faure · 2023 · 11 titres »)
val VinylTextOnGradient = Color(0xFFCFC8DC) // vot_text_on_gradient

// One-off pure-white emphasis used only for hero headlines (e.g. "Ta discothèque,
// à ta façon.") — the design's default text color elsewhere is VinylTextPrimary,
// not pure white.
val VinylTextEmphasis = Color.White // vot_text_on_media

// Sillons du disque, du centre vers le bord
val VinylDiscHole = Color(0xFF0C0A10) // vot_vinyl_hole
val VinylDiscDeep = Color(0xFF0F0E14) // vot_vinyl_deep
val VinylDiscBase = Color(0xFF121117) // vot_vinyl_base
val VinylDiscMid = Color(0xFF16151B) // vot_vinyl_mid
val VinylDiscGroove = Color(0xFF2A2833) // vot_vinyl_groove
val VinylDiscGrooveHi = Color(0xFF302E39) // vot_vinyl_groove_hi
val VinylDiscRim = Color(0xFF4A4753) // vot_vinyl_rim

// Platine
val VinylArmMetal = Color(0xFFE6E2F0) // vot_arm_metal
val VinylArmMetalDim = Color(0xFF8B8798) // vot_arm_metal_dim
val VinylArmShadow = Color(0xFF2C2A34) // vot_arm_shadow

// Feedback — snackbar haute (tons Error / Success / Info / Offline)
val VinylFeedbackError = Color(0xFFFF5C6E) // vot_feedback_error
val VinylFeedbackErrorText = Color(0xFFFF8A96) // vot_feedback_error_text
val VinylFeedbackSuccess = Color(0xFF4ADE9B) // vot_feedback_success
val VinylFeedbackSuccessText = Color(0xFF6FE7B2) // vot_feedback_success_text
val VinylFeedbackNeutralText = Color(0xFFE6E2F0) // vot_feedback_neutral_text
val VinylSnackSurface = Color(0xFF191722) // vot_snack_surface
