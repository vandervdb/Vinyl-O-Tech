package org.vander.core.security.api

/**
 * Persists the serialized Tink keyset as an opaque blob.
 *
 * The implementation never needs to interpret those bytes: the keyset reaches it already
 * encrypted by the master key, so storage stays a dumb byte sink and the crypto stays in
 * one place.
 *
 * [read] returns `null` when no keyset was stored yet — the caller is expected to generate one.
 */
interface KeysetRepository {
    suspend fun read(): ByteArray?

    suspend fun write(keyset: ByteArray)

    suspend fun clear()
}
