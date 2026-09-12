package org.vander.core.domain.data

/**
 * A cover image.
 *
 * @property height pixel height; `null` when the API does not know it.
 * @property width pixel width; `null` when the API does not know it.
 */
data class Image(
    val url: String,
    val height: Int? = null,
    val width: Int? = null,
)
