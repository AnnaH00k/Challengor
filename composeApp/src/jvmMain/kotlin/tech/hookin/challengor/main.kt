package tech.hookin.challengor

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import tech.hookin.challengor.resources.Res
import tech.hookin.challengor.resources.logo_challengor

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Challengor",
        icon = painterResource(Res.drawable.logo_challengor),
    ) {
        App()
    }
}
