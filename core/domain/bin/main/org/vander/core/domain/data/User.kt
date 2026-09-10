package org.vander.core.domain.data

data class User(
    val name: String,
    val imageUrl: String?,
) {
    companion object {
        val empty = User("", null)
    }
}
