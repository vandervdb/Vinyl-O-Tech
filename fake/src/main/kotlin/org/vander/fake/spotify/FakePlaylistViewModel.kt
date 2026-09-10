package org.vander.fake.spotify

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.Playlist
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.ui.presentation.viewmodel.PlaylistViewModel

class FakePlaylistViewModel : PlaylistViewModel {
    private val _playlists =
        MutableStateFlow(
            PlaylistCollection(
                items =
                    List(80) { index ->
                        Playlist(
                            id = "playlist_$index",
                            name = "Name*$index",
                            coverUrl = "https://picsum.photos/200?random=$index",
                        )
                    },
            ),
        )

    override val playlists = _playlists as StateFlow<PlaylistCollection>

    override fun refresh() {
        TODO("Not yet implemented")
    }
}
