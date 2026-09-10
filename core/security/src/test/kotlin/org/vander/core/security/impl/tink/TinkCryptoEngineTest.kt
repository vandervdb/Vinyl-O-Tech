package org.vander.core.security.impl.tink

import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class TinkCryptoEngineTest {
    private lateinit var engine: TinkCryptoEngine

    @Before
    fun setUp() {
        AeadConfig.register()
        val masterKeyAead = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM")).aead()
        engine = TinkCryptoEngine(TinkKeysetHandleProvider(FakeKeysetRepository(), masterKeyAead))
    }

    @Test
    fun `encrypt then decrypt returns the original plain text`() =
        runTest {
            val plainText = "spotify-refresh-token"

            val cipherText = engine.encrypt(plainText)
            val decrypted = engine.decrypt(cipherText)

            assertContentEquals(plainText.toByteArray(), decrypted)
        }

    @Test
    fun `encrypt never returns the plain text bytes as-is`() =
        runTest {
            val plainText = "spotify-refresh-token"

            val cipherText = engine.encrypt(plainText)

            assertFalse(cipherText.contentEquals(plainText.toByteArray()))
        }

    @Test
    fun `decrypt fails when associated data does not match the one used to encrypt`() =
        runTest {
            val cipherText = engine.encrypt("spotify-refresh-token", associatedData = "refresh_token".toByteArray())

            assertFailsWith<GeneralSecurityException> {
                engine.decrypt(cipherText, associatedData = "access_token".toByteArray())
            }
        }
}
