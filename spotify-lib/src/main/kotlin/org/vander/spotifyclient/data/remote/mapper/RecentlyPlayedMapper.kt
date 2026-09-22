package org.vander.spotifyclient.data.remote.mapper

import org.vander.core.domain.data.RecentPlay
import org.vander.core.domain.data.RecentlyPlayed
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.dto.PlayHistoryDto
import org.vander.core.dto.RecentlyPlayedResponseDto

/**
 * Maps `GET /me/player/recently-played` onto the domain.
 *
 * Two things are dropped on the way: the paging fields, since only the first page is requested,
 * and everything of the context but its URI — the Web API sends no name there, unlike the
 * context the App Remote pushes. A screen that wants a label reads it from the track and its
 * album instead.
 */
fun RecentlyPlayedResponseDto.toDomain(): RecentlyPlayed = RecentlyPlayed(items = items.map { it.toDomain() })

/**
 * The context URI is parsed rather than kept as text, so an unexpected shape becomes `null`
 * here instead of failing later, where it would be used to start playback.
 */
fun PlayHistoryDto.toDomain(): RecentPlay =
    RecentPlay(
        track = track.toDomain(),
        playedAt = playedAt,
        contextUri = context?.uri?.let { SpotifyUri.parse(it) },
    )
