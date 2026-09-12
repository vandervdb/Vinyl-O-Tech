package org.vander.core.domain.state

/**
 * Signals that the "saved to library" flag of a track changed outside this app —
 * another device, or the Spotify app itself.
 *
 * @property isSaved new value of the flag.
 * @property trackId track it applies to; empty as long as no change was observed.
 */
data class SavedRemotelyChangedState(
    var isSaved: Boolean = false,
    val trackId: String = "",
)
