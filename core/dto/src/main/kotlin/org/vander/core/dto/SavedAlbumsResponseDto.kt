package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /me/albums`: an offset-paged list of the albums saved in the user's library.
 * Requires the `user-library-read` scope.
 *
 * @property next URL of the following page, `null` on the last one.
 * @property previous URL of the preceding page, `null` on the first one.
 * @property total number of saved albums across every page, not in this one.
 */
@Serializable
data class SavedAlbumsResponseDto(
    val href: String,
    val limit: Int,
    val next: String? = null,
    val offset: Int,
    val previous: String? = null,
    val total: Int,
    val items: List<SavedAlbumDto>,
)

/**
 * One saved album.
 *
 * @property addedAt ISO 8601 timestamp of when the album was saved, kept as sent.
 */
@Serializable
data class SavedAlbumDto(
    @SerialName("added_at") val addedAt: String,
    val album: FullAlbumDto,
)
