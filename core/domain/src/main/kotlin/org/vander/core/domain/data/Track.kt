package org.vander.core.domain.data

/**
 * A Spotify track, mapped from `TrackDto` by `TrackDto.toDomain()` in `spotify-lib`.
 *
 * @property id bare Spotify id; [uri] holds the `spotify:track:<id>` form.
 * @property externalIds the ISRC code only — the DTO's other external ids are dropped.
 * @property externalUrls the public `open.spotify.com` link, flattened from the DTO's map.
 * @property durationMs track length in milliseconds.
 * @property isPlayable whether the track can be played in the user's market.
 */
class Track(
    val album: Album,
    val artists: List<Artist>,
    val availableMarkets: List<String>,
    val discNumber: Int,
    val durationMs: Int,
    val explicit: Boolean,
    val externalIds: String,
    val externalUrls: String,
    val href: String,
    val id: String,
    val isPlayable: Boolean,
    val name: String,
    trackNumber: Int,
    val type: String,
    val uri: String,
) {
    companion object {
        fun empty(): Track =
            Track(
                album = Album.empty(),
                artists = emptyList(),
                availableMarkets = emptyList(),
                discNumber = 0,
                durationMs = 0,
                explicit = false,
                externalIds = "",
                externalUrls = "",
                href = "",
                id = "",
                isPlayable = false,
                name = "",
                trackNumber = 0,
                type = "",
                uri = "",
            )
    }
}
