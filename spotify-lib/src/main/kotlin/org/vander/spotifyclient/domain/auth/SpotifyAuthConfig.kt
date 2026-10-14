package org.vander.spotifyclient.domain.auth

data class SpotifyAuthConfig(
    val clientId: String,
    val redirectUri: String,
)
