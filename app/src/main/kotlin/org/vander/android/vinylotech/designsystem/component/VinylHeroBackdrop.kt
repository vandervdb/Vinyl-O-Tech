package org.vander.android.vinylotech.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.HeroScrim
import org.vander.android.vinylotech.designsystem.VotDimens
import org.vander.android.vinylotech.designsystem.heroGradient

/**
 * Le fond violet du haut de l'écran 02 : dégradé à 155° puis voile qui le fond vers le noir.
 *
 * Il passe sous la barre d'état — l'app est en edge-to-edge — et c'est le contenu qu'il porte
 * qui s'en écarte : le fond mesure [height] plus la hauteur de la barre d'état, [content] garde
 * exactement [height]. Branche enfin `heroGradient` et `HeroScrim` de `Surfaces.kt`, qui
 * attendaient cet écran.
 */
@Composable
fun VinylHeroBackdrop(
    modifier: Modifier = Modifier,
    height: Dp = VotDimens.heroHeight,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(heroGradient(size.width, size.height))
                    drawRect(HeroScrim)
                },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(height),
            content = content,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10)
@Composable
private fun VinylHeroBackdropPreview() {
    AndroidAppTheme {
        VinylHeroBackdrop { Box(Modifier.fillMaxSize()) }
    }
}
