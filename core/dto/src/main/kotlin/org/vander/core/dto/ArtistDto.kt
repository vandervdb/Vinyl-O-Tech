package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape of a Spotify Web API artist object, in its simplified form.
 */
@Serializable
data class ArtistDto(
    @SerialName("external_urls") val externalUrls: ExternalUrlDto,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String,
)
