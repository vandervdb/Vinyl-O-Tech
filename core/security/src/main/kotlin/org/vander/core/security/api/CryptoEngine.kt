package org.vander.core.security.api

/**
 * Authenticated symmetric encryption, with the crypto backend kept behind the interface.
 *
 * The `api/` + `impl/` split of this module exists for that reason: [org.vander.core.security.impl.tink.TinkCryptoEngine]
 * is today's backend, and a consumer that depends on this interface would survive its
 * replacement. Do not generalise that split to the other `core` modules.
 *
 * `associatedData` is authenticated but not encrypted: it must be byte-identical on
 * encrypt and decrypt, otherwise decryption fails. Use it to bind a ciphertext to its
 * context (a user id, a record key) so it cannot be replayed elsewhere.
 *
 * Both operations are `suspend` because the key material may have to be read from disk.
 */
interface CryptoEngine {
    /**
     * @param plainText encoded as UTF-8 before encryption.
     * @param associatedData authenticated but not encrypted; the exact same bytes must be
     *   passed to [decrypt] or it fails. `null` means an empty AAD.
     * @return the ciphertext, which already embeds the nonce and the authentication tag.
     */
    suspend fun encrypt(
        plainText: String,
        associatedData: ByteArray? = null,
    ): ByteArray

    /**
     * @return the decrypted bytes — not a String, unlike what [encrypt] takes: the caller
     *   decides how to decode them.
     * @throws java.security.GeneralSecurityException if the ciphertext was tampered with, the
     *   key does not match, or `associatedData` differs from the one used to encrypt.
     */
    suspend fun decrypt(
        cipherText: ByteArray,
        associatedData: ByteArray? = null,
    ): ByteArray
}
