package org.vander.android.sample.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.drawFadeEdges(
    edgeWidth: Dp = 16.dp,
    fadeColor: Color = Color.Black,
): Modifier =
    composed {
        val edgeWidthPx = with(LocalDensity.current) { edgeWidth.toPx() }

        this
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }.drawWithContent {
                drawContent()

                val width = size.width

                drawRect(
                    brush =
                        Brush.horizontalGradient(
                            colors = listOf(fadeColor, fadeColor.copy(alpha = 0f)),
                            startX = 0f,
                            endX = edgeWidthPx,
                        ),
                    size = size,
                )

                drawRect(
                    brush =
                        Brush.horizontalGradient(
                            colors = listOf(fadeColor.copy(alpha = 0f), fadeColor),
                            startX = width - edgeWidthPx,
                            endX = width,
                        ),
                    size = size,
                )
            }
    }
