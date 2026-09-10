package org.vander.core.security.impl.storage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreKeysetRepositoryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun repository(): DataStoreKeysetRepository {
        val dataStore =
            PreferenceDataStoreFactory.create(
                produceFile = { tempFolder.newFile("keyset_test.preferences_pb") },
            )
        return DataStoreKeysetRepository(dataStore)
    }

    @Test
    fun `read returns null when nothing was written`() =
        runTest {
            assertNull(repository().read())
        }

    @Test
    fun `write then read returns the same keyset`() =
        runTest {
            val repo = repository()
            val keyset = byteArrayOf(1, 2, 3, 42, -128, 127)

            repo.write(keyset)

            assertContentEquals(keyset, repo.read())
        }

    @Test
    fun `clear removes a previously written keyset`() =
        runTest {
            val repo = repository()
            repo.write(byteArrayOf(9, 9, 9))

            repo.clear()

            assertNull(repo.read())
        }
}
