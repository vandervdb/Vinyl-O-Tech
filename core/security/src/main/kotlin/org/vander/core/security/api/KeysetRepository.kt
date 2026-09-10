package org.vander.core.security.api

interface KeysetRepository {
    suspend fun read(): ByteArray?

    suspend fun write(keyset: ByteArray)

    suspend fun clear()
}
