package org.vander.core.ui.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.User

/**
 * Contract for the signed-in user's profile.
 *
 * [currentUser] is a `StateFlow` like the other contracts here, `null` until a profile is
 * known — so a Composable can read it without supplying an initial value of its own.
 */
interface UserViewModel {
    val currentUser: StateFlow<User?>

    /**
     * Reloads the profile and publishes it on [currentUser]. Returns immediately; a failure is
     * logged and emits `null`.
     */
    fun refresh()
}
