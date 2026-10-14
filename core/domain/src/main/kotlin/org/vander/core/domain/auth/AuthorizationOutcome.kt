package org.vander.core.domain.auth

sealed interface AuthorizationOutcome {
    data class Code(
        val value: String,
    ) : AuthorizationOutcome

    data object Cancelled : AuthorizationOutcome

    data class Failed(
        val reason: String,
    ) : AuthorizationOutcome
}
