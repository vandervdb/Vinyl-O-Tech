package org.vander.spotifyclient.domain.state

import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.PlayerStateData

class PlayerStateFlowExtensionsTest {
    @Test
    fun `update replaces the value with the transformed one`() {
        val flow = MutableStateFlow(DomainPlayerState.empty())

        flow.update { it.copy(isTrackSaved = true) }

        assertEquals(true, flow.value.isTrackSaved)
    }

    @Test
    fun `updateIf applies the transform when the predicate holds`() {
        val flow = MutableStateFlow(stateWith(trackId = "abc"))

        flow.updateIf(predicate = { it.base.trackId == "abc" }, transform = { it.copy(isTrackSaved = true) })

        assertEquals(true, flow.value.isTrackSaved)
    }

    @Test
    fun `updateIf leaves the value alone when the predicate fails`() {
        val flow = MutableStateFlow(stateWith(trackId = "abc"))

        flow.updateIf(predicate = { it.base.trackId == "other" }, transform = { it.copy(isTrackSaved = true) })

        assertNull(flow.value.isTrackSaved)
    }

    @Test
    fun `togglePause flips the paused flag both ways`() {
        val flow = MutableStateFlow(stateWith(isPaused = true))

        flow.togglePause()
        assertFalse(flow.value.base.isPaused)

        flow.togglePause()
        assertTrue(flow.value.base.isPaused)
    }

    @Test
    fun `togglePause leaves the other fields of the snapshot untouched`() {
        val flow = MutableStateFlow(stateWith(trackId = "abc", isPaused = false))

        flow.togglePause()

        assertEquals("abc", flow.value.base.trackId)
    }

    @Test
    fun `togglePause does not touch the derived playing flag — known inconsistency`() {
        // PlayerStateData carries isPaused, paused and playing, and the SDK mapper keeps
        // them consistent. This extension flips isPaused only, so a state toggled locally
        // has isPaused == playing. Harmless today because the UI reads isPaused, but the
        // three fields are no longer telling the same story.
        val flow = MutableStateFlow(stateWith(isPaused = true).let { it.copy(base = it.base.copy(playing = false)) })

        flow.togglePause()

        assertFalse(flow.value.base.isPaused)
        assertFalse(flow.value.base.playing)
    }

    @Test
    fun `setTrackSaved writes the flag`() {
        val flow = MutableStateFlow(DomainPlayerState.empty())

        flow.setTrackSaved(true)
        assertEquals(true, flow.value.isTrackSaved)

        flow.setTrackSaved(false)
        assertEquals(false, flow.value.isTrackSaved)
    }

    @Test
    fun `setTrack writes the id into the snapshot`() {
        val flow = MutableStateFlow(DomainPlayerState.empty())

        flow.setTrack("4cOdK2")

        assertEquals("4cOdK2", flow.value.base.trackId)
    }

    @Test
    fun `reset returns to the empty state`() {
        val flow = MutableStateFlow(stateWith(trackId = "abc", isPaused = false))
        flow.setTrackSaved(true)

        flow.reset()

        assertEquals("", flow.value.base.trackId)
        assertNull(flow.value.isTrackSaved)
        assertTrue(flow.value.base.isPaused)
    }

    private fun stateWith(
        trackId: String = "",
        isPaused: Boolean = true,
    ): DomainPlayerState =
        DomainPlayerState(
            base = PlayerStateData.empty().copy(trackId = trackId, isPaused = isPaused),
            isTrackSaved = null,
        )
}
