package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.defaultRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteRecentlyPlayedDataSourceTest {
    @Test
    fun `a cancelled call rethrows instead of reporting a failure`() =
        runTest {
            val engine = MockEngine { throw CancellationException("cancelled") }
            val dataSource = SpotifyRemoteRecentlyPlayedDataSource(clientOf(engine))

            val thrown = runCatching { dataSource.fetchRecentlyPlayed() }.exceptionOrNull()

            assertTrue("expected a CancellationException, got $thrown", thrown is CancellationException)
        }

    private fun clientOf(engine: MockEngine) = HttpClient(engine) { defaultRequest { url(BASE_URL) } }

    private companion object {
        const val BASE_URL = "https://api.spotify.com/v1/"
    }
}
