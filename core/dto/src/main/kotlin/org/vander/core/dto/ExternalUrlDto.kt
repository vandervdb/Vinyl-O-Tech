package org.vander.core.dto

import kotlinx.serialization.Serializable

/**
 * The `external_urls` object, which in practice only ever holds the `spotify` key.
 *
 * Note: [ExternalUrlsDto] in `SpotifyPlaylistDto.kt` declares the exact same shape under a
 * near-identical name. The two are interchangeable and one of them should go — see the
 * known issues in `CLAUDE.md` rather than merging them as a side effect.
 */
@Serializable
data class ExternalUrlDto(
    val spotify: String,
)
