package org.vander.core.logger.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.logger.KermitLoggerImpl
import org.vander.core.logger.Logger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoggerModule {
    @Singleton
    @Provides
    fun provideLoggerModule(): Logger = KermitLoggerImpl("SpotifyClient")
}
