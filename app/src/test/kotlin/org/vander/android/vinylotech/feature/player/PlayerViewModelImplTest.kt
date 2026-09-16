package org.vander.android.vinylotech.feature.player

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.vander.android.vinylotech.testing.FakePlayerController
import org.vander.android.vinylotech.testing.FakeSessionManager
import org.vander.android.vinylotech.testing.MainDispatcherRule
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.QueuedTrack
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.PlaybackState
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SessionState
import org.vander.core.ui.domain.UIQueueItem

class PlayerViewModelImplTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val controller = FakePlayerController()

    private val session = FakeSessionManager(SessionState.Ready)

    @Test
    fun `creating the ViewModel starts the controller`() {
        PlayerViewModelImpl(controller, session)

        assertEquals(1, controller.startCount)
    }

    @Test
    fun `the state combines the session and the controller's state`() =
        runTest {
            val viewModel = PlayerViewModelImpl(controller, session)
            val context = PlaybackContext(uri = SpotifyUri.playlist("p1"))

            controller.emit(
                PlaybackState(
                    player = DomainPlayerState(PlayerStateData.empty().copy(trackId = "t1"), isTrackSaved = true),
                    context = context,
                ),
            )

            viewModel.state.test {
                val state = expectMostRecentItem()
                assertEquals(SessionState.Ready, state.session)
                assertEquals("t1", state.player.base.trackId)
                assertEquals(true, state.player.isTrackSaved)
                assertEquals(context, state.context)
            }
        }

    @Test
    fun `the queue is mapped to UI items on the app side`() =
        runTest {
            val viewModel = PlayerViewModelImpl(controller, session)

            controller.emit(PlaybackState(queue = listOf(QueuedTrack("t1", "Nuits blanches", "Elia Faure"))))

            viewModel.state.test {
                assertEquals(
                    listOf(UIQueueItem(trackName = "Nuits blanches", artistName = "Elia Faure", trackId = "t1")),
                    expectMostRecentItem().queue.items,
                )
            }
        }

    @Test
    fun `a session change reaches the state`() =
        runTest {
            val viewModel = PlayerViewModelImpl(controller, session)

            session.sessionState.value = SessionState.Idle

            viewModel.state.test { assertEquals(SessionState.Idle, expectMostRecentItem().session) }
        }

    @Test
    fun `commands are forwarded to the controller untouched`() {
        val viewModel = PlayerViewModelImpl(controller, session)

        viewModel.onCommand(PlayerCommand.ToggleSave)
        viewModel.onCommand(PlayerCommand.SeekTo(4_200))

        assertEquals(listOf(PlayerCommand.ToggleSave, PlayerCommand.SeekTo(4_200)), controller.dispatched)
    }
}
