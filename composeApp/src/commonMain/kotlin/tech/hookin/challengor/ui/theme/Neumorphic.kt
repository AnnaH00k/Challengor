package tech.hookin.challengor.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Soft "raised" look for elements that share the background colour.
 * Layers, bottom to top:
 *  1. a tight near-black rim all around, so every edge stands out from the background,
 *  2. a light sage highlight to the top-left,
 *  3. a deep near-black shadow to the bottom-right.
 * Apply it before the element's own background (e.g. before a Card).
 */
fun Modifier.neumorphic(shape: Shape, elevation: Dp = 6.dp): Modifier = this
    .dropShadow(
        shape,
        Shadow(
            radius = elevation / 2,
            spread = 1.dp,
            color = ChallengorColors.NearBlack,
            alpha = 0.7f,
        ),
    )
    .dropShadow(
        shape,
        Shadow(
            radius = elevation * 2,
            color = ChallengorColors.ForestLighter,
            offset = DpOffset(-elevation / 2, -elevation / 2),
            alpha = 0.6f,
        ),
    )
    .dropShadow(
        shape,
        Shadow(
            radius = elevation * 2,
            color = ChallengorColors.NearBlack,
            offset = DpOffset(elevation / 2, elevation / 2),
            alpha = 0.85f,
        ),
    )

/** Near-black shadow cast upward from a bottom bar onto the content above it. */
fun Modifier.bottomBarShadow(elevation: Dp = 4.dp): Modifier = this
    .dropShadow(
        RectangleShape,
        Shadow(
            radius = elevation * 2,
            color = ChallengorColors.NearBlack,
            offset = DpOffset(0.dp, -elevation / 2),
            alpha = 0.5f,
        ),
    )
