package org.vander.spotifyclient.data.auth

internal enum class SpotifyScope(
    val value: String,
) {
    STREAMING("streaming"),
    USER_READ_PRIVATE("user-read-private"),
    USER_READ_EMAIL("user-read-email"),
    USER_READ_PLAYBACK_STATE("user-read-playback-state"),
    USER_READ_CURRENTLY_PLAYING("user-read-currently-playing"),
    USER_MODIFY_PLAYBACK_STATE("user-modify-playback-state"),
    USER_LIBRARY_READ("user-library-read"),
    USER_LIBRARY_MODIFY("user-library-modify"),
    USER_READ_RECENTLY_PLAYED("user-read-recently-played"),
    ;

    companion object {
        val required: Array<String> = entries.map { it.value }.toTypedArray()
    }
}
