package org.vander.core.domain.state

data class SavedRemotelyChangedState(
    var isSaved: Boolean = false,
    val trackId: String = "",
)
