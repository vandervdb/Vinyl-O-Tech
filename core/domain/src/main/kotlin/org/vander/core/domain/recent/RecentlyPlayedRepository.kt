package org.vander.core.domain.recent

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.RecentlyPlayed

interface RecentlyPlayedRepository {
    val recentlyPlayed: StateFlow<RecentlyPlayed>

    suspend fun refresh(): Result<Unit>
}
