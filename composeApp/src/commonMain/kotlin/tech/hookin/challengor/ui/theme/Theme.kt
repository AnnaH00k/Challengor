package tech.hookin.challengor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import tech.hookin.challengor.data.AppTheme
import tech.hookin.challengor.resources.Res
import tech.hookin.challengor.resources.logo_challengor
import tech.hookin.challengor.resources.logo_challengor_light
import tech.hookin.challengor.ui.theme.ChallengorColors.ForestDeep
import tech.hookin.challengor.ui.theme.ChallengorColors.ForestGreen
import tech.hookin.challengor.ui.theme.ChallengorColors.ForestLight
import tech.hookin.challengor.ui.theme.ChallengorColors.ForestLighter
import tech.hookin.challengor.ui.theme.ChallengorColors.NearBlack
import tech.hookin.challengor.ui.theme.ChallengorColors.Rust
import tech.hookin.challengor.ui.theme.ChallengorColors.Sage

/** Forest green surfaces with sage content: the dark page, and the cards of the light theme. */
private val ForestColorScheme = darkColorScheme(
    primary = Sage,
    onPrimary = ForestGreen,
    primaryContainer = NearBlack,
    onPrimaryContainer = Sage,
    secondary = Sage,
    onSecondary = ForestGreen,
    secondaryContainer = ForestLighter,
    onSecondaryContainer = Sage,
    tertiary = Sage,
    onTertiary = ForestGreen,
    background = ForestGreen,
    onBackground = Sage,
    surface = ForestGreen,
    onSurface = Sage,
    surfaceVariant = ForestGreen,
    onSurfaceVariant = Sage,
    surfaceTint = ForestGreen,
    surfaceDim = NearBlack,
    surfaceBright = ForestLight,
    surfaceContainerLowest = NearBlack,
    surfaceContainerLow = ForestDeep,
    surfaceContainer = ForestGreen,
    surfaceContainerHigh = ForestGreen,
    surfaceContainerHighest = ForestLight,
    inverseSurface = Sage,
    inverseOnSurface = ForestGreen,
    outline = Sage,
    outlineVariant = ForestLighter,
    error = Rust,
    onError = Sage,
    scrim = NearBlack,
)

/** Reversed palette, forest green content on sage: the light page, and the cards of the dark theme. */
private val SageColorScheme = ForestColorScheme.copy(
    primary = ForestGreen,
    onPrimary = Sage,
    primaryContainer = ForestGreen,
    onPrimaryContainer = Sage,
    secondary = ForestGreen,
    onSecondary = Sage,
    secondaryContainer = lerp(Sage, ForestGreen, 0.18f),
    onSecondaryContainer = ForestGreen,
    tertiary = ForestGreen,
    onTertiary = Sage,
    background = Sage,
    onBackground = ForestGreen,
    surface = Sage,
    onSurface = ForestGreen,
    surfaceVariant = Sage,
    onSurfaceVariant = ForestGreen,
    surfaceTint = Sage,
    surfaceDim = lerp(Sage, ForestGreen, 0.12f),
    surfaceBright = lerp(Sage, ChallengorColors.White, 0.3f),
    surfaceContainerLowest = Sage,
    surfaceContainerLow = Sage,
    surfaceContainer = Sage,
    surfaceContainerHigh = lerp(Sage, ForestGreen, 0.08f),
    surfaceContainerHighest = lerp(Sage, ForestGreen, 0.12f),
    inverseSurface = ForestGreen,
    inverseOnSurface = Sage,
    outline = ForestGreen,
    outlineVariant = lerp(Sage, ForestGreen, 0.3f),
)

/** The theme the user picked; [OnCard], the shadows and the logo follow it. */
val LocalAppTheme = staticCompositionLocalOf { AppTheme.DARK }

private val AppTheme.pageScheme get() = if (this == AppTheme.LIGHT) SageColorScheme else ForestColorScheme
private val AppTheme.cardScheme get() = if (this == AppTheme.LIGHT) ForestColorScheme else SageColorScheme

@Composable
fun ChallengorTheme(theme: AppTheme = AppTheme.DARK, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppTheme provides theme) {
        MaterialTheme(
            colorScheme = theme.pageScheme,
            content = content,
        )
    }
}

/**
 * Content drawn on a card: everything inside picks up the reversed colours
 * (sage cards on the dark theme, forest green cards on the light one).
 */
@Composable
fun OnCard(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LocalAppTheme.current.cardScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

/** The "C" logo matching the current theme. */
@Composable
fun logoPainter(): Painter = painterResource(
    if (LocalAppTheme.current == AppTheme.LIGHT) Res.drawable.logo_challengor_light else Res.drawable.logo_challengor,
)
