package org.vander.core.domain.data

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
