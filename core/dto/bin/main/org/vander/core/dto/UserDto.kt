package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val country: String,
    @SerialName("display_name")
    val displayName: String,
    val email: String,
    @SerialName("explicit_content")
    val explicitContent: ExplicitContentDto,
    @SerialName("external_urls")
    val externalUrls: ExternalUrlsDto,
    val followers: FollowersDto,
    val href: String,
    val id: String,
    val images: List<ImageDto>,
    val product: String,
    val type: String,
    val uri: String,
)

@Serializable
data class ExplicitContentDto(
    @SerialName("filter_enabled")
    val filterEnabled: Boolean,
    @SerialName("filter_locked")
    val filterLocked: Boolean,
)

@Serializable
data class FollowersDto(
    val href: String? = null,
    val total: Int,
)
