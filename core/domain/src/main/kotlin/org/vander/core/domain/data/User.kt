package org.vander.core.domain.data

/**
 * The signed-in Spotify user.
 *
 * @property name the account display name.
 * @property imageUrl first profile picture, `null` when the account has none.
 */
data class User(
    val name: String,
    val imageUrl: String?,
) {
    companion object {
        val empty = User("", null)
    }
}
