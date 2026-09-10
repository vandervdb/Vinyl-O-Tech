package org.vander.core.logger.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.vander.core.logger.Logger
import org.vander.core.logger.test.FakeLogger
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [LoggerModule::class],
)
object TestLoggerModule {
    @Provides
    @Singleton
    fun provideFakeLogger(): Logger = FakeLogger()
}
