package org.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.RecentlyPlayed
import org.vander.core.domain.recent.RecentlyPlayedRepository
import org.vander.spotifyclient.data.remote.mapper.toDomain
import org.vander.spotifyclient.domain.datasource.IRemoteRecentlyPlayedDataSource
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class SpotifyRecentlyPlayedRepository
    @Inject
    constructor(
        private val api: IRemoteRecentlyPlayedDataSource,
    ) : RecentlyPlayedRepository {
        private val _recentlyPlayed = MutableStateFlow<RecentlyPlayed>(RecentlyPlayed.empty())
        override val recentlyPlayed: StateFlow<RecentlyPlayed> = _recentlyPlayed.asStateFlow()

        override suspend fun refresh(): Result<Unit> =
            try {
                val dto = api.fetchRecentlyPlayed().getOrThrow()
                val result = dto.toDomain()
                _recentlyPlayed.update { result }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
