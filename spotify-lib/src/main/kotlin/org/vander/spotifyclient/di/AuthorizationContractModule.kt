package org.vander.spotifyclient.di

import androidx.activity.result.contract.ActivityResultContract
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import org.vander.core.domain.auth.AuthorizationOptions
import org.vander.core.domain.auth.AuthorizationOutcome
import org.vander.spotifyclient.data.auth.SpotifyAuthorizationContract

/**
 * Exposes the Spotify authorization flow as a plain `ActivityResultContract`, so `app` launches
 * it without knowing `SpotifyAuthConfig` or the SDK behind it.
 */
@Module
@InstallIn(ViewModelComponent::class)
internal abstract class AuthorizationContractModule {
    @Binds
    abstract fun bindAuthorizationContract(
        impl: SpotifyAuthorizationContract,
    ): ActivityResultContract<AuthorizationOptions, AuthorizationOutcome>
}
