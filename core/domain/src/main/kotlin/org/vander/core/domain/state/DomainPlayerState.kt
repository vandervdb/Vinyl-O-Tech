package org.vander.core.domain.state

/**
 * Player snapshot enriched with data the App Remote cannot provide.
 *
 * Composition rather than a wider [PlayerStateData]: the remote and the Web API are two
 * sources with two refresh rates, so the saved flag is kept beside the base snapshot
 * instead of being merged into it. Read its fields through the extensions in
 * `PlayerStateExtensions.kt`.
 *
 * @property base last snapshot received from the App Remote.
 * @property isTrackSaved whether the track is in the user's library, from the Web API;
 *   `null` while the answer is still unknown — distinct from a confirmed `false`.
 */
data class DomainPlayerState(
    val base: PlayerStateData,
    val isTrackSaved: Boolean? = null,
) {
    companion object {
        fun empty(): DomainPlayerState = DomainPlayerState(PlayerStateData.Companion.empty(), null)
    }
}
