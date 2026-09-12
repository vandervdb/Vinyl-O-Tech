package org.vander.spotifyclient.domain.datasource

import org.vander.core.dto.UserDto

/** Raw `me` call, returning the DTO untouched. */
interface IRemoteUserDataSource {
    suspend fun fetchUser(): Result<UserDto>
}
