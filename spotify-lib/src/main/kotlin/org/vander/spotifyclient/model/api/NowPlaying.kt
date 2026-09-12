package org.vander.spotifyclient.model.api

/**
 * Flat description of the track being played, device included.
 *
 * Every field is nullable or defaulted, so a partial payload still builds.
 *
 * Note: nothing in the repo constructs or reads this class today.
 */
data class NowPlaying(
    val trackId: String? = null,
    val uri: String? = null,
    val title: String? = null,
    val artists: List<String> = emptyList(),
    val imageUrl: String? = null,
    val durationMs: Long? = null,
    val progressMs: Long? = null,
    val isPlaying: Boolean = false,
    val shuffle: Boolean? = null,
    val repeat: String? = null,
    val deviceName: String? = null,
    val deviceType: String? = null,
)
