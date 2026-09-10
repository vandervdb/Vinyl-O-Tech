package org.vander.core.security.impl.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.vander.core.security.api.KeysetRepository
import java.util.Base64
import javax.inject.Inject

class DataStoreKeysetRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : KeysetRepository {
        companion object {
            private val KEYSET_KEY = stringPreferencesKey("keyset")
        }

        override suspend fun read(): ByteArray? =
            dataStore.data
                .first()[KEYSET_KEY]
                ?.let { Base64.getDecoder().decode(it) }

        override suspend fun write(keyset: ByteArray) {
            dataStore.edit { preferences ->
                preferences[KEYSET_KEY] = Base64.getEncoder().encodeToString(keyset)
            }
        }

        override suspend fun clear() {
            dataStore.edit { preferences -> preferences.remove(KEYSET_KEY) }
        }
    }
