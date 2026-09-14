package org.vander.android.vinylotech.designsystem

import androidx.compose.ui.unit.dp

/**
 * Toutes les mesures des maquettes, côté Compose — pas de dimensionResource(),
 * pas de .dp magique dans les écrans. res/values/dimens.xml a été retiré du kit :
 * mélanger les deux sources était pire que du .dp homogène.
 */
object VotDimens {
    // Échelle d'espacement
    val space2 = 2.dp
    val space4 = 4.dp
    val space6 = 6.dp
    val space7 = 7.dp // écart titre/pochette des vignettes de collection, écran 02
    val space8 = 8.dp
    val space10 = 10.dp
    val space12 = 12.dp
    val space14 = 14.dp
    val space20 = 20.dp
    val space26 = 26.dp
    val space30 = 30.dp
    val space32 = 32.dp
    val space44 = 44.dp

    // Marges d'écran
    val screenPadding = 26.dp // écrans de contenu plein : connexion, paroles, profil
    val screenBottom = 44.dp // bas d'écran, au-dessus des barres système
    val listPaddingStart = 20.dp
    val listPaddingEnd = 30.dp // plus large : le vinyle déborde de la pochette

    // Grille de collection
    val gridGutterH = 26.dp // minimum pour que le disque ne touche pas la vignette voisine
    val gridGutterV = 14.dp
    const val DISC_OVERHANG_RATIO = 0.18f

    // Composants
    val markSize = 68.dp // diamètre du cercle de la marque, écrans 01 / 02
    val markSizeSmall = 30.dp
    val heroHeight = 384.dp
    val dockHeight = 64.dp
    val dockMargin = 14.dp
    val dockIcon = 13.dp // maquette : pastilles 13x13 dans la dock
    val dockElevation = 12.dp // maquette : 0 12px 40px rgba(0,0,0,0.5)
    val miniDisc = 44.dp

    // Maquette : 40 dp sur les écrans 02 · 03 · 04 · 05 · 08 · 10. Agrandi à 48 dp dans
    // l'app : c'est la hauteur des IconButton des contrôles, qui fixent déjà celle de la
    // carte — le disque gagne donc en présence sans la faire grandir d'un pixel.
    val miniPlayerDisc = 48.dp
    val touchMin = 48.dp
    val buttonPadding = 16.dp

    // Rayons
    val radiusSleeve = 2.dp
    val radiusChip = 8.dp
    val radiusCard = 12.dp
    val radiusSheet = 18.dp

    // Ombres — maquette : 0 10px 34px rgba(124,92,255,0.4)
    val buttonElevation = 10.dp
    val sheetElevation = 18.dp

    // Snackbar haute — ancrée sous la status bar, jamais en bas de l'écran
    val snackMarginTop = 12.dp
    val snackMarginH = 14.dp
    val snackRadius = 18.dp
    val snackActionRadius = 10.dp
    val snackGlyph = 28.dp
    val snackTimer = 2.dp
    val snackElevation = 20.dp
}
