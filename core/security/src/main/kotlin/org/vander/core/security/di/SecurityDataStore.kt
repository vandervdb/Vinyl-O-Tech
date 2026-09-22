package org.vander.core.security.di

import javax.inject.Qualifier

/**
 * The `security_store` Preferences DataStore owned by this module.
 *
 * Qualified because `DataStore<Preferences>` is a library type: `spotify-lib` owns a second
 * one (`spotify_prefs`), and two unqualified bindings of the same type would clash.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecurityDataStore
