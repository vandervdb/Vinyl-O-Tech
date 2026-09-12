package org.vander.spotifyclient.domain.player

import org.vander.core.dto.TokenResponseDto

/**
 * Unused duplicate of [org.vander.spotifyclient.domain.auth.IAuthRemoteDatasource]. No file
 * imports this one — the `domain.auth` version is the live contract.
 */
fun interface IAuthRemoteDatasource {
    suspend fun fetchAccessToken(code: String): Result<TokenResponseDto>
}
