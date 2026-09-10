package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistDto(
    @SerialName("external_urls") val externalUrls: ExternalUrlDto,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String,
)
