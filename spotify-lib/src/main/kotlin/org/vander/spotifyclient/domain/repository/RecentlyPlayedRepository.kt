package org.vander.spotifyclient.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.RecentlyPlayed

interface RecentlyPlayedRepository {
    val recentlyPlayed: StateFlow<RecentlyPlayed?>

    suspend fun getRecentlyPlayed(): Result<RecentlyPlayed>
}
