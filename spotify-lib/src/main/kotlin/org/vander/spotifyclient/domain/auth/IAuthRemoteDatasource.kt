package org.vander.spotifyclient.domain.auth

import org.vander.core.dto.TokenResponseDto

interface IAuthRemoteDatasource {
    suspend fun fetchAccessToken(code: String): Result<TokenResponseDto>
}
