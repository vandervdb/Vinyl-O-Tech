package org.vander.android.sample.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.vander.android.sample.designsystem.modifier.drawFadeEdges
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Horizontal marquee for [text] that adapts to the space the parent offers:
 * if the text fits without truncation it is shown once, static, with no
 * animation; only when it overflows does it scroll continuously
 * right-to-left and loop seamlessly, with a fade-out gradient at both edges.
 *
 * How the adaptation works: the whole composable is wrapped in a
 * [BoxWithConstraints], whose `maxWidth` reflects the width the *parent*
 * actually offers (known in Dp at composition time — this is the standard
 * Compose way to react to incoming constraints, as opposed to reading them
 * inside a custom [Layout]'s measure lambda, which runs too late to gate
 * whether the scrolling `LaunchedEffect` should even start). That width is
 * compared, in pixels, against the text's own intrinsic width from
 * [rememberTextMeasurer] (measured unconstrained, so it reflects the text's
 * natural, un-wrapped width). Only when the text is wider than what the
 * parent offers do we compose the looping variant (two duplicated copies +
 * custom [Layout], see inline comment there).
 *
 * @param speedPxPerSecond scroll speed in raw pixels/second (not dp-scaled), used only while scrolling
 * @param spacer gap between the end of one loop and the start of the next, used only while scrolling
 * @param fadeEdgeWidth width of the edge fade-out gradient, used only while scrolling
 * @param fadeColor color the text fades into — should match the surface behind it
 */
@Composable
@Suppress("FunctionNaming")
fun MarqueeTextInfinite(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    speedPxPerSecond: Float = 60f,
    spacer: Dp = 24.dp,
    fadeEdgeWidth: Dp = 16.dp,
    fadeColor: Color = MaterialTheme.colorScheme.surface,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val measured =
        remember(text, textStyle) {
            textMeasurer.measure(text = text, style = textStyle)
        }

    val textWidthPx = measured.size.width
    val textHeightPx = measured.size.height
    val spacerPx = with(density) { spacer.toPx().roundToInt() }
    val totalLoopWidth = textWidthPx + spacerPx

    BoxWithConstraints(modifier = modifier) {
        val availableWidthPx = with(density) { maxWidth.toPx() }

        if (textWidthPx <= availableWidthPx) {
            BasicText(
                text = text,
                style = textStyle.copy(color = MaterialTheme.colorScheme.onBackground),
                maxLines = 1,
                softWrap = false,
            )
            return@BoxWithConstraints
        }

        val offsetX = remember { Animatable(0f) }

        LaunchedEffect(totalLoopWidth) {
            while (true) {
                offsetX.snapTo(0f)
                offsetX.animateTo(
                    targetValue = -totalLoopWidth.toFloat(),
                    animationSpec =
                        tween(
                            durationMillis = ((totalLoopWidth / speedPxPerSecond) * 1000).toInt(),
                            easing = EaseInOutQuad,
                        ),
                )
            }
        }

        Layout(
            modifier =
                Modifier
                    .clipToBounds()
                    .drawFadeEdges(edgeWidth = fadeEdgeWidth, fadeColor = fadeColor),
            content = {
                BasicText(
                    text = text,
                    style = textStyle.copy(color = MaterialTheme.colorScheme.onBackground),
                    maxLines = 1,
                    softWrap = false,
                )
                BasicText(
                    text = text,
                    style = textStyle.copy(color = MaterialTheme.colorScheme.onBackground),
                    maxLines = 1,
                    softWrap = false,
                )
            },
        ) { measurables, constraints ->
            val safeMaxWidth = max(totalLoopWidth, constraints.minWidth)

            val placeables =
                measurables.map {
                    it.measure(constraints.copy(maxWidth = safeMaxWidth))
                }

            layout(width = constraints.maxWidth, height = textHeightPx) {
                // Two copies of the text placed back-to-back (second one shifted by
                // totalLoopWidth) and translated together by the same offset: once the
                // first copy has scrolled out by totalLoopWidth, the second copy sits
                // exactly where the first started, so the snapTo(0f) reset in
                // LaunchedEffect above is visually seamless — no gap or jump.
                val offset = offsetX.value.roundToInt()
                placeables[0].placeRelative(x = offset, y = 0)
                placeables[1].placeRelative(x = offset + totalLoopWidth, y = 0)
            }
        }
    }
}
