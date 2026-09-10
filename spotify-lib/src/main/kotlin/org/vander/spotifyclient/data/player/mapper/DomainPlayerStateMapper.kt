package org.vander.spotifyclient.data.player.mapper

import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.logger.Logger
import org.vander.spotifyclient.bridge.PlayerStateDto

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
