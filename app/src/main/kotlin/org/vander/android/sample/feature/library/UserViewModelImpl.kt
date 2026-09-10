package org.vander.android.sample.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.vander.core.ui.presentation.viewmodel.UserViewModel
import org.vander.spotifyclient.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
open class UserViewModelImpl
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
