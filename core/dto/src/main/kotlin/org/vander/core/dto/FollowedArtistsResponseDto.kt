package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /me/following?type=artist`: the artists the user follows.
 * Requires the `user-follow-read` scope.
 *
 * The page is nested under an `artists` key, unlike the other paged endpoints which return it
 * at the root.
 */
@Serializable
data class FollowedArtistsResponseDto(
    val artists: FollowedArtistsPageDto,
)

/**
 * A cursor-paged slice of followed artists.
 *
 * Unlike the offset-paged answers, there is no `offset`/`previous`: the next page is requested
 * with the `after` query parameter, whose value is [cursors]' `after`.
 *
 * @property next URL of the following page, `null` on the last one.
 * @property total number of followed artists across every page, not in this one.
 */
@Serializable
data class FollowedArtistsPageDto(
    val href: String,
    val limit: Int,
    val next: String? = null,
    val cursors: CursorsDto? = null,
    val total: Int,
    val items: List<FullArtistDto>,
)

/**
 * Wire shape of a Spotify Web API artist object in its full form.
 *
 * [ArtistDto] is the simplified form nested in a track or an album; this one adds the artist's
 * followers, genres, images and popularity. Those fields carry a default so a partial payload
 * still deserializes instead of failing the whole page.
 */
@Serializable
data class FullArtistDto(
    @SerialName("external_urls") val externalUrls: ExternalUrlDto,
    val followers: FollowersDto? = null,
    val genres: List<String> = emptyList(),
    val href: String,
    val id: String,
    val images: List<ImageDto> = emptyList(),
    val name: String,
    val popularity: Int = 0,
    val type: String,
    val uri: String,
)
