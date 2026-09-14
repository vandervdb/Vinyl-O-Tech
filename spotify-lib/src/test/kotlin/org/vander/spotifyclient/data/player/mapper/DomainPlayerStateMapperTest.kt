package org.vander.spotifyclient.data.player.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.data.local.mapper.toAppPlayerState

/**
 * The two mappers that sit on either side of [DomainPlayerState]: one builds it from a raw
 * snapshot, the other flattens it into the DTO the React Native bridge exposes.
 */
class DomainPlayerStateMapperTest {
    @Test
    fun `the bridge DTO carries the bare track id, despite the field being named trackUri`() {
        val state = stateWith(trackId = "4cOdK2")

        val dto = state.toPlayerStateDto(null)

        assertEquals("4cOdK2", dto.trackUri)
    }

    @Test
    fun `isPlaying comes from the playing flag, not from the negation of isPaused`() {
        // The two are kept in sync by the SDK mapper but not by the local togglePause
        // extension, so which one is read matters.
        val state = stateWith(isPaused = true, playing = true)

        assertTrue(state.toPlayerStateDto(null).isPlaying)
    }

    @Test
    fun `position, duration and the text fields are carried over`() {
        val state =
            stateWith(
                trackId = "id",
                positionMs = 42_000,
                durationMs = 214_000,
                trackName = "Nuits blanches",
                artistName = "Elia Faure",
                albumName = "Sillons",
                coverId = "ab67616d",
            )

        val dto = state.toPlayerStateDto(null)

        assertEquals(42_000L, dto.positionMs)
        assertEquals(214_000L, dto.durationMs)
        assertEquals("Nuits blanches", dto.trackName)
        assertEquals("Elia Faure", dto.artistName)
        assertEquals("Sillons", dto.albumName)
        assertEquals("ab67616d", dto.coverId)
    }

    @Test
    fun `the saved flag is not part of the bridge DTO`() {
        // PlayerStateDto has no field for it: the bridge exposes playback only.
        val dto = DomainPlayerState(PlayerStateData.empty(), isTrackSaved = true).toPlayerStateDto(null)

        assertFalse(dto.isPlaying)
    }

    @Test
    fun `a null logger is accepted`() {
        // The parameter is nullable so a caller without a Logger can still map.
        assertEquals("", stateWith().toPlayerStateDto(null).trackUri)
    }

    @Test
    fun `the logger receives the track id when one is given`() {
        val logger = FakeLogger()

        stateWith(trackId = "4cOdK2").toPlayerStateDto(logger)

        assertTrue(logger.contains(FakeLogger.Entry.Level.DEBUG, "DomainPlayerStateMapper", "4cOdK2"))
    }

    @Test
    fun `toAppPlayerState pairs a snapshot with the saved flag`() {
        val snapshot = PlayerStateData.empty().copy(trackId = "4cOdK2")

        val domain = snapshot.toAppPlayerState(isSaved = true)

        assertEquals("4cOdK2", domain.base.trackId)
        assertEquals(true, domain.isTrackSaved)
    }

    @Test
    fun `toAppPlayerState keeps a confirmed false distinct from unknown`() {
        // DomainPlayerState.empty() leaves isTrackSaved null — "not answered yet". Mapping
        // an actual `false` must not collapse into that.
        assertEquals(false, PlayerStateData.empty().toAppPlayerState(isSaved = false).isTrackSaved)
    }

    private fun stateWith(
        trackId: String = "",
        isPaused: Boolean = true,
        playing: Boolean = false,
        positionMs: Long = 0,
        durationMs: Long = 0,
        trackName: String = "",
        artistName: String = "",
        albumName: String = "",
        coverId: String = "",
    ) = DomainPlayerState(
        base =
            PlayerStateData.empty().copy(
                trackId = trackId,
                isPaused = isPaused,
                playing = playing,
                positionMs = positionMs,
                durationMs = durationMs,
                trackName = trackName,
                artistName = artistName,
                albumName = albumName,
                coverId = coverId,
            ),
        isTrackSaved = null,
    )
}
