package org.vander.core.security.api

interface CryptoEngine {
    suspend fun encrypt(
        plainText: String,
        associatedData: ByteArray? = null,
    ): ByteArray

    suspend fun decrypt(
        cipherText: ByteArray,
        associatedData: ByteArray? = null,
    ): ByteArray
}
