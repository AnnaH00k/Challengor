package tech.hookin.challengor.ui.screens.profile

import androidx.lifecycle.ViewModel
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.account
import tech.hookin.challengor.model.UserProfile
import tech.hookin.challengor.ui.screens.mychallenges.MyChallengesUiState
import tech.hookin.challengor.ui.screens.mychallenges.challengesOfUser
import tech.hookin.challengor.ui.uiStateOf

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isMe: Boolean = false,
    val challenges: MyChallengesUiState = MyChallengesUiState(),
) {
    val completedCount: Int get() = challenges.finished.count { it.progress?.isSuccess == true }
}

class ProfileViewModel(
    container: AppContainer,
    userId: String,
    viewerId: String,
) : ViewModel() {
    val uiState = uiStateOf(container.store) { state ->
        val isMe = userId == viewerId
        ProfileUiState(
            profile = state.account(userId)?.toProfile(includeEmail = isMe),
            isMe = isMe,
            challenges = state.challengesOfUser(userId, viewerId, container.clock.today()),
        )
    }
}
