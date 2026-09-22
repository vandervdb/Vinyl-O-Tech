package org.vander.spotifyclient.data.remote.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.dto.PlayContextDto
import org.vander.core.dto.PlayHistoryDto
import org.vander.core.dto.RecentlyPlayedResponseDto
import org.vander.spotifyclient.fixtures.trackDto

class RecentlyPlayedMapperTest {
    @Test
    fun `a page keeps its plays in order, most recent first`() {
        val dto =
            RecentlyPlayedResponseDto(
                items =
                    listOf(
                        play(trackId = "t1", playedAt = "2026-09-18T09:03:41Z"),
                        play(trackId = "t2", playedAt = "2026-09-18T08:51:02Z"),
                    ),
            )

        val domain = dto.toDomain()

        assertEquals(listOf("t1", "t2"), domain.items.map { it.track.id })
        assertEquals("2026-09-18T09:03:41Z", domain.items.first().playedAt)
    }

    @Test
    fun `the context URI is parsed into a typed URI`() {
        val domain = play(contextUri = "spotify:playlist:$PLAYLIST_ID").toDomain()

        assertEquals(SpotifyUri.playlist(PLAYLIST_ID), domain.contextUri)
        assertEquals(SpotifyUri.Kind.Playlist, domain.contextUri?.kind)
    }

    @Test
    fun `a track played on its own has no context`() {
        assertNull(play(contextUri = null).toDomain().contextUri)
    }

    @Test
    fun `a context URI of an unexpected shape becomes null rather than failing`() {
        // Parsing here rather than at playback time: a malformed URI can no longer reach
        // `dispatch(Play(...))`.
        assertNull(play(contextUri = "spotify:user:vander:playlist:$PLAYLIST_ID").toDomain().contextUri)
    }

    @Test
    fun `an empty page maps to an empty history`() {
        assertTrue(RecentlyPlayedResponseDto().toDomain().items.isEmpty())
    }

    // --- lastResumable

    @Test
    fun `lastResumable skips the plays that carry no context`() {
        val dto =
            RecentlyPlayedResponseDto(
                items =
                    listOf(
                        play(trackId = "alone", contextUri = null),
                        play(trackId = "fromPlaylist", contextUri = "spotify:playlist:$PLAYLIST_ID"),
                    ),
            )

        val resumable = dto.toDomain().lastResumable

        assertEquals("fromPlaylist", resumable?.track?.id)
        assertEquals(PLAYLIST_ID, resumable?.contextUri?.id)
    }

    @Test
    fun `lastResumable is null when nothing in the history can be resumed`() {
        val dto = RecentlyPlayedResponseDto(items = listOf(play(contextUri = null)))

        assertNull(dto.toDomain().lastResumable)
    }

    @Test
    fun `lastResumable is null on an empty history`() {
        assertNull(RecentlyPlayedResponseDto().toDomain().lastResumable)
    }

    private fun play(
        trackId: String = "track",
        playedAt: String = "2026-09-18T09:03:41Z",
        contextUri: String? = "spotify:playlist:$PLAYLIST_ID",
    ) = PlayHistoryDto(
        track = trackDto(id = trackId),
        playedAt = playedAt,
        context = contextUri?.let { PlayContextDto(type = "playlist", uri = it) },
    )

    private companion object {
        const val PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M"
    }
}
