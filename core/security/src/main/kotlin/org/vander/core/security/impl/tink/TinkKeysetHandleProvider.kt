package org.vander.core.security.impl.tink

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.TinkProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.vander.core.security.api.KeysetRepository
import org.vander.core.security.di.MasterKeyAead
import javax.inject.Inject
import javax.inject.Singleton

private const val DATA_KEYSET_TEMPLATE = "AES256_GCM"
private val KEYSET_ASSOCIATED_DATA = "core-security-keyset".toByteArray()

// KeysetHandle.getPrimitive(Class) is deprecated in favor of the explicit-Configuration
// overload; RegistryConfiguration.get() reproduces the exact same (registry-based) behavior.
// Shared by TinkCryptoEngine and by this module's tests.
internal fun KeysetHandle.aead(): Aead = getPrimitive(RegistryConfiguration.get(), Aead::class.java)

/**
 * Provides the [KeysetHandle] used by [org.vander.core.security.impl.tink.TinkCryptoEngine]
 * to encrypt/decrypt app data (the "DEK" in an envelope-encryption scheme).
 *
 * [masterKeyAead] is injected rather than instantiated here (Dependency
 * Inversion): production wires an Android-Keystore-backed [Aead] (the "KEK",
 * hardware-backed, never leaves the device's secure hardware), while unit
 * tests can pass a plain in-memory Tink [Aead] — same contract, no need for
 * Robolectric or a real device to test this class's orchestration logic.
 *
 * The DEK itself never touches disk in cleartext: [TinkProtoKeysetFormat]
 * serializes AND encrypts it with [masterKeyAead] in one call before handing
 * the bytes to [keysetRepository], which only ever sees an opaque blob.
 *
 * `@Singleton` is what makes the cache below meaningful: unscoped, every injection point
 * would get its own instance, hence its own empty cache and its own chance to race.
 */
@Singleton
class TinkKeysetHandleProvider
    @Inject
    constructor(
        private val keysetRepository: KeysetRepository,
        @param:MasterKeyAead private val masterKeyAead: Aead,
    ) {
        /**
         * Guards [loadOrGenerate] so the read-then-generate-then-write sequence runs once.
         *
         * Without it, two concurrent callers on a fresh install both read `null`, both
         * generate a keyset, and the last write wins — everything encrypted with the losing
         * keyset becomes permanently undecryptable.
         *
         * A [Mutex] and not `synchronized`: the guarded operations are `suspend`, and a
         * blocking lock would hold the dispatcher's thread while waiting on disk I/O.
         */
        private val mutex = Mutex()

        /** Keeps the keyset off the disk-and-Keystore path after the first call. */
        private var cachedHandle: KeysetHandle? = null

        init {
            AeadConfig.register()
        }

        suspend fun getOrCreateKeysetHandle(): KeysetHandle =
            cachedHandle ?: mutex.withLock {
                // Re-checked inside the lock: a caller that waited here may find the handle
                // already loaded by the one that held it.
                cachedHandle ?: loadOrGenerate().also { cachedHandle = it }
            }

        private suspend fun loadOrGenerate(): KeysetHandle {
            val storedKeyset = keysetRepository.read()
            if (storedKeyset != null) {
                return TinkProtoKeysetFormat.parseEncryptedKeyset(
                    storedKeyset,
                    masterKeyAead,
                    KEYSET_ASSOCIATED_DATA,
                )
            }

            val handle = KeysetHandle.generateNew(KeyTemplates.get(DATA_KEYSET_TEMPLATE))
            keysetRepository.write(
                TinkProtoKeysetFormat.serializeEncryptedKeyset(handle, masterKeyAead, KEYSET_ASSOCIATED_DATA),
            )
            return handle
        }
    }
