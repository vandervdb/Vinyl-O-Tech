package org.vander.core.dto

import kotlinx.serialization.Serializable

/**
 * Why a track or album cannot be played.
 *
 * @property reason kept as a String rather than an enum: the API can introduce a new value,
 *   which would fail to deserialize into a closed enum.
 */
@Serializable
data class RestrictionsDto(
    val reason: String,
)
