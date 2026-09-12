package org.vander.spotifyclient.data.player.mapper

import com.spotify.protocol.types.PlayerState
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.logger.Logger

private const val REPEAT_CONTEXT = 1
private const val REPEAT_TRACK = 2

/**
 * Maps the SDK's `PlayerState` onto [PlayerStateData], stripping the Spotify URI prefixes so
 * the ids are directly usable in a Web API call.
 *
 * Read it before trusting a field name: `stopped` and `seeking` are hardcoded to `false`, and
 * `skippingNext`/`skippingPrevious` actually carry "skipping is not allowed" from the SDK's
 * playback restrictions.
 *
 * The two local constants are also named the wrong way round: the SDK's `Repeat` defines
 * `ONE = 1` and `ALL = 2`, while `REPEAT_CONTEXT` is 1 and `REPEAT_TRACK` is 2 here. Only
 * the names are wrong — `repeating` tests both values, so its result is unaffected.
 */
fun PlayerState.toPlayerStateData(logger: Logger? = null): PlayerStateData {
    val track = this.track

    logger?.d("PlayerStateMapper", "Track uri=${track?.uri}, name=${track?.name}")

    return PlayerStateData(
        trackName = track?.name ?: "Unknown Track",
        artistName = track?.artist?.name ?: "Unknown Artist",
        albumName = track?.album?.name ?: "Unknown Album",
        coverId =
            track
                ?.imageUri
                ?.toString()
                ?.extractSpotifyCoverIdOrNull()
                ?: "",
        trackId =
            track
                ?.uri
                ?.toString()
                ?.extractSpotifyTrackIdOrNull()
                ?: "",
        isPaused = isPaused,
        playing = !isPaused,
        paused = isPaused,
        stopped = false,
        shuffling = playbackOptions?.isShuffling == true,
        repeating =
            playbackOptions?.repeatMode == REPEAT_CONTEXT ||
                playbackOptions?.repeatMode == REPEAT_TRACK,
        seeking = false,
        skippingNext = playbackRestrictions?.canSkipNext == false,
        skippingPrevious = playbackRestrictions?.canSkipPrev == false,
        positionMs = playbackPosition,
        durationMs = track?.duration ?: 0,
    )
}

/**
 * Extracts the image id from the SDK's `ImageId{spotify:image:…'}` rendering.
 *
 * It parses a `toString()` output, so an SDK upgrade that changes that formatting silently
 * returns `null` here rather than failing. Returns `null` when the prefix does not match.
 */
fun String.extractSpotifyCoverIdOrNull(): String? =
    if (startsWith("ImageId{spotify:image:")) {
        substringAfter("ImageId{spotify:image:").substringBefore("'}")
    } else {
        null
    }

/** Strips the `spotify:track:` prefix; returns `null` for anything else, a local file included. */
fun String.extractSpotifyTrackIdOrNull(): String? =
    if (startsWith("spotify:track:")) substringAfter("spotify:track:") else null
