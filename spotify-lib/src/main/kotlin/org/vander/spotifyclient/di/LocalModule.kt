package org.vander.spotifyclient.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.local.DataStoreManager
import org.vander.spotifyclient.domain.auth.IDataStoreManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalModule {
    @Binds
    @Singleton
    abstract fun bindDataStoreManager(impl: DataStoreManager): IDataStoreManager

    companion object {
        @Provides
        @Singleton
        fun provideDataStoreManager(
            @ApplicationContext context: Context,
            logger: Logger,
        ): DataStoreManager = DataStoreManager(context, logger)
    }
}
