package org.vander.core.security.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.securityDataStore by preferencesDataStore(name = "security_store")

/**
 * Provides the single `security_store` Preferences DataStore backing this module.
 *
 * `@Singleton` is load-bearing, not decoration: DataStore throws if a second instance is
 * created on the same file, so this provider has to be the only way to obtain it. The
 * `by preferencesDataStore(...)` delegate stays at file level — it is a `Context` extension,
 * and the delegate itself holds the one-instance-per-file guard.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityDataStoreProvider {
    @Provides
    @Singleton
    @SecurityDataStore
    fun provideSecurityDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.securityDataStore
}
