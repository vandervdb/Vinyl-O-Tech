package org.vander.spotifyclient.model.api

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
