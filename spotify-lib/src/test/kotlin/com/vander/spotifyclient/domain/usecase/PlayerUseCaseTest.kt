package com.vander.spotifyclient.domain.usecase

import com.vander.spotifyclient.domain.player.session.FakeSpotifySessionManager
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager

class PlayerUseCaseTest(
    private val sessionUseCase: SpotifySessionManager = FakeSpotifySessionManager(),
)
