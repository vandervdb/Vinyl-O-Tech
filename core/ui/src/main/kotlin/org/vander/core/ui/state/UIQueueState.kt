package org.vander.core.ui.state

import org.vander.core.ui.domain.UIQueueItem

data class UIQueueState(
    val items: List<UIQueueItem>,
) {
    companion object {
        fun empty(): UIQueueState = UIQueueState(List(0) { UIQueueItem.Companion.empty() })
    }
}
