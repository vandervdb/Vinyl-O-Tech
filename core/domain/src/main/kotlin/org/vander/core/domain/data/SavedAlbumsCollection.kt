package org.vander.core.domain.data

data class SavedAlbumsCollection(
    val items: List<SavedAlbumObject>,
) {
    companion object {
        fun empty() = SavedAlbumsCollection(emptyList())
    }
}
