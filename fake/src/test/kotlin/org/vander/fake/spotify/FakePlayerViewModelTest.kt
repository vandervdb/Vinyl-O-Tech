package org.vander.fake.spotify

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.player.PlayerCommand

/**
 * The fake is what previews run on, so its reactions are the preview's behaviour: a command
 * that does not move the state is a button that does nothing on the design canvas.
 */
class FakePlayerViewModelTest {
    @Test
    fun `the sample state is ready, with a track and a queue to page through`() {
        val state = FakePlayerViewModel().state.value

        assertTrue(state.queue.items.size > 1)
        assertEquals(
            state.queue.items
                .first()
                .trackId,
            state.player.base.trackId,
        )
    }

    @Test
    fun `TogglePlayPause flips the paused flags together`() {
        val viewModel = FakePlayerViewModel()
        val wasPaused = viewModel.state.value.player.base.isPaused

        viewModel.onCommand(PlayerCommand.TogglePlayPause)

        val base = viewModel.state.value.player.base
        assertEquals(!wasPaused, base.isPaused)
        assertEquals(base.isPaused, base.paused)
        assertEquals(!base.isPaused, base.playing)
    }

    @Test
    fun `Pause and Resume set the flag rather than toggling it`() {
        val viewModel = FakePlayerViewModel()

        viewModel.onCommand(PlayerCommand.Pause)
        viewModel.onCommand(PlayerCommand.Pause)
        assertTrue(viewModel.state.value.player.base.isPaused)

        viewModel.onCommand(PlayerCommand.Resume)
        assertFalse(viewModel.state.value.player.base.isPaused)
    }

    @Test
    fun `SkipNext moves to the next queued track and rewinds`() {
        val viewModel = FakePlayerViewModel()
        val next = viewModel.state.value.queue.items[1]

        viewModel.onCommand(PlayerCommand.SkipNext)

        val base = viewModel.state.value.player.base
        assertEquals(next.trackId, base.trackId)
        assertEquals(next.trackName, base.trackName)
        assertEquals(0L, base.positionMs)
    }

    @Test
    fun `SkipPrevious on the first track stays put`() {
        val viewModel = FakePlayerViewModel()
        val first = viewModel.state.value.player.base.trackId

        viewModel.onCommand(PlayerCommand.SkipPrevious)

        assertEquals(first, viewModel.state.value.player.base.trackId)
    }

    @Test
    fun `SkipNext then SkipPrevious comes back`() {
        val viewModel = FakePlayerViewModel()
        val first = viewModel.state.value.player.base.trackId

        viewModel.onCommand(PlayerCommand.SkipNext)
        viewModel.onCommand(PlayerCommand.SkipPrevious)

        assertEquals(first, viewModel.state.value.player.base.trackId)
    }

    @Test
    fun `SeekTo moves the head and stays within the track`() {
        val viewModel = FakePlayerViewModel()
        val duration = viewModel.state.value.player.base.durationMs

        viewModel.onCommand(PlayerCommand.SeekTo(10_000))
        assertEquals(10_000L, viewModel.state.value.player.base.positionMs)

        viewModel.onCommand(PlayerCommand.SeekTo(duration + 50_000))
        assertEquals(duration, viewModel.state.value.player.base.positionMs)
    }

    @Test
    fun `ToggleSave flips the heart`() {
        val viewModel = FakePlayerViewModel()
        val wasSaved = viewModel.state.value.player.isTrackSaved == true

        viewModel.onCommand(PlayerCommand.ToggleSave)

        assertEquals(!wasSaved, viewModel.state.value.player.isTrackSaved)
    }

    @Test
    fun `Play records the context and starts playing`() {
        val viewModel = FakePlayerViewModel()
        viewModel.onCommand(PlayerCommand.Pause)

        viewModel.onCommand(PlayerCommand.Play(SpotifyUri.playlist("37i9dQZF1DXcBWIGoYBM5M")))

        assertEquals("37i9dQZF1DXcBWIGoYBM5M", viewModel.state.value.context.playlistId)
        assertFalse(viewModel.state.value.player.base.isPaused)
    }

    @Test
    fun `every command is recorded, in order`() {
        val viewModel = FakePlayerViewModel()

        viewModel.onCommand(PlayerCommand.SkipNext)
        viewModel.onCommand(PlayerCommand.ToggleSave)

        assertEquals(listOf(PlayerCommand.SkipNext, PlayerCommand.ToggleSave), viewModel.received)
    }
}
