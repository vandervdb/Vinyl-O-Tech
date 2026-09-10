package org.vander.spotifyclient.data.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.vander.core.logger.Logger
import org.vander.core.logger.test.FakeLogger
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class SpotifyAuthClientTest {
    open class TestableSpotifyAuthClient
        @Inject
        constructor(
            logger: Logger,
        ) : SpotifyAuthClient(logger) {
            private var nextParsed: ParsedAuth? = null

            fun stubToken(value: String) {
                nextParsed = ParsedAuth(ParsedAuth.Type.TOKEN, value = value)
            }

            fun stubCode(value: String) {
                nextParsed = ParsedAuth(ParsedAuth.Type.CODE, value = value)
            }

            fun stubError(error: String) {
                nextParsed = ParsedAuth(ParsedAuth.Type.ERROR, error = error)
            }

            fun stubOther() {
                nextParsed = ParsedAuth(ParsedAuth.Type.OTHER)
            }

            override fun parseAuthResponse(
                resultCode: Int,
                data: Intent,
            ): ParsedAuth = nextParsed ?: super.parseAuthResponse(resultCode, data)
        }

    @Test
    fun handleSpotifyAuthResult_returns_success_for_TOKEN() {
        val client = TestableSpotifyAuthClient(FakeLogger()).apply { stubToken("access-token-123") }
        var callback: Result<String>? = null
        val result = ActivityResult(Activity.RESULT_OK, Intent())

        client.handleSpotifyAuthResult(result) { callback = it }

        assertTrue(callback?.isSuccess == true)
        assertEquals("access-token-123", callback?.getOrNull())
    }

    @Test
    fun handleSpotifyAuthResult_returns_success_for_CODE() {
        val client = TestableSpotifyAuthClient(FakeLogger()).apply { stubCode("auth-code-xyz") }
        var callback: Result<String>? = null
        val result = ActivityResult(Activity.RESULT_OK, Intent())

        client.handleSpotifyAuthResult(result) { callback = it }

        assertTrue(callback?.isSuccess == true)
        assertEquals("auth-code-xyz", callback?.getOrNull())
    }

    @Test
    fun handleSpotifyAuthResult_returns_failure_for_ERROR() {
        val client = TestableSpotifyAuthClient(FakeLogger()).apply { stubError("invalid_client") }
        var callback: Result<String>? = null
        val result = ActivityResult(Activity.RESULT_OK, Intent())

        client.handleSpotifyAuthResult(result) { callback = it }

        assertTrue(callback?.isFailure == true)
    }

    @Test
    fun handleSpotifyAuthResult_returns_failure_for_OTHER() {
        val client = TestableSpotifyAuthClient(FakeLogger()).apply { stubOther() }
        var callback: Result<String>? = null
        val result = ActivityResult(Activity.RESULT_OK, Intent())

        client.handleSpotifyAuthResult(result) { callback = it }

        assertTrue(callback?.isFailure == true)
    }

    @Test
    fun handleSpotifyAuthResult_returns_failure_when_resultCode_not_OK() {
        val client = TestableSpotifyAuthClient(FakeLogger())
        var callback: Result<String>? = null
        val result = ActivityResult(Activity.RESULT_CANCELED, Intent())

        client.handleSpotifyAuthResult(result) { callback = it }

        assertTrue(callback?.isFailure == true)
    }

    @Test
    fun handleSpotifyAuthResult_returns_failure_once_when_data_is_null() {
        val client = TestableSpotifyAuthClient(FakeLogger())
        var calls = 0
        val result = ActivityResult(Activity.RESULT_OK, null)

        client.handleSpotifyAuthResult(result) { calls += 1 }

        assertEquals(1, calls)
    }
}
