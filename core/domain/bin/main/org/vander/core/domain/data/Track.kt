package org.vander.core.domain.data

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
