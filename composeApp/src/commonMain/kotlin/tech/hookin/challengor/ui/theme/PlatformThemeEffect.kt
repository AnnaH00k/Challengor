package tech.hookin.challengor.ui.theme

import androidx.compose.runtime.Composable
import tech.hookin.challengor.data.AppTheme

/** Applies [theme] outside the Compose content where the platform needs it, e.g. the system bar icons. */
@Composable
expect fun PlatformThemeEffect(theme: AppTheme)
