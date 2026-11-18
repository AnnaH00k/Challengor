package tech.hookin.challengor

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window
import tech.hookin.challengor.ui.utils.challengeIdFromFragment

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Shared links look like https://…/Challengor/#/challenge/<id>
    val sharedChallengeId = challengeIdFromFragment(window.location.hash)
    ComposeViewport {
        App(initialChallengeId = sharedChallengeId)
    }
}
