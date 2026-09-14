package org.vander.core.domain.data

/**
 * A Spotify resource URI, `spotify:<kind>:<id>`.
 *
 * Exists to stop a bare id and a full URI from sharing the same type. Before it, every layer
 * had to remember on its own whether the `String` it held was already prefixed — the App
 * Remote takes a full URI, the Web API takes a bare id, and the two met in the middle with
 * nothing but a parameter name to tell them apart.
 *
 * It also collapses `play` and `playPlaylist` into a single command: those were two methods
 * only because the type could not say what was being played.
 *
 * Built through the named factories, never through the constructor: [track] and [playlist]
 * are what make a track id impossible to pass where a playlist is expected.
 *
 * @property value the full URI, ready for `PlayerApi.play`.
 */
@JvmInline
value class SpotifyUri private constructor(
    val value: String,
) {
    /** The resources this app addresses. Spotify defines more; add one when it is used. */
    enum class Kind(
        val segment: String,
    ) {
        Track("track"),
        Playlist("playlist"),
        Album("album"),
        Artist("artist"),
    }

    /** `null` for a URI parsed from outside whose kind is not one of [Kind]. */
    val kind: Kind?
        get() = Kind.entries.firstOrNull { value.startsWith("$PREFIX${it.segment}$SEPARATOR") }

    /** The bare id, i.e. what the Web API expects in a path segment. */
    val id: String
        get() = value.substringAfterLast(SEPARATOR)

    override fun toString(): String = value

    companion object {
        private const val PREFIX = "spotify:"

        private const val SEPARATOR = ":"

        fun track(id: String): SpotifyUri = of(Kind.Track, id)

        fun playlist(id: String): SpotifyUri = of(Kind.Playlist, id)

        fun album(id: String): SpotifyUri = of(Kind.Album, id)

        fun artist(id: String): SpotifyUri = of(Kind.Artist, id)

        /**
         * Fails fast on a malformed id: passing an already-prefixed URI here is a programming
         * error, not a runtime condition, so it throws rather than returning a [Result] —
         * unlike [parse], which reads untrusted input.
         */
        fun of(
            kind: Kind,
            id: String,
        ): SpotifyUri {
            require(id.isNotBlank()) { "a Spotify id cannot be blank" }
            require(!id.contains(SEPARATOR)) { "expected a bare id, got '$id'" }
            return SpotifyUri("$PREFIX${kind.segment}$SEPARATOR$id")
        }

        /**
         * Reads a URI coming from outside — the App Remote's player context, the React Native
         * bridge — where the shape is not guaranteed.
         *
         * @return `null` when [raw] is not a `spotify:<kind>:<id>` triple. An unknown kind is
         *   accepted and surfaces as a `null` [kind]: Spotify may address resources this enum
         *   does not list, and refusing them here would lose the URI entirely.
         */
        fun parse(raw: String): SpotifyUri? {
            val parts = raw.split(SEPARATOR)
            if (parts.size != 3) return null
            if (parts[0] != PREFIX.removeSuffix(SEPARATOR)) return null
            if (parts[1].isBlank() || parts[2].isBlank()) return null
            return SpotifyUri(raw)
        }
    }
}
