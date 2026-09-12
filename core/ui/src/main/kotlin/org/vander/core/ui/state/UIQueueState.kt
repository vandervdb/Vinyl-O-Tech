package org.vander.core.ui.state

import org.vander.core.ui.domain.UIQueueItem

/**
 * Playback queue already mapped for display, so no Composable has to touch a domain
 * `Track` or a Spotify DTO.
 *
 * [empty] is the value a flow starts on, before the first queue arrives.
 */
data class UIQueueState(
    val items: List<UIQueueItem>,
) {
    companion object {
        fun empty(): UIQueueState = UIQueueState(List(0) { UIQueueItem.Companion.empty() })
    }
}
