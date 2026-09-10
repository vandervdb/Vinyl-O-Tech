package org.vander.core.security.impl.tink

import org.vander.core.security.api.KeysetRepository

class FakeKeysetRepository : KeysetRepository {
    private var stored: ByteArray? = null

    override suspend fun read(): ByteArray? = stored

    override suspend fun write(keyset: ByteArray) {
        stored = keyset
    }

    override suspend fun clear() {
        stored = null
    }
}
