package org.vander.core.domain.data

/**
 * A page of playlists. Wraps the list so a repository can return a typed value rather
 * than a bare `List`, leaving room for pagination fields later.
 */
data class PlaylistCollection(
    val items: List<Playlist>,
) {
    companion object {
        fun empty() = PlaylistCollection(emptyList())
    }
}
