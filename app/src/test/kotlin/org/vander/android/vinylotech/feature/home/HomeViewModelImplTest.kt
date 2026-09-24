package org.vander.android.vinylotech.feature.home

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.vander.android.vinylotech.testing.FakePlayerController
import org.vander.android.vinylotech.testing.MainDispatcherRule
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.Playlist
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.domain.data.RecentlyPlayed
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.state.PlaybackState
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.domain.repository.RecentlyPlayedRepository
import org.vander.spotifyclient.domain.repository.SpotifyPlaylistRepository
import org.vander.spotifyclient.domain.usecase.PlaylistUseCase
import org.vander.spotifyclient.domain.usecase.RecentlyPlayedUseCase

class HomeViewModelImplTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val controller = FakePlayerController()

    private val playlists = PlaylistCollection(listOf(Playlist(id = PLAYLIST_ID, name = "Sillons", coverUrl = "")))

    private fun viewModel() =
        HomeViewModelImpl(
            playlistUseCase = PlaylistUseCase(FakePlaylistRepository(Result.success(playlists)), FakeLogger()),
            recentlyPlayedUseCase = RecentlyPlayedUseCase(FakeRecentlyPlayedRepository(), FakeLogger()),
            controller = controller,
            logger = FakeLogger(),
        )

    @Test
    fun `creating the ViewModel starts the controller and loads the playlists`() =
        runTest {
            val viewModel = viewModel()

            viewModel.state.test {
                assertEquals(listOf("Sillons"), expectMostRecentItem().playlists.items.map { it.name })
            }
            assertEquals(1, controller.startCount)
        }

    @Test
    fun `nothing is marked as playing while no context is known`() =
        runTest {
            viewModel().state.test { assertNull(expectMostRecentItem().playingPlaylistId) }
        }

    @Test
    fun `the playing playlist comes from the playback context`() =
        runTest {
            val viewModel = viewModel()

            controller.emit(PlaybackState(context = PlaybackContext(uri = SpotifyUri.playlist(PLAYLIST_ID))))

            viewModel.state.test { assertEquals(PLAYLIST_ID, expectMostRecentItem().playingPlaylistId) }
        }

    @Test
    fun `an album playing marks no playlist, even one sharing its id`() =
        runTest {
            val viewModel = viewModel()

            controller.emit(PlaybackState(context = PlaybackContext(uri = SpotifyUri.album(PLAYLIST_ID))))

            viewModel.state.test { assertNull(expectMostRecentItem().playingPlaylistId) }
        }

    @Test
    fun `a click plays the playlist through the controller`() {
        val viewModel = viewModel()

        viewModel.playPlaylist(PLAYLIST_ID)

        assertEquals(listOf(PlayerCommand.Play(SpotifyUri.playlist(PLAYLIST_ID))), controller.dispatched)
    }

    private class FakePlaylistRepository(
        private val result: Result<PlaylistCollection>,
    ) : SpotifyPlaylistRepository {
        override val playlists: StateFlow<PlaylistCollection?> = MutableStateFlow(null)

        override suspend fun getUserPlaylists(): Result<PlaylistCollection> = result
    }

    private class FakeRecentlyPlayedRepository : RecentlyPlayedRepository {
        override val recentlyPlayed: StateFlow<RecentlyPlayed?> = MutableStateFlow(null)

        override suspend fun getRecentlyPlayed(): Result<RecentlyPlayed> = Result.success(RecentlyPlayed.empty())
    }

    private companion object {
        const val PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M"
    }
}
