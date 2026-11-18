package tech.hookin.challengor.ui.screens.challenge

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.LocalState
import tech.hookin.challengor.data.account
import tech.hookin.challengor.data.activeParticipation
import tech.hookin.challengor.data.challenge
import tech.hookin.challengor.data.isOpenToJoin
import tech.hookin.challengor.data.tasksOf
import tech.hookin.challengor.domain.window
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.UserProfile
import tech.hookin.challengor.ui.screens.today.DayChecklist
import tech.hookin.challengor.ui.screens.today.checklistFor
import tech.hookin.challengor.ui.uiStateOf

data class ChallengeDetailUiState(
    val today: LocalDate,
    val challenge: Challenge? = null,
    val creator: UserProfile? = null,
    val isCreator: Boolean = false,
    val participants: List<UserProfile> = emptyList(),
    /** Daily tasks under key `null`, day-plan tasks under their day index. */
    val tasksByDay: Map<Int?, List<TaskTemplate>> = emptyMap(),
    val canJoin: Boolean = false,
    /** Set when the viewer takes part: the checklist of [selectedDay] incl. overall progress. */
    val checklist: DayChecklist? = null,
)

class ChallengeDetailViewModel(
    private val container: AppContainer,
    private val challengeId: String,
    private val userId: String,
) : ViewModel() {
    private val selectedDay = MutableStateFlow<LocalDate?>(null)

    val uiState = uiStateOf(
        combine(container.store.state, selectedDay, ::build),
        build(container.store.current, null),
    )

    fun selectDay(date: LocalDate) {
        selectedDay.value = date
    }

    fun join() {
        container.challenges.join(challengeId, userId)
        selectedDay.value = null
    }

    fun leave() = container.challenges.leave(challengeId, userId)

    fun setDone(taskId: String, done: Boolean) {
        val checklist = uiState.value.checklist ?: return
        container.challenges.setTaskDone(checklist.participation.id, taskId, checklist.date, done)
    }

    fun logAmount(taskId: String, amount: Int) {
        val checklist = uiState.value.checklist ?: return
        container.challenges.logAmount(checklist.participation.id, taskId, checklist.date, amount)
    }

    /** Returns `true` if the challenge was deleted. */
    fun delete(): Boolean = container.challenges.delete(challengeId, userId)

    private fun build(state: LocalState, selected: LocalDate?): ChallengeDetailUiState {
        val today = container.clock.today()
        val challenge = state.challenge(challengeId) ?: return ChallengeDetailUiState(today)
        val participation = state.activeParticipation(challengeId, userId)
        val participantIds = state.participations
            .filter { it.challengeId == challengeId && !it.left }
            .map { it.userId }
            .distinct()

        // Default to today, or the nearest day inside the participant's window.
        val day = participation?.let { p ->
            (selected ?: today).takeIf { it in p.window } ?: if (today < p.startDate) p.startDate else p.endDate
        }

        return ChallengeDetailUiState(
            today = today,
            challenge = challenge,
            creator = state.account(challenge.creatorId)?.toProfile(),
            isCreator = challenge.creatorId == userId,
            participants = participantIds.mapNotNull { state.account(it)?.toProfile() },
            tasksByDay = state.tasksOf(challengeId).sortedBy { it.position }.groupBy { it.dayIndex },
            canJoin = participation == null && challenge.isOpenToJoin(today),
            checklist = if (participation != null && day != null) state.checklistFor(participation, day, today) else null,
        )
    }
}
