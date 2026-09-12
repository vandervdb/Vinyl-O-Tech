package org.vander.core.logger

/**
 * [Logger] that discards everything.
 *
 * Null object pattern: a component that takes a non-nullable [Logger] can be built without
 * one in a preview or a test, with no `?.` sprinkled through the call sites.
 */
class NoOpLogger : Logger {
    override fun d(
        tag: String,
        message: String,
    ) {
    }

    override fun i(
        tag: String,
        message: String,
    ) {
    }

    override fun w(
        tag: String,
        message: String,
    ) {
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
    }
}
