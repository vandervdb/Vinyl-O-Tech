package org.vander.spotifyclient.bridge.di

import android.app.Application
import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.auth.IAuthRepository
import org.vander.core.logger.KermitLoggerImpl
import org.vander.spotifyclient.bridge.SpotifyBridge
import org.vander.spotifyclient.bridge.SpotifyBridgeApi
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager
import org.vander.spotifyclient.domain.usecase.PlayerUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SpotifyEntryPoint {
    fun spotifySessionManager(): SpotifySessionManager

    fun authRepository(): IAuthRepository

    fun spotifyUseCase(): PlayerUseCase
}

fun obtainBridgeFromHilt(context: Context): SpotifyBridgeApi {
    val app = context.applicationContext as Application
    val entryPoint = EntryPointAccessors.fromApplication(app, SpotifyEntryPoint::class.java)
    return SpotifyBridge(
        sessionManager = entryPoint.spotifySessionManager(),
        useCase = entryPoint.spotifyUseCase(),
        authRepository = entryPoint.authRepository(),
        appContext = context.applicationContext,
        logger = KermitLoggerImpl("ANDROID_LIB"),
    )
}
