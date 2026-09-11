package org.vander.android.sample.feature.library

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.vander.android.sample.component.SpotifyTrackCover
import org.vander.android.sample.designsystem.component.MarqueeTextInfinite
import org.vander.core.domain.data.Playlist
import org.vander.core.ui.presentation.viewmodel.PlaylistViewModel

@Composable
fun PlaylistComponent(
    viewModel: PlaylistViewModel,
    modifier: Modifier = Modifier,
) {
    val playlistCollection by viewModel.playlists.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(60.dp),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = playlistCollection.items,
            key = { it.id },
        ) { playlist ->
            PlaylistCoverItem(playlist)
        }
    }
}

@Composable
fun PlaylistCoverItem(playlist: Playlist) {
    val id = playlist.id
    val name = playlist.name
    val coverUrl = playlist.coverUrl
    val modifier =
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .pointerInput(id) {
                detectTapGestures { _ ->
                    run {
                        // TODO: Implement on click to play playlist
                        Log.d("PlaylistCoverItem", "Clicked on playlist: $id, $name, $coverUrl")
                    }
                }
            }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        SpotifyTrackCover(
            modifier = modifier,
            model = coverUrl,
        )
        MarqueeTextInfinite(
            text = name,
            textStyle = MaterialTheme.typography.bodySmall,
            spacer = 35.dp,
        )
    }
}
