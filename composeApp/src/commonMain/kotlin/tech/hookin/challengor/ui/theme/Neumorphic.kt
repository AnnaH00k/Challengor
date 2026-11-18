package tech.hookin.challengor.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import tech.hookin.challengor.data.AppTheme

/** Shadow colours and strengths for the raised look, per theme. */
private class NeumorphicColors(
    val rim: Color,
    val rimAlpha: Float,
    val highlight: Color,
    val highlightAlpha: Float,
    val shadow: Color,
    val shadowAlpha: Float,
)

private val DarkNeumorphic = NeumorphicColors(
    rim = ChallengorColors.NearBlack, rimAlpha = 0.7f,
    highlight = ChallengorColors.ForestLighter, highlightAlpha = 0.6f,
    shadow = ChallengorColors.NearBlack, shadowAlpha = 0.85f,
)

private val LightNeumorphic = NeumorphicColors(
    rim = ChallengorColors.ForestGreen, rimAlpha = 0.25f,
    highlight = lerp(ChallengorColors.Sage, ChallengorColors.White, 0.6f), highlightAlpha = 0.8f,
    shadow = ChallengorColors.ForestGreen, shadowAlpha = 0.35f,
)

private val neumorphicColors: NeumorphicColors
    @Composable get() = if (LocalAppTheme.current == AppTheme.LIGHT) LightNeumorphic else DarkNeumorphic

/**
 * Soft "raised" look for cards. Layers, bottom to top:
 *  1. a tight dark rim all around, so every edge stands out from the background,
 *  2. a light highlight to the top-left,
 *  3. a deep shadow to the bottom-right.
 * Apply it before the element's own background (e.g. before a Card).
 */
@Composable
fun Modifier.neumorphic(shape: Shape, elevation: Dp = 6.dp): Modifier {
    val colors = neumorphicColors
    return this
        .dropShadow(
            shape,
            Shadow(radius = elevation / 2, spread = 1.dp, color = colors.rim, alpha = colors.rimAlpha),
        )
        .dropShadow(
            shape,
            Shadow(
                radius = elevation * 2,
                color = colors.highlight,
                offset = DpOffset(-elevation / 2, -elevation / 2),
                alpha = colors.highlightAlpha,
            ),
        )
        .dropShadow(
            shape,
            Shadow(
                radius = elevation * 2,
                color = colors.shadow,
                offset = DpOffset(elevation / 2, elevation / 2),
                alpha = colors.shadowAlpha,
            ),
        )
}

/** Shadow cast upward from a bottom bar onto the content above it. */
@Composable
fun Modifier.bottomBarShadow(elevation: Dp = 4.dp): Modifier {
    val colors = neumorphicColors
    return this.dropShadow(
        RectangleShape,
        Shadow(
            radius = elevation * 2,
            color = colors.shadow,
            offset = DpOffset(0.dp, -elevation / 2),
            alpha = colors.shadowAlpha * 0.6f,
        ),
    )
}
