package org.vander.spotifyclient.domain.player

import org.vander.core.dto.TokenResponseDto

fun interface IAuthRemoteDatasource {
    suspend fun fetchAccessToken(code: String): Result<TokenResponseDto>
}
