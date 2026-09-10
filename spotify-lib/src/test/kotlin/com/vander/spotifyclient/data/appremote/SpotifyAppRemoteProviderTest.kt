import android.content.Context
import app.cash.turbine.test
import com.vander.spotifyclient.data.appremote.FakeConnector
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.vander.core.domain.state.RemoteClientState
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.data.appremote.SpotifyAppRemoteProvider
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpotifyAppRemoteProviderTest {
    private val connector = FakeConnector()
    private val logger = FakeLogger()
    private val provider = SpotifyAppRemoteProvider(connector, logger)

    @Test
    fun `connect emits Connecting then Connected and stores handle`() =
        runTest {
            // GIVEN
            val context = mockk<Context>(relaxed = true)
            val fakeRemoteHandle = Any()

            val job =
                launch {
                    provider.remoteState.test {
                        assertEquals(RemoteClientState.NotConnected, awaitItem())
                        assertEquals(RemoteClientState.Connecting, awaitItem())
                        assertEquals(RemoteClientState.Connected, awaitItem())
                        cancelAndIgnoreRemainingEvents()
                    }
                }

            // WHEN
            val resultDeferred =
                async {
                    provider.connect(context)
                }
            runCurrent()

            connector.listener!!.onConnected(fakeRemoteHandle)

            val result = resultDeferred.await()

            // THEN
            assertTrue(result.isSuccess, "connect() doit renvoyer Result.success")
            assertEquals(fakeRemoteHandle, provider.getRemoteHandle())
            assertEquals(RemoteClientState.Connected, provider.remoteState.value)

            job.cancel()
        }

    @Test
    fun `connect emits Failed on error and returns failure`() =
        runTest {
            val context = mockk<Context>(relaxed = true)
            val error = RuntimeException("Boom 💥")

            val job =
                launch {
                    provider.remoteState.test {
                        assertEquals(RemoteClientState.NotConnected, awaitItem())
                        assertEquals(RemoteClientState.Connecting, awaitItem())

                        val failed = awaitItem()
                        assertTrue(failed is RemoteClientState.Failed)
                        assertEquals(error, failed.error)

                        cancelAndIgnoreRemainingEvents()
                    }
                }

            val resultDeferred =
                async {
                    provider.connect(context)
                }
            runCurrent()

            connector.listener!!.onFailure(error)

            val result = resultDeferred.await()

            assertTrue(result.isFailure)
            assertEquals(RemoteClientState.Failed(error), provider.remoteState.value)

            job.cancel()
        }

    @Test
    fun `disconnect clears handle and updates state`() =
        runTest {
            val handle = Any()
            val providerWithHandle = SpotifyAppRemoteProvider(connector, logger)

            val connectDeferred =
                async {
                    providerWithHandle.connect(mockk(relaxed = true))
                }
            runCurrent()

            connector.listener!!.onConnected(handle)
            connectDeferred.await()

            providerWithHandle.disconnect()

            assertEquals(RemoteClientState.NotConnected, providerWithHandle.remoteState.value)
            assertEquals(null, providerWithHandle.getRemoteHandle())
            assertEquals(handle, connector.disconnectedWith)
        }
}
