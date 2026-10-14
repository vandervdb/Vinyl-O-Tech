package org.vander.spotifyclient.data.remote.datasource

import org.vander.core.dto.UserDto

/** Raw `me` call, returning the DTO untouched. */
internal fun interface RemoteUserDataSource {
    suspend fun fetchUser(): Result<UserDto>
}
