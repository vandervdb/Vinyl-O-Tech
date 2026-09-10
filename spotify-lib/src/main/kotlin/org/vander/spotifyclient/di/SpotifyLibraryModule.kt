package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.repository.SpotifyLibraryRepository
import org.vander.spotifyclient.domain.repository.LibraryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpotifyLibraryModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: SpotifyLibraryRepository): LibraryRepository
}
