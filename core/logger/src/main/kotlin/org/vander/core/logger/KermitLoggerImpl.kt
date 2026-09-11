package org.vander.core.logger

import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import co.touchlab.kermit.Logger as KermitLogger

/**
 * [baseTag] identifies the component that owns this logger ("SpotifyClient",
 * "APP"); the `tag` of each call identifies the call site. Both end up in the
 * emitted tag, composed as `baseTag/tag`.
 */
class KermitLoggerImpl(
    private val baseTag: String = "KERMIT",
    config: LoggerConfig = StaticConfig(logWriterList = listOf(platformLogWriter())),
) : Logger {
    private val logger = KermitLogger(config)

    // Kermit's `withTag` REPLACES the tag, it does not append. Tagging the
    // instance with baseTag and again at every call would silently drop it,
    // leaving the constructor parameter with no observable effect.
    private fun at(tag: String) = logger.withTag(if (tag.isEmpty()) baseTag else "$baseTag/$tag")

    override fun d(
        tag: String,
        message: String,
    ) {
        at(tag).d { message }
    }

    override fun i(
        tag: String,
        message: String,
    ) {
        at(tag).i { message }
    }

    override fun w(
        tag: String,
        message: String,
    ) {
        at(tag).w { message }
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (throwable != null) {
            at(tag).e(throwable) { message }
        } else {
            at(tag).e { message }
        }
    }
}

class KermitLoggerInitializer : LoggerInitializer {
    override fun init(isDebug: Boolean) {
        // DoNothing
    }
}
