package org.vander.android.vinylotech.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.player.PlayerController
import org.vander.core.domain.state.PlaybackState

/** [PlayerController] whose state the test sets, and which records what it is asked. */
class FakePlayerController : PlayerController {
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    var startCount: Int = 0
        private set

    val dispatched = mutableListOf<PlayerCommand>()

    var nextResult: Result<Unit> = Result.success(Unit)

    fun emit(state: PlaybackState) {
        _state.value = state
    }

    override fun start() {
        startCount++
    }

    override suspend fun dispatch(command: PlayerCommand): Result<Unit> {
        dispatched += command
        return nextResult
    }
}
