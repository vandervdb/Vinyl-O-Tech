package org.vander.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExternalIdsDto(
    val isrc: String? = null,
    val ean: String? = null,
    val upc: String? = null,
)
