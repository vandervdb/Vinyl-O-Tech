package org.vander.core.dto

import kotlinx.serialization.Serializable

/**
 * A paginated page of playlists, as returned by `GET /me/playlists`.
 *
 * @property next URL of the following page, `null` on the last one.
 * @property previous URL of the preceding page, `null` on the first one.
 * @property total number of playlists across every page, not in this one.
 */
@Serializable
data class SpotifyPlaylistsResponseDto(
    val href: String,
    val limit: Int,
    val next: String? = null,
    val offset: Int,
    val previous: String? = null,
    val total: Int,
    val items: List<SpotifyPlaylistDto>,
)
