package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.library.LibraryRepository
import org.vander.spotifyclient.data.repository.SpotifyLibraryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SpotifyLibraryModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: SpotifyLibraryRepository): LibraryRepository
}
