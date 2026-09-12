package org.vander.core.ui.presentation.viewmodel

import kotlinx.coroutines.flow.Flow
import org.vander.core.domain.data.User

/**
 * Contract for the signed-in user's profile.
 *
 * [currentUser] is a plain `Flow` and not a `StateFlow`, unlike the other ViewModel
 * contracts here — a collector gets no value until the first emission, so a Composable
 * reading it needs an initial value of its own.
 */
interface UserViewModel {
    val currentUser: Flow<User?>

    /**
     * Reloads the profile and publishes it on [currentUser]. Returns immediately; a failure is
     * logged and emits `null`.
     */
    fun refresh()
}
