package tech.hookin.challengor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
    primaryContainer = ForestGreen,
    onPrimaryContainer = Sage,
    secondary = Sage,
    onSecondary = ForestGreen,
    secondaryContainer = ForestLighter,
    onSecondaryContainer = Sage,
    tertiary = Sage,
    onTertiary = ForestGreen,
    background = NearBlack,
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
