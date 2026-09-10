package org.vander.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpotifyPlaylistsResponseDto(
    val href: String,
    val limit: Int,
    val next: String? = null,
    val offset: Int,
    val previous: String? = null,
    val total: Int,
    val items: List<SpotifyPlaylistDto>,
)
