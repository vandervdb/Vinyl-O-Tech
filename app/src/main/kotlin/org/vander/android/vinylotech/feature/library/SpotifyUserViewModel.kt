package org.vander.android.vinylotech.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.vander.core.domain.user.UserRepository
import org.vander.core.ui.presentation.viewmodel.UserViewModel
import javax.inject.Inject

@HiltViewModel
open class SpotifyUserViewModel
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) : ViewModel(),
        UserViewModel {
        override val currentUser = userRepository.currentUser

        override fun refresh() {
            viewModelScope.launch {
                userRepository.fetchCurrentUser()
            }
        }
    }
