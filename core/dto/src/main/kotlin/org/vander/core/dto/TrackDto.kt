package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape of a Spotify Web API track object.
 *
 * Mirrors the JSON one-to-one; every rename happens through [SerialName] so the Kotlin
 * side keeps camelCase. Converted to the domain model by `TrackDto.toDomain()` in
 * `spotify-lib`, which keeps only part of these fields — [popularity], [previewUrl],
 * [linkedFrom], [restrictions] and [isLocal] are parsed but currently dropped.
 *
 * Optional fields carry a default so a partial payload still deserializes instead of throwing.
 */
@Serializable
data class TrackDto(
    val album: AlbumDto,
    val artists: List<ArtistDto>,
    @SerialName("available_markets") val availableMarkets: List<String>,
    @SerialName("disc_number") val discNumber: Int,
    @SerialName("duration_ms") val durationMs: Int,
    val explicit: Boolean,
    @SerialName("external_ids") val externalIds: ExternalIdsDto,
    @SerialName("external_urls") val externalUrls: ExternalUrlDto,
    val href: String,
    val id: String,
    @SerialName("is_playable") val isPlayable: Boolean = true,
    @SerialName("linked_from") val linkedFrom: Map<String, String> = emptyMap(),
    val restrictions: RestrictionsDto? = null,
    val name: String,
    val popularity: Int,
    @SerialName("preview_url") val previewUrl: String?,
    @SerialName("track_number") val trackNumber: Int,
    val type: String,
    val uri: String,
    @SerialName("is_local") val isLocal: Boolean,
)
