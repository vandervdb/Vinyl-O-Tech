package org.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.User
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.remote.mapper.toDomain
import org.vander.spotifyclient.domain.datasource.IRemoteUserDataSource
import org.vander.spotifyclient.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class SpotifyUserRepository
    @Inject
    constructor(
        private val api: IRemoteUserDataSource,
        private val logger: Logger,
    ) : UserRepository {
        private val _currentUser = MutableStateFlow<User?>(null)
        override val currentUser: Flow<User?> = _currentUser.asStateFlow()

        override suspend fun fetchCurrentUser() {
            try {
                val dto = api.fetchUser().getOrThrow()
                val user = dto.toDomain()
                logger.d(TAG, "Received user: $user")
                _currentUser.update { user }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                logger.e(TAG, "Error fetching user")
                _currentUser.update { null }
            }
        }

        private companion object {
            const val TAG = "SpotifyUserRepository"
        }
    }
