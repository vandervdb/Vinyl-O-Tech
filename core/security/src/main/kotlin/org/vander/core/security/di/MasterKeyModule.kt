package org.vander.core.security.di

import com.google.crypto.tink.Aead
import com.google.crypto.tink.integration.android.AndroidKeystore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val MASTER_KEY_ALIAS = "vinylotech_core_security_master_key"

/**
 * Provides the KEK: an [Aead] whose key material is generated inside the Android Keystore and
 * never leaves it. It encrypts the data keyset only, never app data.
 *
 * [AndroidKeystore] is the entry point that is *not* deprecated in Tink 1.20 —
 * `AndroidKeysetManager` and `AndroidKeystoreKmsClient` both are. Generation is idempotent
 * through [AndroidKeystore.hasKey], and `@Singleton` keeps the Keystore round-trip to once
 * per process.
 *
 * Both calls throw `GeneralSecurityException` on a device whose Keystore is unusable, which
 * surfaces as a crash at the first injection. Losing the alias (uninstall, key invalidation)
 * makes every ciphertext unreadable — that is the design, not a failure mode to work around.
 */
@Module
@InstallIn(SingletonComponent::class)
object MasterKeyModule {
    @Provides
    @Singleton
    @MasterKeyAead
    fun provideMasterKeyAead(): Aead {
        if (!AndroidKeystore.hasKey(MASTER_KEY_ALIAS)) {
            AndroidKeystore.generateNewAes256GcmKey(MASTER_KEY_ALIAS)
        }
        return AndroidKeystore.getAead(MASTER_KEY_ALIAS)
    }
}
