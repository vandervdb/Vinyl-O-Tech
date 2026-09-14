package org.vander.android.vinylotech.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import org.vander.android.vinylotech.R
import org.vander.android.vinylotech.component.SpotifyTrackCover
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VinylTileLabel
import org.vander.android.vinylotech.designsystem.VotDimens
import org.vander.android.vinylotech.designsystem.component.VinylDisc
import org.vander.core.domain.data.Playlist

/** Maquette écran 02 : `grid-template-columns: repeat(3, 1fr)`. */
private const val GRID_COLUMNS = 3

/** Un tour de disque, en ms. Maquette : `animation: spin 3.6s linear infinite`. */
private const val SPIN_CYCLE_MS = 3600

/** Durée de la décélération jusqu'à l'arrêt, quelle que soit la course restante. */
private const val SPIN_STOP_MS = 1400

/**
 * Course minimale laissée au disque pour ralentir. Sans elle, un arrêt déclenché juste
 * avant la fin d'un tour ne laisserait que quelques degrés, et la halte paraîtrait nette.
 */
private const val SPIN_STOP_MIN_SWEEP = 180f

private const val FULL_TURN = 360f

/** Maquette : titre de vignette en 12px / 500, interligne 1,2 — soit 14,4sp. */
private val TILE_TITLE_SIZE = 12.sp

private val TILE_TITLE_LINE_HEIGHT = 14.4.sp

/**
 * Les vignettes de « Ta bibliothèque », écran 02.
 *
 * À injecter dans le `LazyVerticalGrid` de l'écran, qui portera aussi le héros et l'en-tête
 * de section : un second grid imbriqué mettrait un défilement vertical dans un autre.
 * [PlaylistGrid] est le tandem autonome, pour les previews et l'usage isolé.
 *
 * **Écart assumé avec la maquette** : elle fait tourner le disque au survol
 * (`--vt-spin: running` sur `:hover`), ce qui n'existe pas au doigt. Ici il tourne quand la
 * playlist est celle en cours de lecture — l'animation devient un indicateur d'état plutôt
 * qu'un effet de survol.
 *
 * @param playingPlaylistId identifiant de la playlist en cours ; `null` quand rien ne joue,
 *   ou quand la source ne sait pas encore le dire.
 */
fun LazyGridScope.playlistGridItems(
    playlists: List<Playlist>,
    playingPlaylistId: String?,
    onPlaylistClick: (Playlist) -> Unit,
) {
    items(
        items = playlists,
        key = { it.id },
    ) { playlist ->
        PlaylistGridItem(
            playlist = playlist,
            isPlaying = playlist.id == playingPlaylistId,
            onClick = { onPlaylistClick(playlist) },
        )
    }
}

/**
 * [playlistGridItems] dans son propre `LazyVerticalGrid`.
 *
 * Le `contentPadding` est dissymétrique — 20 dp à gauche, 30 dp à droite — parce que le
 * disque de la dernière colonne déborde de sa pochette ; c'est ce que documente déjà
 * [VotDimens.listPaddingEnd].
 */
@Composable
fun PlaylistGrid(
    playlists: List<Playlist>,
    playingPlaylistId: String?,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = modifier,
        contentPadding =
            PaddingValues(
                start = VotDimens.listPaddingStart,
                end = VotDimens.listPaddingEnd,
            ),
        verticalArrangement = Arrangement.spacedBy(VotDimens.gridGutterV),
        horizontalArrangement = Arrangement.spacedBy(VotDimens.gridGutterH),
    ) {
        playlistGridItems(playlists, playingPlaylistId, onPlaylistClick)
    }
}

/**
 * Une vignette : le disque qui dépasse derrière, la pochette par-dessus, le titre dessous.
 *
 * La maquette pose une seconde ligne sous le titre (l'artiste, 11px `#857E92`) qu'on
 * n'affiche pas : [Playlist] ne porte que `id`, `name` et `coverUrl`, et une playlist n'a
 * de toute façon pas d'artiste unique.
 */
