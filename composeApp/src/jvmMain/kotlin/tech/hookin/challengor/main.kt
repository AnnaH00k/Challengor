package tech.hookin.challengor

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.AppTheme
import tech.hookin.challengor.resources.Res
import tech.hookin.challengor.resources.logo_challengor
import tech.hookin.challengor.resources.logo_challengor_light

fun main() = application {
    val container = remember { AppContainer() }
    val theme by container.preferences.theme.collectAsState()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Challengor",
        // The window / taskbar icon follows the chosen theme.
        icon = painterResource(if (theme == AppTheme.LIGHT) Res.drawable.logo_challengor_light else Res.drawable.logo_challengor),
    ) {
        App(container = container)
    }
}
