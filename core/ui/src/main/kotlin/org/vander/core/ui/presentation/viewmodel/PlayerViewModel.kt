package org.vander.core.ui.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.SessionState
import org.vander.core.ui.state.UIQueueState

/**
 * Contract every player ViewModel must honour, real or fake.
 *
 * Implemented by the app's `@HiltViewModel` and by [org.vander.fake.spotify.FakePlayerViewModel]
 * for `@Preview`. It lives in `core-ui` rather than in `app` so both sides compile against the
 * same surface without `app` depending on `fake` at runtime — dependency inversion applied to
 * the presentation layer.
 *
 * State is exposed read-only and collected with `collectAsStateWithLifecycle()`. Every
 * function is fire-and-forget: it starts work and the outcome comes back through a flow,
 * never as a return value.
 */
interface PlayerViewModel {
    /** Connection lifecycle with the Spotify App Remote. */
    val sessionState: StateFlow<SessionState>

    /** Upcoming tracks, already mapped to UI models. */
    val uiQueueState: StateFlow<UIQueueState>

    /** Current track, playback position and flags. */
    val domainPlayerState: StateFlow<DomainPlayerState>

    fun togglePlayPause()

    fun skipNext()

    fun skipPrevious()

    /**
     * @param trackId bare Spotify id, without the `spotify:track:` prefix — the layers below
     *   wrap it in `SpotifyUri.track(...)`. Passing a full URI now throws there rather than
     *   silently producing `spotify:track:spotify:track:…`.
     */
    fun playTrack(trackId: String)

    fun toggleSave()

    /**
     * @param position absolute playback head in milliseconds, not a delta. Out-of-range values
     *   are not clamped here.
     */
    fun seekTo(position: Long)
}
