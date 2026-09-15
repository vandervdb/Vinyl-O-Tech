package org.vander.core.domain.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackContextTest {
    @Test
    fun `a playlist context exposes its id`() {
        val context = PlaybackContext(uri = SpotifyUri.playlist(PLAYLIST_ID), title = "Sillons")

        assertEquals(PLAYLIST_ID, context.playlistId)
    }

    @Test
    fun `an album context exposes no playlist id`() {
        // An album playing must not light up a playlist tile, even one sharing the id.
        val context = PlaybackContext(uri = SpotifyUri.album(PLAYLIST_ID))

        assertNull(context.playlistId)
        assertFalse(context.isPlaying(PLAYLIST_ID))
    }

    @Test
    fun `an artist context exposes no playlist id either`() {
        assertNull(PlaybackContext(uri = SpotifyUri.artist(PLAYLIST_ID)).playlistId)
    }

    @Test
    fun `isPlaying answers true for the playing playlist`() {
        val context = PlaybackContext(uri = SpotifyUri.playlist(PLAYLIST_ID))

        assertTrue(context.isPlaying(PLAYLIST_ID))
        assertFalse(context.isPlaying("another"))
    }

    @Test
    fun `None matches nothing`() {
        assertNull(PlaybackContext.None.uri)
        assertNull(PlaybackContext.None.playlistId)
        assertFalse(PlaybackContext.None.isPlaying(PLAYLIST_ID))
    }

    @Test
    fun `a context whose uri did not parse keeps its labels`() {
        // Built the way the mapper does when the SDK sends something unexpected: the kind is
        // lost, the name shown on screen is not.
        val context = PlaybackContext(uri = null, title = "Sillons", subtitle = "Vander")

        assertNull(context.playlistId)
        assertEquals("Sillons", context.title)
        assertEquals("Vander", context.subtitle)
    }

    @Test
    fun `a kind the enum does not list yields no playlist id`() {
        // Spotify addresses resources Kind does not cover — a station, a show. `parse` keeps
        // the URI, and `playlistId` simply does not claim it is a playlist.
        val context = PlaybackContext(uri = SpotifyUri.parse("spotify:station:$PLAYLIST_ID"))

        assertNull(context.playlistId)
    }

    private companion object {
        const val PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M"
    }
}
