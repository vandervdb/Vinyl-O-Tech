package org.vander.core.security.impl.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.vander.core.logger.test.FakeLogger
import org.vander.core.security.api.StoredTokensResult
import org.vander.core.security.impl.tink.FakeKeysetRepository
import org.vander.core.security.impl.tink.TinkCryptoEngine
import org.vander.core.security.impl.tink.TinkKeysetHandleProvider
import org.vander.core.security.impl.tink.aead
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Exercised against the real [TinkCryptoEngine] with an in-memory master key, the same
 * substitution the other tests of this module use: the associated-data test only means
 * something against real crypto.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSecureTokenStorageTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var logger: FakeLogger
    private lateinit var storage: DataStoreSecureTokenStorage

    @Before
    fun setUp() {
        AeadConfig.register()
        val masterKeyAead = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM")).aead()
        val cryptoEngine = TinkCryptoEngine(TinkKeysetHandleProvider(FakeKeysetRepository(), masterKeyAead))

        dataStore =
            PreferenceDataStoreFactory.create(
                produceFile = { tempFolder.newFile("token_test.preferences_pb") },
            )
        logger = FakeLogger()
        storage = DataStoreSecureTokenStorage(dataStore, cryptoEngine, logger)
    }

    @Test
    fun `get returns Empty when nothing was saved`() =
        runTest {
            assertIs<StoredTokensResult.Empty>(storage.get())
        }

    @Test
    fun `save then get returns the original tokens`() =
        runTest {
            assertTrue(storage.save("access-123", "refresh-456", expiresAt = 1_700_000_000_000).isSuccess)

            val result = assertIs<StoredTokensResult.Found>(storage.get())
            assertEquals("access-123", result.tokens.accessToken)
            assertEquals("refresh-456", result.tokens.refreshToken)
            assertEquals(1_700_000_000_000, result.tokens.expiresAt)
        }

    @Test
    fun `stored values hold no readable token`() =
        runTest {
            storage.save("access-123", "refresh-456", expiresAt = 1L)

            val stored = dataStore.data.first().asMap().values.filterIsInstance<String>()
            assertTrue(stored.isNotEmpty())
            assertTrue(stored.none { it.contains("access-123") || it.contains("refresh-456") })
        }

    @Test
    fun `an access token ciphertext moved to the refresh slot is reported as DecryptionFailed`() =
        runTest {
            storage.save("access-123", "refresh-456", expiresAt = 1L)
            val accessCipherText = dataStore.data.first()[stringPreferencesKey("access_token_cipher")]
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("refresh_token_cipher")] = requireNotNull(accessCipherText)
            }

            assertIs<StoredTokensResult.DecryptionFailed>(storage.get())
            assertTrue(
                logger.contains(FakeLogger.Entry.Level.ERROR, "DataStoreSecureTokenStorage", "could not be decrypted"),
            )
        }

    @Test
    fun `a corrupted value is reported as DecryptionFailed`() =
        runTest {
            storage.save("access-123", "refresh-456", expiresAt = 1L)
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("access_token_cipher")] = "not-base64-!!"
            }

            assertIs<StoredTokensResult.DecryptionFailed>(storage.get())
        }

    @Test
    fun `a failed read leaves the stored session untouched`() =
        runTest {
            storage.save("access-123", "refresh-456", expiresAt = 1L)
            val before = dataStore.data.first().asMap()
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("access_token_cipher")] = "not-base64-!!"
            }

            storage.get()

            // A read must not repair, purge or otherwise write: that is the caller's call.
            assertEquals(before.keys, dataStore.data.first().asMap().keys)
        }

    @Test
    fun `clear removes every stored value`() =
        runTest {
            storage.save("access-123", "refresh-456", expiresAt = 1L)

            assertTrue(storage.clear().isSuccess)

            assertIs<StoredTokensResult.Empty>(storage.get())
            assertTrue(dataStore.data.first().asMap().isEmpty())
        }
}
