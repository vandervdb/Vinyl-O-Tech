package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /me/player/recently-played`.
 *
 * A cursor-paged list, unlike the offset-paged `GET /me/playlists`: [cursors] carries the
 * bounds to ask for the previous or next page, and there is no `offset`. Requires the
 * `user-read-recently-played` scope.
 *
 * Every field but [items] has a default: the endpoint omits several of them depending on
 * where the page sits, and a missing key would otherwise fail the whole call — the trap
 * `UserDto` still falls into.
 *
 * @property next URL of the following page, `null` on the last one.
 * @property total documented by the API but not always sent; do not rely on it to know how
 *   much history exists.
 */
@Serializable
data class RecentlyPlayedResponseDto(
    val href: String? = null,
    val limit: Int = 0,
    val next: String? = null,
    val cursors: CursorsDto? = null,
    val total: Int = 0,
    val items: List<PlayHistoryDto> = emptyList(),
)

/**
 * Bounds of a cursor-paged answer.
 *
 * Both are Unix timestamps in milliseconds, sent as strings — they are meant to be handed
 * back as the `after`/`before` query parameter, not parsed.
 */
@Serializable
data class CursorsDto(
    val after: String? = null,
    val before: String? = null,
)

/**
 * One play in the history: what was played, when, and from where.
 *
 * @property playedAt ISO-8601 UTC timestamp, e.g. `2026-09-18T07:03:41.769Z`.
 * @property context what playback ran from — a playlist, an album, an artist. `null` when the
 *   track was played on its own, which is exactly the case where nothing can be resumed.
 */
@Serializable
data class PlayHistoryDto(
    val track: TrackDto,
    @SerialName("played_at") val playedAt: String,
    val context: PlayContextDto? = null,
)

/**
 * The `context` object of a play: the same triple the App Remote publishes on its own channel,
 * seen from the Web API.
 *
 * @property type `playlist`, `album`, `artist`, … Prefer reading the kind from [uri], whose
 *   `spotify:<kind>:<id>` shape is documented, rather than from this free-form string.
 */
@Serializable
data class PlayContextDto(
    val type: String,
    val href: String? = null,
    @SerialName("external_urls") val externalUrls: ExternalUrlDto? = null,
    val uri: String,
)
