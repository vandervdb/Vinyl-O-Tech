package org.vander.core.security.di

/**
 * Placeholder for this module's Hilt wiring: an empty class, with no `@Module` and no
 * binding yet.
 *
 * `core-security` is work in progress and is not declared as a dependency by any other
 * module, so nothing provides the `DataStore<Preferences>` that [org.vander.core.security.impl.storage.DataStoreKeysetRepository]
 * and the `Aead` that [org.vander.core.security.impl.tink.TinkKeysetHandleProvider] expect.
 */
class SecurityDataStoreProvider
