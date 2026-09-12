package org.vander.core.domain.data

/**
 * An album, as returned alongside a track by the Web API.
 *
 * @property albumType `album`, `single` or `compilation`, verbatim from the API.
 * @property externalUrls the public `open.spotify.com` link, flattened from the DTO's map.
 * @property images cover art in several sizes, in the order the API returned them.
 * @property releaseDate ISO-8601 date whose precision varies (`YYYY`, `YYYY-MM`, `YYYY-MM-DD`).
 */
data class Album(
    val albumType: String,
    val totalTracks: Int,
    val availableMarkets: List<String>,
    val externalUrls: String,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val releaseDate: String,
    val type: String,
    val uri: String,
    val artists: List<Artist>,
) {
    companion object {
        fun empty(): Album =
            Album(
                albumType = "",
                totalTracks = 0,
                availableMarkets = emptyList(),
                externalUrls = "",
                href = "",
                id = "",
                images = emptyList(),
                name = "",
                releaseDate = "",
                type = "",
                uri = "",
                artists = emptyList(),
            )
    }
}
