package org.vander.core.domain.data

/**
 * A playlist reduced to what the grid needs — the Web API returns far more.
 *
 * @property coverUrl first image of the playlist, empty when it has none.
 */
data class Playlist(
    val id: String,
    val name: String,
    val coverUrl: String,
)
