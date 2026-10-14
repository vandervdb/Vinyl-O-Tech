package org.vander.core.security.impl.tink

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.vander.core.logger.test.FakeLogger
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
            val provider = TinkKeysetHandleProvider(repository, masterKeyAead, FakeLogger())

            provider.getOrCreateKeysetHandle()

            assertNotNull(repository.read())
        }

    @Test
    fun `getOrCreateKeysetHandle reuses the same key across provider instances`() =
        runTest {
            val repository = FakeKeysetRepository()
            val firstHandle =
                TinkKeysetHandleProvider(
                    repository,
                    masterKeyAead,
                    FakeLogger(),
                ).getOrCreateKeysetHandle()
            val secondHandle =
                TinkKeysetHandleProvider(
                    repository,
                    masterKeyAead,
                    FakeLogger(),
                ).getOrCreateKeysetHandle()

            val plainText = "spotify-access-token".toByteArray()
            val encrypted = firstHandle.aead().encrypt(plainText, byteArrayOf())
            val decrypted = secondHandle.aead().decrypt(encrypted, byteArrayOf())

            assertContentEquals(plainText, decrypted)
        }

    @Test
    fun `getOrCreateKeysetHandle does not overwrite an existing keyset`() =
        runTest {
            val repository = FakeKeysetRepository()
            TinkKeysetHandleProvider(repository, masterKeyAead, FakeLogger()).getOrCreateKeysetHandle()
            val storedAfterFirstCall = repository.read()

            TinkKeysetHandleProvider(repository, masterKeyAead, FakeLogger()).getOrCreateKeysetHandle()

            assertContentEquals(storedAfterFirstCall, repository.read())
        }

    @Test
    fun `getOrCreateKeysetHandle replaces a keyset the master key can no longer decrypt`() =
        runTest {
            // What an Auto Backup restore leaves behind: the keyset file comes back, the
            // Keystore master key that encrypted it does not.
            val repository = FakeKeysetRepository()
            val lostMasterKey = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM")).aead()
            TinkKeysetHandleProvider(repository, lostMasterKey, FakeLogger()).getOrCreateKeysetHandle()
            val restoredKeyset = repository.read()
            val logger = FakeLogger()

            val handle = TinkKeysetHandleProvider(repository, masterKeyAead, logger).getOrCreateKeysetHandle()

            val plainText = "spotify-access-token".toByteArray()
            assertContentEquals(
                plainText,
                handle.aead().decrypt(handle.aead().encrypt(plainText, byteArrayOf()), byteArrayOf()),
            )
            assertFalse(restoredKeyset.contentEquals(repository.read()))
            assertTrue(
                logger.contains(FakeLogger.Entry.Level.ERROR, "TinkKeysetHandleProvider", "could not be decrypted"),
            )
        }
}
