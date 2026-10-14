package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape of a Spotify Web API album object in its full form, as sent by `GET /me/albums`.
 *
 * [AlbumDto] is the simplified form nested in a track; this one adds the album's own track
 * list, label, genres, copyrights and popularity. The fields beyond identity carry a default so
 * a partial payload still deserializes instead of failing the whole page.
 *
 * @property releaseDatePrecision `year`, `month` or `day` — tells how much of [releaseDate]
 *   is meaningful.
 * @property tracks the first page of the album's tracks only; [AlbumTracksPageDto.next] points
 *   to the rest.
 */
@Serializable
data class FullAlbumDto(
    @SerialName("album_type") val albumType: String,
    @SerialName("total_tracks") val totalTracks: Int,
    @SerialName("available_markets") val availableMarkets: List<String> = emptyList(),
    @SerialName("external_urls") val externalUrls: ExternalUrlDto,
    val href: String,
    val id: String,
    val images: List<ImageDto>,
    val name: String,
    @SerialName("release_date") val releaseDate: String,
    @SerialName("release_date_precision") val releaseDatePrecision: String,
    val restrictions: RestrictionsDto? = null,
    val type: String,
    val uri: String,
    val artists: List<ArtistDto>,
    val tracks: AlbumTracksPageDto,
    val copyrights: List<CopyrightDto> = emptyList(),
    @SerialName("external_ids") val externalIds: ExternalIdsDto = ExternalIdsDto(),
    val genres: List<String> = emptyList(),
    val label: String? = null,
    val popularity: Int = 0,
)

/** An offset-paged slice of an album's tracks. */
@Serializable
data class AlbumTracksPageDto(
    val href: String,
    val limit: Int,
    val next: String? = null,
    val offset: Int,
    val previous: String? = null,
    val total: Int,
    val items: List<SimplifiedTrackDto>,
)

/**
 * A track as listed inside an album: unlike [TrackDto], it carries no album (the parent is
 * the album), no external ids and no popularity.
 *
 * @property linkedFrom the track originally requested, when Spotify relinked it to another one
 *   playable in the user's market; `null` when no relinking happened.
 */
@Serializable
data class SimplifiedTrackDto(
    val artists: List<ArtistDto>,
    @SerialName("available_markets") val availableMarkets: List<String> = emptyList(),
    @SerialName("disc_number") val discNumber: Int,
    @SerialName("duration_ms") val durationMs: Int,
    val explicit: Boolean,
    @SerialName("external_urls") val externalUrls: ExternalUrlDto,
    val href: String,
    val id: String,
    @SerialName("is_playable") val isPlayable: Boolean = true,
    @SerialName("linked_from") val linkedFrom: LinkedTrackDto? = null,
    val restrictions: RestrictionsDto? = null,
    val name: String,
    @SerialName("preview_url") val previewUrl: String? = null,
    @SerialName("track_number") val trackNumber: Int,
    val type: String,
    val uri: String,
    @SerialName("is_local") val isLocal: Boolean = false,
)

/**
 * The original track behind a relinked one.
 *
 * Every field defaults because Spotify's own reference sends `"linked_from": {}` on a track
 * that was not relinked; required fields would fail the whole page on that empty object.
 */
@Serializable
data class LinkedTrackDto(
    @SerialName("external_urls") val externalUrls: ExternalUrlDto? = null,
    val href: String? = null,
    val id: String? = null,
    val type: String? = null,
    val uri: String? = null,
)

/**
 * @property type `C` for the copyright, `P` for the sound recording (performance) copyright;
 *   kept as a String like [RestrictionsDto.reason].
 */
@Serializable
data class CopyrightDto(
    val text: String,
    val type: String,
)
