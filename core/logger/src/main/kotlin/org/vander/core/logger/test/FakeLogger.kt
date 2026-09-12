package org.vander.core.logger.test

import org.vander.core.logger.Logger

/**
 * [Logger] that records every call in memory so a test can assert on it.
 *
 * Shipped in `main` rather than in `src/test` on purpose: it is consumed by the tests of
 * the other modules, which only see this module's main source set.
 *
 * Not thread-safe — entries go into a plain `MutableList`. Fine under `runTest`, not for
 * a test that logs from several real threads.
 */
class FakeLogger : Logger {
    data class Entry(
        val tag: String,
        val level: Level,
        val message: String,
        val throwable: Throwable?,
    ) {
        enum class Level { DEBUG, INFO, WARN, ERROR }
    }

    private val entries = mutableListOf<Entry>()

    override fun d(
        tag: String,
        message: String,
    ) {
        entries.add(Entry(tag, Entry.Level.DEBUG, message, null))
    }

    override fun i(
        tag: String,
        message: String,
    ) {
        entries.add(Entry(tag, Entry.Level.INFO, message, null))
    }

    override fun w(
        tag: String,
        message: String,
    ) {
        entries.add(Entry(tag, Entry.Level.WARN, message, null))
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        entries.add(Entry(tag, Entry.Level.ERROR, message, throwable))
    }

    // Helpers pratiques pour les assertions
    fun last(): Entry? = entries.lastOrNull()

    fun clear() = entries.clear()

    fun count(level: Entry.Level? = null): Int =
        if (level == null) entries.size else entries.count { it.level == level }

    fun contains(
        level: Entry.Level,
        tag: String,
        messageSubstring: String,
    ): Boolean = entries.any { it.level == level && it.tag == tag && it.message.contains(messageSubstring) }
}
