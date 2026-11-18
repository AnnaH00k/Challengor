package tech.hookin.challengor.ui.screens.mychallenges

import androidx.lifecycle.ViewModel
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.LocalState
import tech.hookin.challengor.data.challenge
import tech.hookin.challengor.data.createdBy
import tech.hookin.challengor.data.latestParticipationsOf
import tech.hookin.challengor.data.progressOf
import tech.hookin.challengor.domain.ProgressStatus
import tech.hookin.challengor.model.Visibility
import tech.hookin.challengor.ui.screens.explore.ChallengeListItem
import tech.hookin.challengor.ui.screens.explore.listItem
import tech.hookin.challengor.ui.uiStateOf
import kotlinx.datetime.LocalDate

data class MyChallengesUiState(
    val active: List<ChallengeListItem> = emptyList(),
    val finished: List<ChallengeListItem> = emptyList(),
    val created: List<ChallengeListItem> = emptyList(),
)

/** A user's challenges split into running, finished and self-created. Also used by profiles. */
fun LocalState.challengesOfUser(userId: String, viewerId: String?, today: LocalDate): MyChallengesUiState {
    val joined = latestParticipationsOf(userId).mapNotNull { participation ->
        val challenge = challenge(participation.challengeId) ?: return@mapNotNull null
        if (userId != viewerId && challenge.visibility != Visibility.PUBLIC) {
            return@mapNotNull null
        }
        listItem(challenge, viewerId = null, today).copy(progress = progressOf(participation, today))
    }
    val running = setOf(ProgressStatus.ACTIVE, ProgressStatus.UPCOMING)
    return MyChallengesUiState(
        active = joined.filter { it.progress?.status in running },
        finished = joined.filter { it.progress?.status !in running },
        created = createdBy(userId, viewerId).map { listItem(it, viewerId, today) },
    )
}

class MyChallengesViewModel(
    container: AppContainer,
    userId: String,
) : ViewModel() {
    val uiState = uiStateOf(container.store) { state ->
        state.challengesOfUser(userId, viewerId = userId, today = container.clock.today())
    }
}
