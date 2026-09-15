package org.vander.core.domain.data

/**
 * What the player is playing *from* — a playlist, an album, an artist's page.
 *
 * The App Remote publishes it on a channel of its own, separate from the player state and
 * with its own rhythm: `PlayerState` carries the track, never the context. That is why this
 * is a type beside [org.vander.core.domain.state.PlayerStateData] rather than a field
 * inside it, the same reasoning that keeps `isTrackSaved` out of the snapshot.
 *
 * The two channels are not synchronised. Right after a playlist is started, a new track can
 * land before the new context does, so a screen comparing the two sees them disagree for a
 * frame.
 *
 * @property uri `null` when nothing is playing, or when the SDK sends something that is not
 *   a `spotify:<kind>:<id>` triple.
 * @property title name of the playlist or album, as the SDK spells it.
 * @property subtitle its owner or artist, as the SDK spells it.
 */
data class PlaybackContext(
    val uri: SpotifyUri? = null,
    val title: String = "",
    val subtitle: String = "",
) {
    /**
     * The playlist id when a playlist is playing, `null` otherwise — an album playing is not
     * a playlist, and must not light up a playlist tile that happens to share its id.
     */
    val playlistId: String?
        get() = uri?.takeIf { it.kind == SpotifyUri.Kind.Playlist }?.id

    /** Whether [playlistId] designates this playlist. False while nothing is playing. */
    fun isPlaying(playlistId: String): Boolean = this.playlistId == playlistId

    companion object {
        val None = PlaybackContext()
    }
}
