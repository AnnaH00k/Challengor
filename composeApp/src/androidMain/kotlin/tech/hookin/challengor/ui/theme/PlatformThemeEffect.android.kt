package tech.hookin.challengor.ui.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import tech.hookin.challengor.data.AppTheme

/** Edge-to-edge with dark system bar icons on the sage light theme, light icons on the forest dark theme. */
fun ComponentActivity.applySystemBars(theme: AppTheme) {
    val style = when (theme) {
        AppTheme.LIGHT -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        AppTheme.DARK -> SystemBarStyle.dark(Color.TRANSPARENT)
    }
    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
}

@Composable
actual fun PlatformThemeEffect(theme: AppTheme) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    LaunchedEffect(activity, theme) { activity.applySystemBars(theme) }
}
