package org.vander.android.vinylotech.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

/**
 * Lets an element span the horizontal content padding of the lazy list or grid it sits in.
 *
 * The Accueil grid needs asymmetric side padding for its tiles (the disc sticks out on the
 * right), while its hero runs edge to edge. `contentPadding` applies to every item, so the hero
 * is measured wider than the slot it is given and placed [start] to the left. The container
 * clips to its own bounds, which include the padding, so the overflow stays visible.
 *
 * Only meaningful on an item with a bounded width — a full-span grid item, a list item.
 */
fun Modifier.fullBleed(
    start: Dp,
    end: Dp,
): Modifier =
    layout { measurable, constraints ->
        val startPx = start.roundToPx()
        val width = constraints.maxWidth + startPx + end.roundToPx()
        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        layout(constraints.maxWidth, placeable.height) { placeable.place(-startPx, 0) }
    }
