package tech.hookin.challengor.ui.screens.today

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.LocalState
import tech.hookin.challengor.data.challenge
import tech.hookin.challengor.data.progressOf
import tech.hookin.challengor.data.tasksOf
import tech.hookin.challengor.domain.ChallengeProgress
import tech.hookin.challengor.domain.canEditDay
import tech.hookin.challengor.domain.planStart
import tech.hookin.challengor.domain.tasksForDay
import tech.hookin.challengor.domain.window
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.Participation
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.completionId
import tech.hookin.challengor.ui.uiStateOf

/** A task of one day with what the participant did: ticked, or the logged [amount] for measurable tasks. */
data class DayTask(val task: TaskTemplate, val done: Boolean, val amount: Int? = null) {
    /** Checkbox ticked, or the daily target reached. */
    val isComplete: Boolean
        get() = if (task.isMeasurable) (amount ?: 0) >= task.target!! else done
}

data class DayChecklist(
    val challenge: Challenge,
    val participation: Participation,
    val progress: ChallengeProgress,
    val date: LocalDate,
    val tasks: List<DayTask>,
    val editable: Boolean,
) {
    val allDone: Boolean get() = tasks.isNotEmpty() && tasks.all { it.isComplete }

    /** 1-based day of the participant's own window. */
    val dayNumber: Int get() = participation.startDate.daysUntil(date) + 1
}

data class TodayUiState(
    val today: LocalDate,
    val selectedDate: LocalDate = today,
    /** Checklists of [selectedDate]. */
    val active: List<DayChecklist> = emptyList(),
    val upcoming: List<DayChecklist> = emptyList(),
    /** Whether the user takes part in any challenge at all. */
    val hasParticipations: Boolean = false,
) {
    val isTodaySelected: Boolean get() = selectedDate == today
    val tasksDone: Int get() = active.sumOf { item -> item.tasks.count { it.isComplete } }
    val tasksTotal: Int get() = active.sumOf { it.tasks.size }
    val isEmpty: Boolean get() = !hasParticipations
}

/** Builds the Today tab's state for [userId], showing [selectedDate]. */
fun LocalState.todayUiState(
    userId: String,
    today: LocalDate,
    selectedDate: LocalDate,
): TodayUiState {
    val mine = participations.filter { it.userId == userId && !it.left }
    return TodayUiState(
        today = today,
        selectedDate = selectedDate,
        active = mine
            .filter { selectedDate in it.window }
            .mapNotNull { checklistFor(it, selectedDate, today) }
            .sortedBy { it.allDone },
        upcoming = if (selectedDate != today) emptyList() else mine
            .filter { it.startDate > today }
            .sortedBy { it.startDate }
            .mapNotNull { checklistFor(it, it.startDate, today) },
        hasParticipations = mine.isNotEmpty(),
    )
}

/** The tasks a participant has to do on [date] in one challenge. */
fun LocalState.checklistFor(participation: Participation, date: LocalDate, today: LocalDate): DayChecklist? {
    val challenge = challenge(participation.challengeId) ?: return null
    val progress = progressOf(participation, today) ?: return null
    val dayIndex = challenge.timing.planStart(participation).daysUntil(date)
    val tasks = if (date in participation.window) {
        tasksForDay(challenge.scheduleMode, tasksOf(challenge.id), dayIndex)
    } else {
        emptyList()
    }
    val entries = completions
        .filter { it.participationId == participation.id && it.date == date }
        .associateBy { it.id }
    return DayChecklist(
        challenge = challenge,
        participation = participation,
        progress = progress,
        date = date,
        tasks = tasks.map { task ->
            val entry = entries[completionId(participation.id, task.id, date)]
            DayTask(task, done = entry?.done == true, amount = entry?.amount)
        },
        editable = canEditDay(participation, date, today),
    )
}

class TodayViewModel(
    private val container: AppContainer,
    private val userId: String,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(container.clock.today())

    private fun build(state: LocalState, date: LocalDate) =
        state.todayUiState(userId, container.clock.today(), date)

    val uiState = uiStateOf(
        combine(container.store.state, selectedDate, ::build),
        build(container.store.current, selectedDate.value),
    )

    /** Moves the selection [days] days forward (or back, if negative). */
    fun shiftDays(days: Int) {
        selectedDate.value = selectedDate.value.plus(days, DateTimeUnit.DAY)
    }

    fun goToToday() {
        selectedDate.value = container.clock.today()
    }

    fun setDone(participationId: String, taskId: String, date: LocalDate, done: Boolean) {
        container.challenges.setTaskDone(participationId, taskId, date, done)
    }

    fun logAmount(participationId: String, taskId: String, date: LocalDate, amount: Int) {
        container.challenges.logAmount(participationId, taskId, date, amount)
    }
}
