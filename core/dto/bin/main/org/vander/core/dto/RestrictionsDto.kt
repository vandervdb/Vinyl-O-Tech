package org.vander.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class RestrictionsDto(
    val reason: String,
)
