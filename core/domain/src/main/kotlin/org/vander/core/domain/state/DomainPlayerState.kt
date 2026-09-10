package org.vander.core.domain.state

data class DomainPlayerState(
    val base: PlayerStateData,
    val isTrackSaved: Boolean? = null,
) {
    companion object {
        fun empty(): DomainPlayerState = DomainPlayerState(PlayerStateData.Companion.empty(), null)
    }
}
