package org.vander.core.security.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.security.api.CryptoEngine
import org.vander.core.security.api.KeysetRepository
import org.vander.core.security.api.SecureTokenStorage
import org.vander.core.security.impl.storage.DataStoreKeysetRepository
import org.vander.core.security.impl.storage.DataStoreSecureTokenStorage
import org.vander.core.security.impl.tink.TinkCryptoEngine
import javax.inject.Singleton

/**
 * Binds each `api/` contract to its `impl/` counterpart — the one place in the module allowed
 * to name an implementation type. Consumers inject the interface.
 *
 * `@Binds` rather than `@Provides`: Hilt builds all three by constructor injection, so an
 * explicit factory would only add a body to keep in sync. `@Provides` is reserved for what
 * Hilt cannot construct — the `DataStore` and the Keystore-backed `Aead`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityBindingsModule {
    @Binds
    @Singleton
    abstract fun bindKeysetRepository(impl: DataStoreKeysetRepository): KeysetRepository

    @Binds
    @Singleton
    abstract fun bindCryptoEngine(impl: TinkCryptoEngine): CryptoEngine

    @Binds
    @Singleton
    abstract fun bindSecureTokenStorage(impl: DataStoreSecureTokenStorage): SecureTokenStorage
}
