package org.vander.core.security.impl.tink

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class TinkKeysetHandleProviderTest {
    // Stand-in for the Android-Keystore-backed master key: a real, independent
    // Tink AEAD generated in memory. Same contract as the production key, just
    // not hardware-backed — exercises the real (de)serialization/encryption
    // code paths without needing Robolectric or a device.
    private lateinit var masterKeyAead: Aead

    @Before
    fun setUp() {
        AeadConfig.register()
        masterKeyAead = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM")).aead()
    }

    @Test
    fun `getOrCreateKeysetHandle generates and persists a keyset when nothing was stored`() =
        runTest {
            val repository = FakeKeysetRepository()
            val provider = TinkKeysetHandleProvider(repository, masterKeyAead)

            provider.getOrCreateKeysetHandle()

            assertNotNull(repository.read())
        }

    @Test
    fun `getOrCreateKeysetHandle reuses the same key across provider instances`() =
        runTest {
            val repository = FakeKeysetRepository()
            val firstHandle = TinkKeysetHandleProvider(repository, masterKeyAead).getOrCreateKeysetHandle()
            val secondHandle = TinkKeysetHandleProvider(repository, masterKeyAead).getOrCreateKeysetHandle()

            val plainText = "spotify-access-token".toByteArray()
            val encrypted = firstHandle.aead().encrypt(plainText, byteArrayOf())
            val decrypted = secondHandle.aead().decrypt(encrypted, byteArrayOf())

            assertContentEquals(plainText, decrypted)
        }

    @Test
    fun `getOrCreateKeysetHandle does not overwrite an existing keyset`() =
        runTest {
            val repository = FakeKeysetRepository()
            TinkKeysetHandleProvider(repository, masterKeyAead).getOrCreateKeysetHandle()
            val storedAfterFirstCall = repository.read()

            TinkKeysetHandleProvider(repository, masterKeyAead).getOrCreateKeysetHandle()

            assertContentEquals(storedAfterFirstCall, repository.read())
        }
}
