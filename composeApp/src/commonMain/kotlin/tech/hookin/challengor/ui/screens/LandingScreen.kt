package tech.hookin.challengor.ui.screens

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.hookin.challengor.ui.components.CenteredRow
import tech.hookin.challengor.ui.components.ChallengorHeadline
import tech.hookin.challengor.ui.components.H2
import tech.hookin.challengor.ui.components.MainButton
import tech.hookin.challengor.ui.components.MainPage
import tech.hookin.challengor.ui.theme.ChallengorTheme

@Composable
fun LandingScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    MainPage {
        ChallengorHeadline()
        H2("Find and create your next challenge")

        CenteredRow {
            MainButton(text = "Login", onClick = onLogin)
            MainButton(text = "Register", onClick = onRegister)
        }
    }
}

@Preview
@Composable
private fun LandingScreenPreview() {
    ChallengorTheme {
        LandingScreen(onLogin = {}, onRegister = {})
    }
}
