package org.vander.android.sample.designsystem

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
    val space8 = 8.dp
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
    val miniDisc = 44.dp
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
}
