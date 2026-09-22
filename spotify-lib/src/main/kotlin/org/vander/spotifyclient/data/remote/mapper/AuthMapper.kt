package org.vander.spotifyclient.data.remote.mapper

import org.vander.core.domain.data.TokenResponse
import org.vander.core.dto.TokenResponseDto

/**
 * DTO-to-domain mapper for the accounts token endpoint.
 *
 * `expires_in` is a duration counted from the moment the token was issued, so the absolute
 * expiry has to be computed as close to reception as possible: every millisecond spent
 * between the response and this call is lifetime the app believes it still has.
 *
 * [now] is a parameter rather than an inline `System.currentTimeMillis()` so a test can pin it.
 */
fun TokenResponseDto.toDomain(now: Long = System.currentTimeMillis()): TokenResponse =
    TokenResponse(
        accessToken = accessToken,
        expiresAt = now + expiresIn * 1_000L,
        refreshToken = refreshToken,
    )
