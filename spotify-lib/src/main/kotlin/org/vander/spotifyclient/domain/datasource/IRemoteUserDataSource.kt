package org.vander.spotifyclient.domain.datasource

import org.vander.core.dto.UserDto

interface IRemoteUserDataSource {
    suspend fun fetchUser(): Result<UserDto>
}
