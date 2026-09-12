package org.vander.core.domain.data

/**
 * An artist credited on a track or an album.
 *
 * @property externalUrls the public `open.spotify.com` link, flattened from the DTO's map.
 */
data class Artist(
    val externalUrls: String,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String,
)
