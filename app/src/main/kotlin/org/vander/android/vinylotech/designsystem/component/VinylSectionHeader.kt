package org.vander.android.vinylotech.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VinylPurple
import org.vander.android.vinylotech.designsystem.VinylTextPrimary

/**
 * En-tête de section de l'écran 02 (« Ta bibliothèque · 243 albums ») : titre à gauche, compte
 * à droite, alignés sur leur ligne de base comme dans la maquette (`align-items: baseline`) —
 * pas centrés, les deux corps de texte sont trop différents.
 *
 * La maquette pose le titre en 26 px ; l'échelle typographique n'a que 24 sp à cette graisse,
 * d'où la surcharge locale plutôt qu'un nouveau cran utilisé une seule fois.
 */
@Composable
fun VinylSectionHeader(
    title: String,
    trailing: String?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            color = VinylTextPrimary,
            modifier = Modifier.weight(1f).alignBy(FirstBaseline),
        )
        trailing?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = VinylPurple,
                modifier = Modifier.alignBy(FirstBaseline),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10)
@Composable
private fun VinylSectionHeaderPreview() {
    AndroidAppTheme {
        VinylSectionHeader(title = "Ta bibliothèque", trailing = "243 playlists")
    }
}
