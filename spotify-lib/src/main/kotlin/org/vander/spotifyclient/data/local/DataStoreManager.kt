package org.vander.spotifyclient.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.auth.IDataStoreManager
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "spotify_prefs")

/**
 * Token storage on top of a Preferences DataStore named `spotify_prefs`.
 *
 * Every operation is wrapped so an [IOException] comes back as a failed [Result] rather than
 * escaping the data layer. `@Singleton` matters here: DataStore refuses more than one active
 * instance per file, so a second provider of this class would fail at runtime.
 *
 * A few caveats in the current state:
 * - the three refresh-token methods are `TODO("Not yet implemented")` and throw if called.
 * - the same `Context.dataStore` delegate is declared twice, once at file level and once in
 *   the class, both on `spotify_prefs`. Only the class-level one is reachable; the
 *   file-level one is never touched, which is the only reason the duplicate does not blow up.
 * - [saveAccessToken] logs the token itself at debug level.
 */
@Singleton
class DataStoreManager
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val logger: Logger,
    ) : IDataStoreManager {
        companion object {
            private const val TAG = "DataStoreManager"
            private const val DATASTORE_NAME = "spotify_prefs"
            private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
            private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        }

        private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

        override val accessTokenFlow: Flow<String?> =
            context.dataStore.data
                .map { preferences -> preferences[ACCESS_TOKEN_KEY] }

        override suspend fun saveAccessToken(token: String): Result<Unit> =
            try {
                context.dataStore.edit { preferences ->
                    logger.d(TAG, "Saving access token: $token")
                    preferences[ACCESS_TOKEN_KEY] = token
                }
                Result.success(Unit)
            } catch (e: IOException) {
                Result.failure(e)
            }

        override suspend fun getAccessToken(): Result<String> =
            try {
                val token =
                    context.dataStore.data
                        .map { preferences ->
                            preferences[ACCESS_TOKEN_KEY] ?: ""
                        }.first()
                Result.success(token)
            } catch (e: IOException) {
                Result.failure(e)
            }

        override suspend fun clearAccessToken(): Result<Unit> =
            try {
                context.dataStore.edit { preferences ->
                    logger.d(TAG, "Clearing access token")
                    preferences.remove(ACCESS_TOKEN_KEY)
                }
                Result.success(Unit)
            } catch (e: IOException) {
                Result.failure(e)
            }

        override suspend fun saveRefreshToken(token: String): Result<Unit> {
            TODO("Not yet implemented")
        }

        override suspend fun getRefreshToken(): Result<String> {
            TODO("Not yet implemented")
        }

        override suspend fun clearRefreshToken(): Result<Unit> {
            TODO("Not yet implemented")
        }
    }
