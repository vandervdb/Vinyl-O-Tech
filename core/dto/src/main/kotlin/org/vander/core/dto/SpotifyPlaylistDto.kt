package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape of a playlist object. Only [id], [name] and the first image survive the
 * mapping to `Playlist`.
 *
 * @property images `null` for a playlist with no cover, hence the nullable list.
 * @property public `null` when the playlist's visibility is not known to the caller —
 *   distinct from a confirmed `false`.
 * @property snapshotId version identifier of the playlist content, required by the API to
 *   apply a modification against a known state.
 */
@Serializable
data class SpotifyPlaylistDto(
    val collaborative: Boolean,
    val description: String,
    @SerialName("external_urls") val externalUrls: ExternalUrlsDto,
    val href: String,
    val id: String,
    val images: List<SpotifyImageDto>? = null,
    val name: String,
    val owner: SpotifyOwnerDto,
    val public: Boolean? = null,
    @SerialName("snapshot_id") val snapshotId: String,
    val tracks: SpotifyTracksDto,
    val type: String,
    val uri: String,
)

@Serializable
data class ExternalUrlsDto(
    val spotify: String,
)

@Serializable
data class SpotifyOwnerDto(
    @SerialName("external_urls") val externalUrls: ExternalUrlsDto,
    val href: String,
    val id: String,
    val type: String,
    val uri: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
data class SpotifyTracksDto(
    val href: String,
    val total: Int,
)

@Serializable
data class SpotifyImageDto(
    val url: String,
    val height: Int? = null,
    val width: Int? = null,
)
