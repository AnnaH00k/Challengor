package tech.hookin.challengor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.lerp
import tech.hookin.challengor.ui.theme.ChallengorColors.ForestDeep
import tech.hookin.challengor.ui.theme.ChallengorColors.ForestGreen
import tech.hookin.challengor.ui.theme.ChallengorColors.ForestLight
import tech.hookin.challengor.ui.theme.ChallengorColors.ForestLighter
import tech.hookin.challengor.ui.theme.ChallengorColors.NearBlack
import tech.hookin.challengor.ui.theme.ChallengorColors.Rust
import tech.hookin.challengor.ui.theme.ChallengorColors.Sage

private val ChallengorColorScheme = darkColorScheme(
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

@Composable
fun ChallengorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChallengorColorScheme,
        content = content,
    )
}

/** Reversed palette for sage cards: forest green content on a sage surface. */
private val SageColorScheme = ChallengorColorScheme.copy(
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
    surfaceContainerLowest = Sage,
    surfaceContainerLow = Sage,
    surfaceContainer = Sage,
    surfaceContainerHigh = lerp(Sage, ForestGreen, 0.08f),
    surfaceContainerHighest = lerp(Sage, ForestGreen, 0.12f),
    outline = ForestGreen,
    outlineVariant = lerp(Sage, ForestGreen, 0.3f),
)

/** Content drawn on a sage card: everything inside picks up the reversed colours. */
@Composable
fun OnSage(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SageColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
