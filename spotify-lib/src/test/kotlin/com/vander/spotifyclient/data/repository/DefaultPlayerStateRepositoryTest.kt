package com.vander.spotifyclient.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SavedRemotelyChangedState
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.data.repository.DefaultPlayerStateRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultPlayerStateRepositoryTest {
    val fakeClient = FakeSpotifyPlayerClient()
    val logger = FakeLogger()

    @Test
    fun `startListening propage les etats du player`() =
        runTest {
            val repo = DefaultPlayerStateRepository(fakeClient, logger)

            val expected =
                PlayerStateData(
                    trackName = "Song",
                    artistName = "Artist",
                    albumName = "Album",
                    coverId = "cover",
                    trackId = "t1",
                    isPaused = false,
                    playing = true,
                    paused = false,
                    stopped = false,
                    shuffling = false,
                    repeating = false,
                    seeking = false,
                    skippingNext = false,
                    skippingPrevious = false,
                    positionMs = 123,
                    durationMs = 456,
                )

            repo.playerStateData.test {
                assertEquals(PlayerStateData.empty(), awaitItem())

                // WHEN
                repo.startListening()
                runCurrent()
                fakeClient.emit(expected)

                // THEN
                assertEquals(expected, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `reception du meme etat emet un SavedRemotelyChangedState true puis reset`() =
        runTest {
            val repo = DefaultPlayerStateRepository(fakeClient, logger)

            val state =
                PlayerStateData(
                    trackName = "Song",
                    artistName = "Artist",
                    albumName = "Album",
                    coverId = "cover",
                    trackId = "t1",
                    isPaused = false,
                    playing = true,
                    paused = false,
                    stopped = false,
                    shuffling = false,
                    repeating = false,
                    seeking = false,
                    skippingNext = false,
                    skippingPrevious = false,
                    positionMs = 10,
                    durationMs = 100,
                )

            repo.savedRemotelyChangedState.test {
                // initial false
                val initial = awaitItem()
                assertFalse(initial.isSaved)

                repo.startListening()
                runCurrent()
                fakeClient.emit(state)
                runCurrent()

                fakeClient.emit(state)

                val toggled: SavedRemotelyChangedState = awaitItem()
                assertTrue(toggled.isSaved)
                assertEquals("t1", toggled.trackId)

                val reset: SavedRemotelyChangedState = awaitItem()
                assertFalse(reset.isSaved)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
