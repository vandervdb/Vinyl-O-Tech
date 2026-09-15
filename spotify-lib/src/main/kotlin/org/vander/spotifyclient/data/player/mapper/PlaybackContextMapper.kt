package org.vander.spotifyclient.data.player.mapper

import com.spotify.protocol.types.PlayerContext
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.SpotifyUri

/**
 * Maps the SDK's `PlayerContext` onto [PlaybackContext].
 *
 * `PlayerContext.type` is a bare `String` the SDK never interprets — there is no constant
 * for it anywhere in the AAR, it is forwarded from the Spotify application as-is. The kind
 * is therefore read from the URI, whose `spotify:<kind>:<id>` shape is documented, rather
 * than from that field.
 *
 * A URI that does not parse yields a `null` [PlaybackContext.uri] while keeping the title
 * and subtitle: losing the label would be worse than not knowing the kind.
 */
fun PlayerContext.toPlaybackContext(): PlaybackContext =
    PlaybackContext(
        uri = uri?.let { SpotifyUri.parse(it) },
        title = title.orEmpty(),
        subtitle = subtitle.orEmpty(),
    )
