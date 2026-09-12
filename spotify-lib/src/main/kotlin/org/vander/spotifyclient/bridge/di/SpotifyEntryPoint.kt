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

/**
 * Hilt [EntryPoint] letting a component Hilt does not build reach into the graph.
 *
 * This is the escape hatch for a host that owns its own object creation, a React Native
 * module for instance: it cannot be `@Inject`-ed, so it asks the graph for the three
 * dependencies it needs instead.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SpotifyEntryPoint {
    fun spotifySessionManager(): SpotifySessionManager

    fun authRepository(): IAuthRepository

    fun spotifyUseCase(): PlayerUseCase
}

/**
 * Builds a [SpotifyBridgeApi] outside the Hilt graph, from the application context alone.
 *
 * Note that it constructs a [SpotifyBridge] by hand rather than asking the graph for the one
 * bound in [SpotifyBridgeModule], so each call returns a new instance while the singletons it
 * depends on stay shared. Its logger is also built directly instead of being injected.
 */
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
