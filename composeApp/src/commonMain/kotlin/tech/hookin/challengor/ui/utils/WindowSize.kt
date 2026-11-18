package tech.hookin.challengor.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val WideScreenBreakpoint = 600.dp

/** Width of the window the app is drawn in (works on every platform). */
@Composable
fun windowWidth(): Dp = with(LocalDensity.current) {
    LocalWindowInfo.current.containerSize.width.toDp()
}

@Composable
fun isWideScreen(): Boolean = windowWidth() >= WideScreenBreakpoint
