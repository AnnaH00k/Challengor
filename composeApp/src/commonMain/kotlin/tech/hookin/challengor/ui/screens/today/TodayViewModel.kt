package tech.hookin.challengor.ui.screens.today

import androidx.lifecycle.ViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
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
    val active: List<DayChecklist> = emptyList(),
    val upcoming: List<DayChecklist> = emptyList(),
) {
    val tasksDone: Int get() = active.sumOf { item -> item.tasks.count { it.isComplete } }
    val tasksTotal: Int get() = active.sumOf { it.tasks.size }
    val isEmpty: Boolean get() = active.isEmpty() && upcoming.isEmpty()
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

    val uiState = uiStateOf(container.store) { state ->
        val today = container.clock.today()
        val mine = state.participations.filter { it.userId == userId && !it.left }
        TodayUiState(
            today = today,
            active = mine
                .filter { today in it.window }
                .mapNotNull { state.checklistFor(it, today, today) }
                .sortedBy { it.allDone },
            upcoming = mine
                .filter { it.startDate > today }
                .sortedBy { it.startDate }
                .mapNotNull { state.checklistFor(it, it.startDate, today) },
        )
    }

    fun setDone(participationId: String, taskId: String, done: Boolean) {
        container.challenges.setTaskDone(participationId, taskId, container.clock.today(), done)
    }

    fun logAmount(participationId: String, taskId: String, amount: Int) {
        container.challenges.logAmount(participationId, taskId, container.clock.today(), amount)
    }
}
