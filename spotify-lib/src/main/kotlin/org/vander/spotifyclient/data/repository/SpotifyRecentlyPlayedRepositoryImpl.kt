package org.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.RecentlyPlayed
import org.vander.spotifyclient.data.remote.mapper.toDomain
import org.vander.spotifyclient.domain.datasource.IRemoteRecentlyPlayedDataSource
import org.vander.spotifyclient.domain.repository.RecentlyPlayedRepository
import javax.inject.Inject

class SpotifyRecentlyPlayedRepositoryImpl
    @Inject
    constructor(
        private val api: IRemoteRecentlyPlayedDataSource,
    ) : RecentlyPlayedRepository {
        private val _recentlyPlayed = MutableStateFlow<RecentlyPlayed?>(null)
        override val recentlyPlayed: StateFlow<RecentlyPlayed?> = _recentlyPlayed.asStateFlow()

        override suspend fun getRecentlyPlayed(): Result<RecentlyPlayed> =
            try {
                val dto = api.fetchRecentlyPlayed().getOrThrow()
                val result = dto.toDomain()
                _recentlyPlayed.update { result }
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
