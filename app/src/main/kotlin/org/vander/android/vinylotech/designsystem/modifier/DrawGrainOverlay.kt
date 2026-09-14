package org.vander.android.vinylotech.designsystem.modifier

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.imageResource
import org.vander.android.vinylotech.R

/**
 * Tiles the design's noise/grain texture across the content with an
 * "overlay" blend mode — matches the CSS
 * `opacity: 0.6; mix-blend-mode: overlay; background-image: url(noise.png)`
 * applied to every screen background in the Vinyl O'Tech design.
 *
 * Uses [androidx.compose.ui.graphics.Canvas.saveLayer] with a single [Paint]
 * carrying both [alpha] and [BlendMode.Overlay] — combining alpha and a blend
 * mode directly on `drawRect`'s own parameters (tried first) washed the whole
 * background out to flat gray instead of a subtle texture; an isolated
 * `saveLayer` is the documented way to apply "alpha of an overlay-blended
 * layer" without that per-pixel interaction.
 */
fun Modifier.drawGrainOverlay(alpha: Float = 0.6f): Modifier =
    composed {
        val noise = ImageBitmap.imageResource(id = R.drawable.noise)
        val brush =
            remember(noise) {
                ShaderBrush(ImageShader(noise, tileModeX = TileMode.Repeated, tileModeY = TileMode.Repeated))
            }

        this.drawWithContent {
            drawContent()

            drawIntoCanvas { canvas ->
                val paint =
                    Paint().apply {
                        this.alpha = alpha
                        this.blendMode = BlendMode.Overlay
                    }
                canvas.saveLayer(Rect(Offset.Zero, size), paint)
                drawRect(brush = brush)
                canvas.restore()
            }
        }
    }
