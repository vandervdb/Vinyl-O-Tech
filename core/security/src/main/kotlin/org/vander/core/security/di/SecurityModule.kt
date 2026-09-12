package org.vander.core.security.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.securityDataStore by preferencesDataStore(
    name = "security_store",
)

/**
 * Earlier home of this module's Hilt wiring, now an empty class kept alongside
 * [SecurityDataStoreProvider] while the DI is being moved.
 *
 * The only live declaration in this file is the private `securityDataStore` extension,
 * which creates the `security_store` Preferences DataStore. It is file-private and
 * therefore unreachable from any Hilt module — nothing consumes it today.
 */
class SecurityModule
