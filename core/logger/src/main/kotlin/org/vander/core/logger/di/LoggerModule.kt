package org.vander.core.logger.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.logger.KermitLoggerImpl
import org.vander.core.logger.Logger
import javax.inject.Singleton

/**
 * Binds [Logger] to the Kermit implementation for the whole application.
 *
 * `@Provides` and not `@Binds` because [KermitLoggerImpl] is built with a base tag Hilt
 * cannot guess, and `@Singleton` so every consumer shares one Kermit configuration.
 */
@Module
@InstallIn(SingletonComponent::class)
object LoggerModule {
    @Singleton
    @Provides
    fun provideLoggerModule(): Logger = KermitLoggerImpl("SpotifyClient")
}
