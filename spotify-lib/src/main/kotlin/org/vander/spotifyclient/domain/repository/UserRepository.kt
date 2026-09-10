package org.vander.spotifyclient.domain.repository

import kotlinx.coroutines.flow.Flow
import org.vander.core.domain.data.User

interface UserRepository {
    val currentUser: Flow<User?>

    suspend fun fetchCurrentUser()
}
