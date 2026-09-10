package org.vander.fake.spotify

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.data.User
import org.vander.core.ui.presentation.viewmodel.UserViewModel

class FakeUserViewModel : UserViewModel {
    private val _currentUser =
        MutableStateFlow(
            User(
                name = "",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273123323323323323323323323",
            ),
        )
    override val currentUser = _currentUser.asStateFlow()

    override fun refresh() = Unit
}
