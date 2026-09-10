package org.vander.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageDto(
    val url: String,
    val height: Int? = null,
    val width: Int? = null,
)
