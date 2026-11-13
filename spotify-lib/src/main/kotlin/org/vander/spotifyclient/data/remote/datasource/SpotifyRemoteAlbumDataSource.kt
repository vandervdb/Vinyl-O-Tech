package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.utils.io.CancellationException
import org.vander.core.dto.SavedAlbumsResponseDto
import org.vander.spotifyclient.utils.parseSpotifyResult
import javax.inject.Inject
import javax.inject.Named

internal class SpotifyRemoteAlbumDataSource
    @Inject
    constructor(
        @param:Named("auth_api_v1_client") private val httpClient: HttpClient,
    ) : RemoteAlbumDataSource {
        override suspend fun fetchSavedAlbums(): Result<SavedAlbumsResponseDto> =
            try {
                httpClient.get("me/albums").parseSpotifyResult<SavedAlbumsResponseDto>(TAG)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }

        private companion object {
            const val TAG = "SpotifyRemoteAlbumDataSource"
        }
    }
