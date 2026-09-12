package org.vander.core.dto

import kotlinx.serialization.Serializable

/**
 * Wire shape of a cover image. [height] and [width] are nullable because the API omits
 * them on some images.
 */
@Serializable
data class ImageDto(
    val url: String,
    val height: Int? = null,
    val width: Int? = null,
)
