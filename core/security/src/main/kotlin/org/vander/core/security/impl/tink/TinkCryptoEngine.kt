package org.vander.core.security.impl.tink

import org.vander.core.security.api.CryptoEngine
import javax.inject.Inject

private val NO_ASSOCIATED_DATA = ByteArray(0)

/**
 * [CryptoEngine] backed by the DEK provided by [TinkKeysetHandleProvider].
 * [associatedData] defaults to an empty byte array (Tink's [com.google.crypto.tink.Aead]
 * requires a non-null AAD) when the caller doesn't bind the ciphertext to a context.
 */
class TinkCryptoEngine
    @Inject
    constructor(
        private val keysetHandleProvider: TinkKeysetHandleProvider,
    ) : CryptoEngine {
        override suspend fun encrypt(
            plainText: String,
            associatedData: ByteArray?,
        ): ByteArray {
            val aead = keysetHandleProvider.getOrCreateKeysetHandle().aead()
            return aead.encrypt(plainText.toByteArray(Charsets.UTF_8), associatedData ?: NO_ASSOCIATED_DATA)
        }

        override suspend fun decrypt(
            cipherText: ByteArray,
            associatedData: ByteArray?,
        ): ByteArray {
            val aead = keysetHandleProvider.getOrCreateKeysetHandle().aead()
            return aead.decrypt(cipherText, associatedData ?: NO_ASSOCIATED_DATA)
        }
    }
