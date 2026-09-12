package org.vander.spotifyclient.domain.repository

import kotlinx.coroutines.flow.Flow
import org.vander.core.domain.data.User

/**
 * The signed-in user's profile.
 *
 * [fetchCurrentUser] returns nothing: the result lands on [currentUser], and a failure only
 * shows up in the logs. Unlike the other repositories here, it gives the caller no way to
 * react to an error.
 */
interface UserRepository {
    val currentUser: Flow<User?>

    suspend fun fetchCurrentUser()
}
