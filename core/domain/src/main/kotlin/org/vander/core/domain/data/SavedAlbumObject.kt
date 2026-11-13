package org.vander.core.domain.data

data class SavedAlbumObject(
    val addedAt: String = "",
    val album: Album = Album.empty(),
) {
    companion object {
        fun empty() = SavedAlbumObject()
    }
}
