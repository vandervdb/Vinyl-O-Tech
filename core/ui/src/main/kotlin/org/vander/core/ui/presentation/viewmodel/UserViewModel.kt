package org.vander.core.ui.presentation.viewmodel

import kotlinx.coroutines.flow.Flow
import org.vander.core.domain.data.User

interface UserViewModel {
    val currentUser: Flow<User?>

    fun refresh()
}
