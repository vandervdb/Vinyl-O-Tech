package org.vander.core.domain.data

/**
 * The user's listening history, most recent first — the domain counterpart of
 * `RecentlyPlayedResponseDto`.
 *
 * Wraps the list rather than being one, like [PlaylistCollection]: the endpoint is cursor-paged,
 * so a cursor will have to live here the day a second page is asked for.
 */
data class RecentlyPlayed(
    val items: List<RecentPlay>,
) {
    /**
     * The last play that can actually be resumed.
     *
     * A track played on its own carries no context, and playing its URI again would play that
     * one track rather than resume anything — so those entries are skipped rather than shown.
     */
    val lastResumable: RecentPlay?
        get() = items.firstOrNull { it.contextUri != null }

    companion object {
        fun empty() = RecentlyPlayed(emptyList())
    }
}

/**
 * One play: what was played, when, and what it was played from.
 *
 * @property playedAt ISO-8601 UTC timestamp, as the API sends it — no date type here, which
 *   would pull a dependency into this pure Kotlin module.
 * @property contextUri the playlist, album or artist playback ran from; `null` for a track
 *   played on its own, or when the API sent a URI that is not a `spotify:<kind>:<id>` triple.
 */
data class RecentPlay(
    val track: Track,
    val playedAt: String,
    val contextUri: SpotifyUri?,
)
