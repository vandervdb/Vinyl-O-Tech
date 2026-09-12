package org.vander.spotifyclient.data.player.mapper

import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.logger.Logger
import org.vander.spotifyclient.bridge.PlayerStateDto

/**
 * Flattens the domain state into the [PlayerStateDto] the bridge exposes to its callers.
 *
 * `trackUri` receives the bare track id, not a `spotify:track:` URI, despite the name.
 */
fun DomainPlayerState.toPlayerStateDto(logger: Logger?): PlayerStateDto {
    logger?.d("DomainPlayerStateMapper", "Track uri: ${this.base.trackId}")
    return PlayerStateDto(
        isPlaying = base.playing,
        positionMs = base.positionMs,
        durationMs = base.durationMs,
        trackUri = base.trackId,
        coverId = base.coverId,
        trackName = base.trackName,
        artistName = base.artistName,
        albumName = base.albumName,
    )
}