@Composable
private fun PlaylistGridItem(
    playlist: Playlist,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    val playingLabel = stringResource(R.string.content_desc_playlist_playing, playlist.name)

    Column(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                // Le titre est déjà lu par le lecteur d'écran sous la vignette ; ce qu'il
                // rate, c'est le disque qui tourne. La sémantique de la tuile porte donc
                // l'état de lecture, et le nom seul sinon.
                .clearAndSetSemantics {
                    contentDescription = if (isPlaying) playingLabel else playlist.name
                },
        verticalArrangement = Arrangement.spacedBy(VotDimens.space7),
    ) {
        // BoxWithConstraints et non un offset fixe : le débord du disque est une fraction
        // de la largeur de la vignette, qui dépend elle-même de celle de l'écran.
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            val overhang = maxWidth * VotDimens.DISC_OVERHANG_RATIO

            SpinningDisc(
                spinning = isPlaying,
                modifier =
                    Modifier
                        .matchParentSize()
                        .offset(x = overhang),
            )

            SpotifyTrackCover(
                modifier = Modifier.matchParentSize(),
                model = playlist.coverUrl,
            )
        }

        Text(
            text = playlist.name,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = TILE_TITLE_SIZE,
                    lineHeight = TILE_TITLE_LINE_HEIGHT,
                ),
            color = VinylTileLabel,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Le disque, immobile ou en rotation.
 *
 * Même grammaire de mouvement que `VinylMiniTurntable` : un tour à la fois en repartant de
 * l'angle courant, et une décélération jusqu'à 0° à l'arrêt plutôt qu'un figeage net —
 * l'inertie d'un plateau. Pas de bras ici : la maquette n'en pose pas sur les vignettes.
 *
 * L'état de l'animation suit l'item par la clé de `items` ([Playlist.id]), donc le recyclage
 * du `LazyVerticalGrid` ne le transfère pas d'une playlist à l'autre.
 */
@Composable
private fun SpinningDisc(
    spinning: Boolean,
    modifier: Modifier = Modifier,
) {
    val angle = remember { Animatable(0f) }

    LaunchedEffect(spinning) {
        if (spinning) {
            // À l'arrêt l'effet est annulé et l'Animatable garde sa valeur : la décélération
            // enchaîne donc sans saut d'angle.
            while (isActive) {
                angle.animateTo(
                    targetValue = angle.value + FULL_TURN,
                    animationSpec = tween(SPIN_CYCLE_MS, easing = LinearEasing),
                )
                angle.snapTo(angle.value % FULL_TURN)
            }
            return@LaunchedEffect
        }

        if (angle.value == 0f) return@LaunchedEffect

        // On termine toujours sur 0°, quitte à ajouter un tour pour avoir de quoi ralentir.
        // LinearOutSlowIn part à pleine vitesse et s'éteint : c'est l'inertie d'un plateau,
        // pas une transition qui démarre.
        val remaining = FULL_TURN - angle.value
        val sweep = if (remaining < SPIN_STOP_MIN_SWEEP) remaining + FULL_TURN else remaining
        angle.animateTo(
            targetValue = angle.value + sweep,
            animationSpec = tween(SPIN_STOP_MS, easing = LinearOutSlowInEasing),
        )
        angle.snapTo(0f)
    }

    VinylDisc(modifier = modifier.graphicsLayer { rotationZ = angle.value })
}

private val previewPlaylists =
    listOf(
        Playlist(id = "1", name = "Sillons", coverUrl = ""),
        Playlist(id = "2", name = "Marée haute", coverUrl = ""),
        Playlist(id = "3", name = "Braise", coverUrl = ""),
        Playlist(id = "4", name = "Gris perle", coverUrl = ""),
        Playlist(id = "5", name = "Or mat", coverUrl = ""),
        Playlist(id = "6", name = "Onde longue", coverUrl = ""),
    )

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10)
@Composable
private fun PlaylistGridPreview() {
    AndroidAppTheme {
        PlaylistGrid(
            playlists = previewPlaylists,
            playingPlaylistId = null,
            onPlaylistClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10, name = "Une playlist en lecture")
@Composable
private fun PlaylistGridPlayingPreview() {
    AndroidAppTheme {
        PlaylistGrid(
            playlists = previewPlaylists,
            playingPlaylistId = "3",
            onPlaylistClick = {},
        )
    }
}
