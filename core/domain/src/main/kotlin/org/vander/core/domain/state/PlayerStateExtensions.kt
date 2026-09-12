package org.vander.core.domain.state

/**
 * Read-only accessors that flatten [DomainPlayerState.base], so a consumer writes
 * `state.trackName` instead of `state.base.trackName`, and two `copyWith*` helpers.
 *
 * Extensions rather than members: [DomainPlayerState] stays a plain data class with no
 * behaviour, and the app can keep the shorthand without the domain model growing an API.
 */
val DomainPlayerState.trackName get() = base.trackName
val DomainPlayerState.artistName get() = base.artistName
val DomainPlayerState.albumName get() = base.albumName
val DomainPlayerState.coverId get() = base.coverId
val DomainPlayerState.trackId get() = base.trackId

val DomainPlayerState.isPaused get() = base.isPaused
val DomainPlayerState.playing get() = base.playing
val DomainPlayerState.paused get() = base.paused
val DomainPlayerState.stopped get() = base.stopped
val DomainPlayerState.shuffling get() = base.shuffling
val DomainPlayerState.repeating get() = base.repeating
val DomainPlayerState.seeking get() = base.seeking
val DomainPlayerState.skippingNext get() = base.skippingNext
val DomainPlayerState.skippingPrevious get() = base.skippingPrevious
val DomainPlayerState.positionMs get() = base.positionMs
val DomainPlayerState.durationMs get() = base.durationMs

fun DomainPlayerState.copyWithSaved(isSaved: Boolean): DomainPlayerState = this.copy(isTrackSaved = isSaved)

fun DomainPlayerState.copyWithBase(base: PlayerStateData): DomainPlayerState = this.copy(base = base)
