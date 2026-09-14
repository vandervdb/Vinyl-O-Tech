package org.vander.core.domain.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpotifyUriTest {
    @Test
    fun `track builds a track URI from a bare id`() {
        assertEquals("spotify:track:4cOdK2wGLETKBW3PvgPWqT", SpotifyUri.track(TRACK_ID).value)
    }

    @Test
    fun `playlist builds a playlist URI from a bare id`() {
        assertEquals("spotify:playlist:37i9dQZF1DXcBWIGoYBM5M", SpotifyUri.playlist(PLAYLIST_ID).value)
    }

    @Test
    fun `factories of the same id produce different URIs`() {
        // The whole point of the type: a bare id alone never says what it addresses.
        assertNotEquals(SpotifyUri.track(TRACK_ID), SpotifyUri.album(TRACK_ID))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `of rejects an already prefixed URI`() {
        SpotifyUri.track("spotify:track:$TRACK_ID")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `of rejects a blank id`() {
        SpotifyUri.playlist("  ")
    }

    @Test
    fun `id gives back the bare id`() {
        assertEquals(PLAYLIST_ID, SpotifyUri.playlist(PLAYLIST_ID).id)
    }

    @Test
    fun `kind is read from the URI`() {
        assertEquals(SpotifyUri.Kind.Playlist, SpotifyUri.playlist(PLAYLIST_ID).kind)
        assertEquals(SpotifyUri.Kind.Track, SpotifyUri.track(TRACK_ID).kind)
    }

    @Test
    fun `parse accepts a well formed URI`() {
        val parsed = SpotifyUri.parse("spotify:playlist:$PLAYLIST_ID")

        assertEquals(SpotifyUri.playlist(PLAYLIST_ID), parsed)
    }

    @Test
    fun `parse keeps a URI whose kind is unknown`() {
        // Spotify addresses resources Kind does not list (stations, shows, local files).
        // Dropping them would lose the URI; only the typed kind is unavailable.
        val parsed = SpotifyUri.parse("spotify:station:$PLAYLIST_ID")

        assertEquals("spotify:station:$PLAYLIST_ID", parsed?.value)
        assertNull(parsed?.kind)
    }

    @Test
    fun `parse rejects anything that is not a three part spotify URI`() {
        assertNull(SpotifyUri.parse("spotify:track"))
        assertNull(SpotifyUri.parse("spotify:track:$TRACK_ID:extra"))
        assertNull(SpotifyUri.parse("https://open.spotify.com/track/$TRACK_ID"))
        assertNull(SpotifyUri.parse("deezer:track:$TRACK_ID"))
        assertNull(SpotifyUri.parse("spotify::$TRACK_ID"))
        assertNull(SpotifyUri.parse("spotify:track:"))
        assertNull(SpotifyUri.parse(""))
    }

    @Test
    fun `parse and the factories agree on the same resource`() {
        val built = SpotifyUri.track(TRACK_ID)
        val parsed = SpotifyUri.parse(built.value)

        assertEquals(built, parsed)
        assertEquals(built.id, parsed?.id)
        assertEquals(built.kind, parsed?.kind)
    }

    private companion object {
        const val TRACK_ID = "4cOdK2wGLETKBW3PvgPWqT"

        const val PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M"
    }
}
