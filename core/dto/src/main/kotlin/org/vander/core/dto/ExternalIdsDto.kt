package org.vander.core.dto

import kotlinx.serialization.Serializable

/**
 * The `external_ids` object. All three keys are optional; the domain model keeps [isrc] only.
 *
 * @property isrc International Standard Recording Code.
 * @property ean European Article Number.
 * @property upc Universal Product Code.
 */
@Serializable
data class ExternalIdsDto(
    val isrc: String? = null,
    val ean: String? = null,
    val upc: String? = null,
)
