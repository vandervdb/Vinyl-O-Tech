package org.vander.android.vinylotech.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.domain.session.SessionManager

@EntryPoint
@InstallIn(SingletonComponent::class)
fun interface SpotifySessionEntryPoint {
    fun spotifySessionManager(): SessionManager
}
