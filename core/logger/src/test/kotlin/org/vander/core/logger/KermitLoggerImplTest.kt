package org.vander.core.logger

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Captures what Kermit actually emits, so the tag composition can be asserted
 * without an Android runtime — [KermitLoggerImpl] takes its config by
 * constructor precisely to make this possible.
 */
private class RecordingWriter : LogWriter() {
    data class Entry(
        val severity: Severity,
        val message: String,
        val tag: String,
        val throwable: Throwable?,
    )

    val entries = mutableListOf<Entry>()

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        entries += Entry(severity, message, tag, throwable)
    }
}

class KermitLoggerImplTest {
    private val writer = RecordingWriter()
    private val logger = KermitLoggerImpl("APP", StaticConfig(logWriterList = listOf(writer)))

    @Test
    fun `base tag survives the per-call tag`() {
        logger.d("Connection", "hello")

        assertEquals("APP/Connection", writer.entries.single().tag)
    }

    @Test
    fun `base tag alone is used when the call site gives none`() {
        logger.i("", "hello")

        assertEquals("APP", writer.entries.single().tag)
    }

    @Test
    fun `error carries its throwable`() {
        val boom = IllegalStateException("boom")

        logger.e("Auth", "authorization failed", boom)

        val entry = writer.entries.single()
        assertEquals(Severity.Error, entry.severity)
        assertEquals("APP/Auth", entry.tag)
        assertEquals("authorization failed", entry.message)
        assertEquals(boom, entry.throwable)
    }
}
