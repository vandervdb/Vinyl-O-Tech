package org.vander.core.domain.state

/**
 * Connection state of the player, without a failure cause — unlike [RemoteClientState],
 * which carries one.
 *
 * Only ever observed as [NotConnected] in the current code: see
 * [org.vander.spotifyclient.domain.player.PlayerClient.playerConnectionState].
 */
sealed class PlayerConnectionState {
    object NotConnected : PlayerConnectionState()

    object Connecting : PlayerConnectionState()

    object Connected : PlayerConnectionState()
}
