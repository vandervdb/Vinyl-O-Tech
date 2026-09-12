package org.vander.core.logger

/**
 * The only logging entry point for `spotify-lib`, `app` and every `core` module that
 * depends on `core-logger` — `android.util.Log` must not be called directly there.
 *
 * Keeping the interface here, free of any Android type, is what lets a pure JVM unit test
 * inject [org.vander.core.logger.test.FakeLogger] and assert on what was logged.
 * Hilt provides the production binding in `di/LoggerModule`.
 *
 * Every method takes a `tag` identifying the call site; the implementation prefixes it
 * with its own base tag.
 */
interface Logger {
    fun d(
        tag: String,
        message: String,
    )

    fun i(
        tag: String,
        message: String,
    )

    fun w(
        tag: String,
        message: String,
    )

    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}

/**
 * One-shot logging setup at application start, called once from the `Application` class.
 */
interface LoggerInitializer {
    fun init(isDebug: Boolean)
}
